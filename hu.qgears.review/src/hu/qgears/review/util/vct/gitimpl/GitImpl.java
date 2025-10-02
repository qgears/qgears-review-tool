package hu.qgears.review.util.vct.gitimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import hu.qgears.commons.UtilProcess;
import hu.qgears.commons.UtilTime;
import hu.qgears.review.action.ReviewToolConfig;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.UtilSha1;
import hu.qgears.review.util.vct.EVersionControlTool;
import hu.qgears.review.util.vct.IVersionControlTool;

/**
 * {@link IVersionControlTool} implementation for GIT repository.
 * 
 * @author agostoni
 *
 */
public class GitImpl implements IVersionControlTool{

	private static final class CLIMonitor extends NullProgressMonitor {
		private int work;
		UtilTime t = UtilTime.createTimer();
		UtilTime full = UtilTime.createTimer();

		@Override
		public void worked(int w) {
			this.work+=w;
			if (this.work % 100 == 0) {
				t.printElapsed("Worked "+work);
			}
		}

		@Override
		public void done() {
			full.printElapsed("Loading git");
		}
	}


	private static final Logger LOG = Logger.getLogger(GitImpl.class);
	
	private static String gitTool = "/usr/bin/git";
	
	@Override
	public List<ReviewSource> loadSources(String id, File targetFolder,ReviewToolConfig rtc,IProgressMonitor m)
			throws Exception {
		if (!targetFolder.exists()){
			throw new IOException("Source folder does not exist: "+targetFolder);
		}
		LOG.info("Loading sources from GIT repository "+targetFolder);
		String folderVersion;
		try {
			folderVersion = getFileVersion(targetFolder,".");
		} catch (Exception e) {
			throw new Exception(targetFolder+" is not a valid GIT repo",e);
		}
		String sourceFolderURL = "file://"+ targetFolder.getCanonicalPath();
		List<ReviewSource> sources = new ArrayList<ReviewSource>();
		List<String> allFiles = stdOutLines(getGitTool() +" -C "+targetFolder+" ls-tree -r HEAD "+targetFolder.getAbsolutePath())
				.filter(rtc::matchesSource)
				.map(gitLine -> {
						int lastTabPos = gitLine.lastIndexOf('\t');
						String filePath = gitLine.substring(lastTabPos+1);
						return filePath;
					})
				.collect(Collectors.toList());
		m.beginTask("", allFiles.size());
		//parallel computation helps a lot here, as there is a lot of waiting time on external 'git' command calls
		allFiles.parallelStream().forEach(filePath ->{
			ReviewSource rs = createReviewSource(id, targetFolder, folderVersion, sourceFolderURL, filePath);
			synchronized (sources) {
				sources.add(rs);
				m.worked(1);
			}
		});
		m.done();
		checkRepoStatus(targetFolder);
		return sources;
	}


	private ReviewSource createReviewSource(String id, File targetFolder, String folderVersion, String sourceFolderURL,
			String filePath) {
		
		try {
			File fileInWorkingCopy = new File(targetFolder,filePath);
			String sha1;
				sha1 = UtilSha1.getSHA1(fileInWorkingCopy);
			String fileVersion = getFileVersion(targetFolder,filePath);
			ReviewSource rs = new ReviewSource(id, sourceFolderURL, filePath, folderVersion, fileVersion, sha1, fileInWorkingCopy, EVersionControlTool.GIT);
			//fillPreviousUrlsFromHistory(targetFolder, filePath, rs);
			return rs;
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load "+filePath,e);
		}
	}
	


	private List<String> getOldVersions(File gitFolder, String filePath) throws IOException {
		//git log --follow --name-status --pretty=format: UtilEventListener.java
		String command = getGitTool() +" -C "+gitFolder+" log --oneline  --follow -n1 -m --first-parent --diff-filter=R --name-status "+filePath;
		return stdOutLines(command)
		.filter(
				s -> {
					return s.startsWith("R");
				}
		)
		.map(this::selectSecondColumn)
		.collect(Collectors.toList());
	}

	
	public static Stream<String> stdOutLines(String process) throws IOException {
		Process p = Runtime.getRuntime().exec(process);
	    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
	    return reader.lines().onClose(() -> {
			try {
				reader.close();
			} catch (IOException e) {
				throw new UncheckedIOException("Cannot close stdout reader after stream is read", e);
			}
		});
	}

	private void checkRepoStatus(File targetFolder) throws IOException {
		String st = UtilProcess.execute(getGitTool() + " -C "+targetFolder.getAbsolutePath()+" status -s");
		if (!st.isEmpty()){
			LOG.warn("There are uncommitted changes in repo: \n"+st);
		}
	}

	private String getFileVersion(File root, String relativePath) throws IOException {
		String command = getGitTool()+ " -C "+root.getAbsolutePath()+" log --no-color --format=oneline -n 1 -- "+relativePath;
		try {
			String log = UtilProcess.execute(command);
			return log.substring(0,log.indexOf(' '));
		} catch (Exception e) {
			throw new IOException("Unexpected output of command : "+command,e);
		}
	}

	public static void main(String[] args) throws Exception {
		File git = new File(args[0]);
		String p = args[1];
		ReviewToolConfig cfg = new ReviewToolConfig();
		//		System.out.println(new GitImpl().getOldVersions(git, p));
		cfg.addSourcePattern(".*\\.java$");
		GitImpl gitImpl = new GitImpl();
		List<ReviewSource> srcs = gitImpl.loadSources("hello", git, cfg, new CLIMonitor());
		gitImpl.fetchOldFileUrls(srcs,new CLIMonitor());
		
		
	}
	@Override
	public void fetchOldFileUrls(List<ReviewSource> srcs, IProgressMonitor m) {
		
		m.beginTask("Fetching old urls from git", srcs.size());
		srcs.parallelStream().forEach(rs -> {
			try  {
				List<String> oldVersions = getOldVersions(rs.getFileInWorkingCopy().getParentFile(), rs.getFileInWorkingCopy().getName());
				for (String oldVersion : oldVersions) {
					rs.addPreviousSourceUrl(oldVersion);
				}
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to fetch old path names of "+rs,e);
			}
			
			synchronized (m) {
				m.worked(1);
			}
		});
		m.done();
		
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


	private String selectSecondColumn(String line) {
		int firstSpace= line.indexOf('\t') +1;
		String c2 =  line.substring(firstSpace,line.indexOf('\t',firstSpace));
		return c2;
	}

}
