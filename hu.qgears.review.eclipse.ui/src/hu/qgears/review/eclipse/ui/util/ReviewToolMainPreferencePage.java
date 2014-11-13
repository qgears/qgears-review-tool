package hu.qgears.review.eclipse.ui.util;

import hu.qgears.review.eclipse.ui.ReviewToolUI;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for editing reviewer tool settings in preference store.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolMainPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public ReviewToolMainPreferencePage() {
		super(GRID);
		setPreferenceStore(ReviewToolUI.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		FileFieldEditor ffe = new FileFieldEditor(Preferences.CONFIGURATION_FILE, "Configuration file path", parent);
		addField(ffe);
		
		BooleanFieldEditor bfe = new BooleanFieldEditor(Preferences.SVN_CACHE, "Cache working copy source information", parent);
		addField(bfe);
		
		StringFieldEditor sfe = new StringFieldEditor(Preferences.DEFAULT_USER_NAME, "Default user name for new review entries", parent);
		addField(sfe);
	}

}
