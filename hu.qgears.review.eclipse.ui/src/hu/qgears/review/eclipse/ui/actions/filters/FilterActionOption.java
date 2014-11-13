package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.util.Preferences;

import org.eclipse.jface.action.Action;

/**
 * A menu entry for enable / disable filter options. The menu entry is rendered
 * as checkbox.
 * <p>
 * Defines run method to automatically store filter settings in preference
 * store, so values selected by user is saved before separate sessions.
 * 
 * 
 * @author agostoni
 * @see Preferences#rememberFilterStatus(String, boolean)
 */
public abstract class FilterActionOption extends Action {

	public FilterActionOption(String text,boolean defultEnabled) {
		super(text,Action.AS_CHECK_BOX);
		setChecked(defultEnabled);
	}
	
	@Override
	public final void run() {
		Preferences.rememberFilterStatus(getText(), isChecked());
		actionRun(getText(),isChecked());
	}

	/**
	 * Function called, when user selected this menu entry. Subclasses should
	 * implement custom actions here.
	 * 
	 * @param text
	 *            The name of selected filter option
	 * @param checked
	 *            The enabled (checkbox checked) state of menu entry
	 */
	protected abstract void actionRun(String text, boolean checked);
	
}
