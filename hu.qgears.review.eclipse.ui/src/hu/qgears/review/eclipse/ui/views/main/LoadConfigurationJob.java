package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.util.Preferences;
import hu.qgears.review.model.ReviewInstance;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Eclipse {@link Job} for loading review tool configuration on a BG thread
 * (avoid blocking UI during long running operation).
 * 
 * @author agostoni
 * @see LoadConfiguration
 */
public class LoadConfigurationJob extends Job {

	static final String TITLE = "Loading review tool configuration...";
	private ReviewInstance reviewInstance;

	public LoadConfigurationJob() {
		super(TITLE);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		String configurationFile = Preferences.getConfigurationFile();
		IStatus st = Status.OK_STATUS;
		if (configurationFile == null || configurationFile.isEmpty()){
			st = new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, "Review tool configuration file is not set. Please open preference store and set file path!");
		} else {
			if (Preferences.useSVNCache()){
				System.setProperty("use.svn.cache", "true");
			} else {
				System.setProperty("use.svn.cache", "false");
			}
			LoadConfiguration lc = new LoadConfiguration();
			try {
				reviewInstance = lc.loadConfiguration(new File (configurationFile));
			} catch (Exception e) {
				st = new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, "Cannot load configuration",e);
			}
		}
		return st;
	}

	public ReviewInstance getReviewInstance() {
		return reviewInstance;
	}

}