package hu.qgears.review.eclipse.ui;


import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The bundle activator for plugin {@value #PLUGIN_ID}.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolUI extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "hu.qgears.review.eclipse.ui"; 
	public static final String ID_MAIN_VIEW = "hu.qgears.review.eclipse.ui.main"; 
	public static final String ID_TODO_LIST_VIEW = "hu.qgears.review.eclipse.ui.todos"; 
	public static final String ID_STATISTICS_VIEW = "hu.qgears.review.eclipse.ui.stats"; 
	private static ReviewToolUI defaultActivator;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		defaultActivator = this;
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		ReviewToolImages.init(reg);
	}
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}
	
	public static ReviewToolUI getDefault() {
		return defaultActivator;
	}
	
}
