package hu.qgears.review.eclipse.ui.util;

import hu.qgears.review.action.SourceCache;
import hu.qgears.review.eclipse.ui.ReviewToolUI;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Utility class for accessing review tool preference store.
 * 
 * @author agostoni
 *
 */
public final class Preferences {

	public static IPreferenceStore preferenceStore = ReviewToolUI.getDefault().getPreferenceStore(); 
	
	public static final String CONFIGURATION_FILE = "hu.qgears.review.config.file";
	
	private static final String FILTER_PREFIX = "hu.qgears.review.filters.";
	private static final String LINK_WITH_EDITOR_TOGGLE_PREFIX = "hu.qgears.review.link.with.editor.";

	public static final String SVN_CACHE = "hu.qgears.review.svn.cache";

	public static final String DEFAULT_USER_NAME = "hu.qgears.review.default.user.name";
	public static final String DEFAULT_REPORT_PATH = "hu.qgears.review.default.report.path";
	
	
	private Preferences (){}
	
	/**
	 * Returns the status of specified filter option.
	 * 
	 * @param fo
	 *            The name (id) of filter option
	 * @return <code>true</code> if filter is enabled <code>false</code>
	 *         otherwise.
	 */
	public static boolean getFilterStatus(String fo){
		String filterOption = FILTER_PREFIX+fo;
		preferenceStore.setDefault(filterOption, true);
		return preferenceStore.getBoolean(filterOption);
	}

	/**
	 * Saves the status of specified filter option in preference store.
	 * 
	 * @param fo  The name (id) of filter option
	 * @param enabled the state to save.
	 */
	public static void rememberFilterStatus(String fo,boolean enabled){
		String filterOption = FILTER_PREFIX+fo;
		preferenceStore.setValue(filterOption, enabled);
	}
	
	/**
	 * Returns the file path of review tool configuration file (*.mapping)
	 * 
	 * @return
	 */
	public static String getConfigurationFile(){
		return preferenceStore.getString(CONFIGURATION_FILE);
	}
	
	/**
	 * Returns true if SVN cache is enabled. See {@link SourceCache}
	 * 
	 * @return
	 */
	public static boolean useSVNCache(){
		return preferenceStore.getBoolean(SVN_CACHE);
	}
	
	/**
	 * Returns the user name that will be used by default for creating new
	 * reviewer entries, or displaying todo list.
	 * 
	 * @return
	 */
	public static String getDefaultUserName() {
		return preferenceStore.getString(DEFAULT_USER_NAME);
	}

	/**
	 * Returns the toggle state of "Link with editor" button on viewer toolbars.
	 * 
	 * @param viewId The id of target view
	 * @return
	 */
	public static boolean getLinkWithEditorToggleState(String viewId) {
		return preferenceStore.getBoolean(LINK_WITH_EDITOR_TOGGLE_PREFIX+viewId);
	}
	/**
	 * Saves the toggle state of "Link with editor" button on viewer toolbars.
	 * 
	 * @param viewId The id of target view
	 * @param state the new state
	 */
	public static void setLinkWithEditorToggleState(String viewId,boolean state) {
		preferenceStore.setValue(LINK_WITH_EDITOR_TOGGLE_PREFIX+viewId,state);
	}

	/**
	 * Returns a folder name where exported HTML reports will be saved by default.
	 * 
	 * @return
	 */
	public static String getDefaultReportPath() {
		return preferenceStore.getString(DEFAULT_REPORT_PATH);
	}
}
