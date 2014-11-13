package hu.qgears.review.eclipse.ui.util;

import hu.qgears.review.eclipse.ui.ReviewToolUI;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore ps = ReviewToolUI.getDefault().getPreferenceStore();
		ps.setDefault(Preferences.SVN_CACHE, true);
		ps.setDefault(Preferences.DEFAULT_USER_NAME, System.getProperty("user.name"));
	}

}
