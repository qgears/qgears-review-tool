package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.util.vct.EVersionControlTool;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

public class VersionContolExtensionManager {

	private static final String VCT_EXT_ID = "hu.qgears.reivew.eclipse.ui.vct";
	private static Map<EVersionControlTool,IVersionControlToolUi> contributions;
	
	public static IVersionControlToolUi getVersionControlToolUi(EVersionControlTool tool){
		if (tool == null){
			throw new IllegalArgumentException("tool must not be null");
		}
		synchronized (VersionContolExtensionManager.class) {
			if (contributions == null){
				initialize();
			}
		}
		if (contributions.containsKey(tool)){
			return contributions.get(tool);
		} else {
			return new DefaultVersionControlToolImplementation(tool);
		}
	}
	
	private static synchronized void initialize() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
	    IConfigurationElement[] elements = reg.getConfigurationElementsFor(VCT_EXT_ID);
	    contributions = new HashMap<EVersionControlTool, IVersionControlToolUi>();
	    for (final IConfigurationElement ce : elements){
	    	SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					IVersionControlToolUi vctui = (IVersionControlToolUi) ce.createExecutableExtension("class");
					EVersionControlTool id = vctui.getToolId();
					if (id == null){
						throw new Exception("Tool id shouldn't be null "+id);
					}
					if (contributions.containsKey(id)){
						throw new Exception("Duplicate implementation for tool: "+id);
					} else {
						contributions.put(id, vctui);
					}
				}
				@Override
				public void handleException(Throwable arg0) {
					UtilLog.showErrorDialog("Error while loading VCT UI extension point impl: " +ce.getName(), arg0);
				}
			});
	    }
	}
	
}
