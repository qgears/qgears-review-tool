package hu.qgears.review.eclipse.ui.util;

import hu.qgears.review.eclipse.ui.ReviewToolUI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

/**
 * Utility for showing log messages using built-in {@link ErrorDialog}.
 * 
 * @author agostoni
 * 
 */
public class UtilLog {

	private UtilLog() {}

	/**
	 * Opens an {@link ErrorDialog} and showings specified message and stack
	 * trace. A log message will be created into default logger associated to
	 * {@link ReviewToolUI} bundle.
	 * 
	 * @param message The error message
	 * @param exc The Exception holding stack trace information
	 */
	public static void showErrorDialog(String message,Throwable exc){
		Status status = new Status(IStatus.ERROR,ReviewToolUI.PLUGIN_ID,exc == null ? message : exc.getMessage(),exc);
		ErrorDialog .openError(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Error", message, status);
		ReviewToolUI.getDefault().getLog().log(status);
	}
	
}
