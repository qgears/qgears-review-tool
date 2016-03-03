package hu.qgears.review.util.vct;

import hu.qgears.review.model.ReviewSource;

import java.io.File;
import java.util.List;

/**
 * Defines the interface between the review tool and the underlying version
 * control tool.
 * 
 * @author agostoni
 *
 */
public interface IVersionControlTool {

	/**
	 * Loads sources from specified directory
	 * 
	 * @param id
	 * @param file
	 * @return
	 * @throws Exception
	 */
	List<ReviewSource> loadSources(String id, File file) throws Exception;

	/**
	 * Download a source file from the scm server.
	 * 
	 * @param svnurl the URL of the file
	 * @param revision the revision/commit id of the file
	 * @return
	 * @throws Exception
	 */
	byte[] downloadResource(String svnurl, String revision) throws Exception;

}
