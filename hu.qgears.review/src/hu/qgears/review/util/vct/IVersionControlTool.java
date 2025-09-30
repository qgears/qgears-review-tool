package hu.qgears.review.util.vct;

import hu.qgears.review.action.ReviewToolConfig;
import hu.qgears.review.model.ReviewSource;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Defines the interface between the review tool and the underlying version
 * control tool.
 * 
 * @author agostoni
 * @since 2.0
 *
 */
public interface IVersionControlTool {

	/**
	 * Loads sources from specified directory
	 * 
	 * @param id
	 * @param file
	 * @param cfg 
	 * @return
	 * @throws Exception
	 */
	List<ReviewSource> loadSources(String id, File file,ReviewToolConfig cfg, IProgressMonitor mon) throws Exception;

	/**
	 * Download a source file from the scm server.
	 * 
	 * @param svnurl the URL of the file
	 * @param revision the revision/commit id of the file
	 * @return
	 * @throws Exception
	 */
	byte[] downloadResource(String svnurl, String revision) throws Exception;

	/**
	 * Searches for the old paths where the given source file was originally committed, and moved from. 
	 * Will fill {@link ReviewSource#getPreviousSourceUrls()} list.
	 * 
	 * @param srcs
	 * @param m
	 */
	void fetchOldFileUrls(List<ReviewSource> srcs, IProgressMonitor m);

}
