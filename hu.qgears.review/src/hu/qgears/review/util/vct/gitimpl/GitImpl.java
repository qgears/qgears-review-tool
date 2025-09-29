package hu.qgears.review.util.vct.gitimpl;

import hu.qgears.commons.UtilProcess;
import hu.qgears.review.action.ReviewToolConfig;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.UtilSha1;
import hu.qgears.review.util.vct.EVersionControlTool;
import hu.qgears.review.util.vct.IVersionControlTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * {@link IVersionControlTool} implementation for GIT repository.
 * 
 * @author agostoni
 *
 */
public class GitImpl implements IVersionControlTool{

	private static final Logger LOG = Logger.getLogger(GitImpl.class);
	
	private static String gitTool = "/usr/bin/git";
	
	@Override
	public List<ReviewSource> loadSources(String id, File targetFolder,ReviewToolConfig rtc,IProgressMonitor m)
			throws Exception {
		if (!targetFolder.exists()){
			throw new IOException("Source folder does not exist: "+targetFolder);
		}
		LOG.info("Loading sources from GIT repository "+targetFolder);
		String folderVersion = getFileVersion(targetFolder,".");
		String sourceFolderURL = "file://"+ targetFolder.getCanonicalPath();
		List<ReviewSource> sources = new ArrayList<ReviewSource>();
		String files = UtilProcess.execute(getGitTool() +" -C "+targetFolder+" ls-tree -r HEAD "+targetFolder.getAbsolutePath());
		String[] allFiles = files.split(("\\r?\\n"));
		m.beginTask("", allFiles.length);
		for (String file : allFiles){
			if (rtc.matchesSource(file)){
				String[] columns = file.split("\\\t");
				String filePath = columns[1];
				File fileInWorkingCopy = new File(targetFolder,filePath);
				String sha1 = UtilSha1.getSHA1(fileInWorkingCopy);
				String fileVersion = getFileVersion(targetFolder,filePath);
				ReviewSource rs = new ReviewSource(id, sourceFolderURL, filePath, folderVersion, fileVersion, sha1, fileInWorkingCopy, EVersionControlTool.GIT);
				fillPreviousUrlsFromHistory(filePath, rs);
				
				sources.add(rs);
			}
			m.worked(1);
		}
		m.done();
		checkRepoStatus(targetFolder);
		return sources;
	}

	/**
	 * Searches for the old paths where the given source file was moved from. The GIT history it used to identify move operations.
	 * @param filePath
	 * @param rs
	 */
	private void fillPreviousUrlsFromHistory(String filePath, ReviewSource rs) {
		if (filePath.contains("src/main/java")) {
			//TODO mine it from history
			//git log --follow --name-status --pretty=format: UtilEventListener.java
			rs.addPreviousSourceUrl(filePath.replace("src/main/java", "src"));
		}
	}

	private void checkRepoStatus(File targetFolder) throws IOException {
		String st = UtilProcess.execute(getGitTool() + " -C "+targetFolder.getAbsolutePath()+" status -s");
		if (!st.isEmpty()){
			LOG.warn("There are uncommitted changes in repo: \n"+st);
		}
	}

	private String getFileVersion(File root, String relativePath) throws IOException {
		String log = UtilProcess.execute(getGitTool()+ " -C "+root.getAbsolutePath()+" log --no-color --format=oneline -n 1 -- "+relativePath);
		return log.split(" ")[0];
	}

	public static void main(String[] args) throws Exception {
		new GitImpl().loadSources("hello", new File("/home/agostoni/git-hub/repository-builder/opensource-utils"), new ReviewToolConfig(), new NullProgressMonitor());
	}
	
	@Override
	public byte[] downloadResource(String svnurl, String revision)
			throws Exception {
		//TODO implement
		throw new UnsupportedOperationException();
	}
	
	public static String getGitTool() {
		return gitTool;
	}

}
