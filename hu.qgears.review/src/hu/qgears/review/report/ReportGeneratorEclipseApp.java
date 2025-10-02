package hu.qgears.review.report;

import java.net.URL;

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
			if (System.getProperty("log4j.configuration") == null) {
				URL r =ReportGeneratorStandalone.class.getResource("/log4j.properties");
				System.setProperty("log4j.configuration", r.toExternalForm());
			}
			ReportGeneratorStandalone.main(Platform.getApplicationArgs());
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	@Override
	public void stop() {
	}

}
