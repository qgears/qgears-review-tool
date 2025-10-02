package hu.qgears.review.report;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Eclipse application entry point for {@link ReportGeneratorStandalone}.
 * 
 * @author agostoni
 *
 */
public class ReportGeneratorEclipseApp implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		context.applicationRunning();
		try {
			ReportGeneratorStandalone.main(Platform.getApplicationArgs());
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	@Override
	public void stop() {
	}

}
