package hu.qgears.review.util;

import java.io.File;

/**
 * Helper methods for filtering
 * version control files.
 * @author rizsi
 *
 */
public class UtilFileFilter {
	/**
	 * Is this file or directory filtered out?
	 * @param f
	 * @return true means that the file must not be involved in processing.
	 */
	public boolean isFilteredOut(File f)
	{
		return ".svn".equals(f.getName());
	}
}
