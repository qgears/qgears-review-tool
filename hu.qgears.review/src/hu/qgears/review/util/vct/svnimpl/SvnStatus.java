package hu.qgears.review.util.vct.svnimpl;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilProcess;
import hu.qgears.review.action.ReviewToolConfig;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.tool.UtilDom4j;
import hu.qgears.review.util.UtilSha1;
import hu.qgears.review.util.vct.EVersionControlTool;
import hu.qgears.review.util.vct.IVersionControlTool;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Execute SVN status on a folder.
 * Retrieves the following information:
 *  * SVN url of the folder
 *  * SVN status of the files: uncommitted modified files are logged.
 * @author rizsi
 *
 */
public class SvnStatus implements IVersionControlTool {
	private static final Logger LOG = Logger.getLogger(SvnStatus.class);
	private static String svnTool = "/usr/bin/svn";
	@Override
	public List<ReviewSource> loadSources(String id, File dir,ReviewToolConfig rtc,IProgressMonitor m) throws Exception {
		LOG.info("Loading source from SVN working copy "+dir);
		List<ReviewSource> ret=new ArrayList<ReviewSource>();
		String svnurl=getSvnUrl(dir);
		String s=UtilProcess.execute(Runtime.getRuntime().exec(getSvnTool() + " status -v --xml", null, dir));
		Document doc=UtilDom4j.read(new StringReader(s));
		List<Element> es=UtilDom4j.selectElements(doc.getRootElement(), "target/entry[@path='.']/wc-status");
		String rev=es.get(0).attributeValue("revision");
		
		List<Element> svnSrcDomEntries = UtilDom4j.selectElements(doc.getRootElement(), "target/entry");
		m.beginTask("", svnSrcDomEntries.size());
		for(Element e:svnSrcDomEntries)
		{
			String mod=((Element)e.selectSingleNode("wc-status")).attributeValue("item");
			String path=e.attributeValue("path");
			if (rtc.matchesSource(path)){
				if ("added".equals(mod)) {
					LOG.error("Uncommitted file in the working copy: " + path);
				} else if(!mod.equals("unversioned") && !path.equals(".") && (!mod.equals("obstructed")) )
				{
					Element commit=((Element)e.selectSingleNode("wc-status/commit"));
					String ver=commit.attributeValue("revision");
					if(!mod.equals("normal"))
					{
						System.err.println("Working copy is not clean! " +path);
					}
					String sha1=null;
					File f=new File(dir, path);
					if(f.isFile())
					{
						sha1=UtilSha1.getSHA1(f);
					}
					ret.add(new ReviewSource(id, svnurl, path, rev, ver, sha1,f,EVersionControlTool.SVN));
				}
			}
			m.worked(1);
		}
		return ret;
	}
	
	/**
	 * Queries the SVN repository URL in an SVN working copy directory.
	 * @param dir SVN working copy directory, in which the 
	 * {@link #getSvnTool() svn tool} will be run to retrieve the repository URL 
	 * @return the SVN repository URL of the given working directory
	 * @throws IOException if IO-related error occurs during the 
	 * execution of the SVN tool  
	 * @throws DocumentException if the expected xml output of the svn
	 * tool cannot be parsed 
	 * @throws InterruptedException if the svn tool is interrupted
	 */
	private String getSvnUrl(final File dir) throws IOException,
			DocumentException, InterruptedException {
		final String svnInfoCommand = getSvnTool() + " info --xml";
		final Process svnInfoProcess = Runtime.getRuntime().exec(
				svnInfoCommand, null, dir);
		final String svnInfo = UtilProcess.execute(svnInfoProcess);
		
		try {
			final Document doc = UtilDom4j.read(new StringReader(svnInfo));
			final List<Element> es = UtilDom4j.selectElements(
					doc.getRootElement(), "//entry/url");
			String url=es.get(0).getText();
			
			return url;
		} catch (final DocumentException de) {
			LOG.error("Could not parse the output of command `"
					+ svnInfoCommand + "` executed in directory: " + dir
					+ "; erroneous output: \n" + svnInfo, de);
			
			throw de;
		} finally {
			try {
				final int svnInfoExitCode = svnInfoProcess.waitFor();
				
				if (svnInfoExitCode != 0) {
					LOG.warn("Exit code of `"+ svnInfoCommand + "` in" 
							+ dir.toString() + ": " + svnInfoExitCode);
				}
			} catch (final Exception e) {
				LOG.warn("Exception during running the `" + svnInfoCommand + "`", e);
			}
		}
	}
	
	public static void setSvnTool(String svnTool) {
		SvnStatus.svnTool = svnTool;
	}
	
	public static String getSvnTool() {
		return svnTool;
	}
	@Override
	public byte[] downloadResource(String svnurl, String revision)
			throws Exception {
		Process p=Runtime.getRuntime().exec(new String[]{svnTool, "cat",
				"-r",revision, svnurl});
		Future<Pair<byte[], byte[]>> fut=UtilProcess.saveOutputsOfProcess(p);
		Pair<byte[], byte[]> pa=fut.get();
		return pa.getA();
	}
}
