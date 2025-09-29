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
 * @since 3.0
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
		showErrorDialog("Error",message,exc);
	}
	public static void showErrorDialog(String dialogTitle, String message,Throwable exc){
		Status status = new Status(IStatus.ERROR,ReviewToolUI.PLUGIN_ID,exc == null ? message : exc.getMessage(),exc);
		ErrorDialog .openError(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				dialogTitle, message, status);
		ReviewToolUI.getDefault().getLog().log(status);
	}
	
	public static void logError(String message, Throwable ex){
		Status status = new Status(IStatus.ERROR,ReviewToolUI.PLUGIN_ID,message,ex);
		ReviewToolUI.getDefault().getLog().log(status);
	}
	public static void logInfo(String message){
		logInfo(message, null);
	}
	public static void logInfo(String message, Throwable ex){
		Status status = new Status(IStatus.INFO,ReviewToolUI.PLUGIN_ID,message,ex);
		ReviewToolUI.getDefault().getLog().log(status);
	}
	
}
