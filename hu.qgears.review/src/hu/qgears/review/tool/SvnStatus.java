package hu.qgears.review.tool;

import hu.qgears.commons.UtilProcess;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.UtilSha1;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * Execute SVN status on a folder.
 * Retrieves the following information:
 *  * SVN url of the folder
 *  * SVN status of the files: uncommitted modified files are logged.
 * @author rizsi
 *
 */
public class SvnStatus {
	public List<ReviewSource> execute(String id, File dir) throws Exception
	{
		List<ReviewSource> ret=new ArrayList<ReviewSource>();
		String svnurl=getSvnUrl(dir);
		String s=UtilProcess.execute(Runtime.getRuntime().exec("/usr/bin/svn status -v --xml", null, dir));
		Document doc=UtilDom4j.read(new StringReader(s));
		List<Element> es=UtilDom4j.selectElements(doc.getRootElement(), "target/entry[@path='.']/wc-status");
		String rev=es.get(0).attributeValue("revision");
		for(Element e:UtilDom4j.selectElements(doc.getRootElement(), "target/entry"))
		{
			String mod=((Element)e.selectSingleNode("wc-status")).attributeValue("item");
			String path=e.attributeValue("path");
			
			if ("added".equals(mod)) {
				System.err.println("Uncommitted file in the working copy: " + path);
			} else if(!mod.equals("unversioned") && !path.equals("."))
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
				ret.add(new ReviewSource(id, svnurl, path, rev, ver, sha1,f));
//				System.out.print(""+mod+" ");
//				System.out.println(""+e.attributeValue("path"));
			}
		}
		return ret;
	}
	private String getSvnUrl(File dir) throws IOException, DocumentException {
		String svninfo=UtilProcess.execute(Runtime.getRuntime().exec("/usr/bin/svn info --xml", null, dir));
		Document doc=UtilDom4j.read(new StringReader(svninfo));
		List<Element> es=UtilDom4j.selectElements(doc.getRootElement(), "//entry/url");
		String url=es.get(0).getText();
		return url;
	}
}
