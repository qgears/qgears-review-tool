package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.action.ConfigParsingResult;
import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.action.ConfigParsingResult.Problem;
import hu.qgears.review.action.ConfigParsingResult.Problem.Type;
import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.preferences.Preferences;
import hu.qgears.review.model.ReviewInstance;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

/**
 * Eclipse {@link Job} for loading review tool configuration on a BG thread
 * (avoid blocking UI during long running operation).
 * 
 * @author agostoni
 * @see LoadConfiguration
 */
public class LoadConfigurationJob extends Job {

	public static final String TITLE = "Loading review tool configuration...";
	
	/**
	 * Lookup table for converting {@link Problem#Type} values to integers 
	 * suitable for Eclipse APIs.
	 */
	@SuppressWarnings("serial") // It will not be serialized or even used elsewhere
	private static final Map<Problem.Type, Integer> problemTypeMap = 
			Collections.unmodifiableMap(new HashMap<Problem.Type, Integer>() {
				{
					put(Problem.Type.ERROR, IStatus.ERROR);
					put(Problem.Type.WARNING, IStatus.WARNING);
				}
			});
	private ReviewInstance reviewInstance;

	public LoadConfigurationJob() {
		super(TITLE);
	}
	
	private IStatus convertToStatus(final Problem problem) {
		final String lineSeparator = System.getProperty("line.separator");
		final Type problemType = problem.getType();
		final Integer severity = problemTypeMap.get(problemType);
		final String problemDetails = problem.getDetails();

		return new Status(severity == null ? IStatus.ERROR : severity, 
				ReviewToolUI.PLUGIN_ID, problem.getMessage() + 
				(problemDetails == null || problemDetails.isEmpty() ?
						"" : lineSeparator + problemDetails), 
				problem.getException());
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor m = SubMonitor.convert(monitor);
		try {
			final String configurationFile = Preferences.getConfigurationFile();
			IStatus status = Status.OK_STATUS;
			m.beginTask("Loading..", 1);
			if (configurationFile == null || configurationFile.isEmpty()){
				status = new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, "Review " +
						"tool configuration file is not set. Please open the " +
						"preference store and set file path!");
			} else {
				if (Preferences.useSVNCache()){
					System.setProperty("use.svn.cache", "true");
				} else {
					System.setProperty("use.svn.cache", "false");
				}
				try {
					final LoadConfiguration lc = new LoadConfiguration();
					final ConfigParsingResult configParsingResult = 
							lc.loadConfiguration(new File (configurationFile));
					reviewInstance = configParsingResult.getReviewInstance();
					
					final List<Problem> configParsingProblems = 
							configParsingResult.getProblems();
					
					if (configParsingProblems != null && !configParsingProblems.isEmpty()) {
						final Bundle bundle = Platform.getBundle(ReviewToolUI.PLUGIN_ID);
						final ILog log = Platform.getLog(bundle);
						
						for (final Problem problem : configParsingProblems) {
							log.log(convertToStatus(problem));
						}
						
						status = new Status(IStatus.WARNING, ReviewToolUI.PLUGIN_ID, 
								"Problems have been encountered during loading " +
								"the review configuration.");
					}
				} catch (final Exception e) {
					status = new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, 
							"Could not load review configuration because of a " +
									"critical problem", e);
				}
			}
			return status;
		} finally {
			m.done();
		}
	}

	public ReviewInstance getReviewInstance() {
		return reviewInstance;
	}

}