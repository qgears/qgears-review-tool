package hu.qgears.review.eclipse.ui.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 * @since 3.0
 */
public class UtilWorkspace {

	/**
	 * Returns the {@link IFile} representation of the given file. If the file
	 * is not present the Eclipse workspace, than
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static IFile getFileInWorkspace(File file) {
		IFile wsFile = null;
		try {
			wsFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
			if (wsFile == null) {
				wsFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getCanonicalPath()));
			}
		} catch (IOException e) {
			UtilLog.logError("Error resolving workspace file. ", e);
		}
		return wsFile;
	}

}
