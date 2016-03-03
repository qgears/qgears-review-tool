package hu.qgears.review.web;

import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.tool.ConfigParsingResult;

import java.io.File;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;

/**
 * Start the annotation (review) server's web server.
 * @author rizsi
 *
 */
public class StartJetty {
	public static void main(String[] args) throws Exception {
		if(args.length<3)
		{
			throw new RuntimeException("Program requires 3 parameters. (Host, port and configuration file name.)");
		}
		
		final ConfigParsingResult configParsingResult =
				new LoadConfiguration().loadConfiguration(new File(args[2]));
		final ReviewInstance instance= configParsingResult.getReviewInstance();
		Server s=new Server();
		SocketConnector conn=new SocketConnector();
		conn.setHost(args[0]);
		conn.setPort(Integer.parseInt(args[1]));
		s.setConnectors(new Connector[]{conn});
		s.setHandler(new WebHandler(instance));
		s.start();
		s.join();
	}
}
