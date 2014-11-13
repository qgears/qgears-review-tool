package hu.qgears.review.web;

import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.model.ReviewInstance;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;

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
		ReviewInstance instance=new LoadConfiguration().loadConfiguration(new File(args[2]));
		Server s=new Server();
		SocketConnector conn=new SocketConnector();
		conn.setHost(args[0]);
		conn.setPort(Integer.parseInt(args[1]));
		s.setConnectors(new Connector[]{conn});
		s.addHandler(new WebHandler(instance));
		s.start();
		while(true)
		{
			Thread.sleep(1000);
		}
	}
}
