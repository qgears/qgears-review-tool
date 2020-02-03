package hu.qgears.sonar.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hu.qgears.sonar.client.commands.post67.SonarIssueFinder;
import hu.qgears.sonar.client.commands.post67.SonarMetricsHandler67;
import hu.qgears.sonar.client.commands.post67.SonarResourceHandler67;
import hu.qgears.sonar.client.commands.post67.SonarResourceMetricsHandler67;
import hu.qgears.sonar.client.commands.post67.SonarRuleFinder;
import hu.qgears.sonar.client.commands.pre43.SonarMetricsHandler;
import hu.qgears.sonar.client.commands.pre43.SonarResourceHandler;
import hu.qgears.sonar.client.commands.pre43.SonarResourceMetricsHandler;
import hu.qgears.sonar.client.model.SonarAPI;

/**
 * Simple command line based Java client for the REST API of Sonar.
 * <p>
 * Reads commands from {@link System#in}, identifies
 * corresponding {@link ICommandHandler} and delegates the command execution to
 * it.
 * 
 * @author agostoni
 * 
 */
public class SonarCommandLineInterface {

	private Map<String,ICommandHandler> commands = new HashMap<String, ICommandHandler>();
	private Logger logger = Logger.getLogger(getClass().getName());
	private boolean end;
	
	/**
	 * Terminates the client when user has typed the keyword {@value #KEY}.
	 * 
	 * @author agostoni
	 * 
	 */
	private class ExitHandler implements ICommandHandler{

		private static final String KEY = "exit";
		
		@Override
		public String handleCommand(List<String> parameters) {
			end = true;
			return "Sonar client closed!";
		}
	}
	private class HelpHandler implements ICommandHandler{
		
		private static final String KEY = "help";
		
		@Override
		public String handleCommand(List<String> parameters) {
			StringBuilder bld = new StringBuilder("Available commands\n");
			for (String c : commands.keySet() ){
				bld.append(c).append("\n");
			}
			return bld.toString();
		}
	}

	public SonarCommandLineInterface() {
		registerCommandHandler(ExitHandler.KEY,new ExitHandler());
		registerCommandHandler(HelpHandler.KEY,new HelpHandler());
	}
	
	
	/**
	 * Binds the handler to given key. If the first word of input message read
	 * from {@link System#in} equals key, then the command execution will be
	 * delegated to specified handler.
	 * <p>
	 * Two handler with same key is not permitted, the first one will be
	 * registered only.
	 * 
	 * @param key
	 * @param handler
	 */
	public void registerCommandHandler(String key, ICommandHandler handler) {
		if (!commands.containsKey(key)){
			commands.put(key, handler);
		} else {
			logger.log(Level.SEVERE,"Unable to register handler with this key : "+key+". A handler is already registered with same key "+commands.get(key).getClass().getName());
		}
	}


	public void start() throws Exception {
		BufferedReader bfr = null;
		try {
			bfr = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			while (!end){
				String line = bfr.readLine();
				parseCommand(line);
			}
		}finally {
			if (bfr != null){
				bfr.close();
			}
		}
	}
	
	private static void printToOutput(String message){
		System.out.println(message);
	}
	
	private void parseCommand(String line) {
		String[] parts = line.split("\\s+");
		if (parts.length > 0){
			String cmd = parts[0];
			ICommandHandler handler = commands.get(cmd);
			String answer;
			if (handler != null){
				List<String> params  = parts.length > 1 ? Arrays.asList(parts).subList(1,parts.length) : Collections.<String>emptyList();
				answer = handler.handleCommand(params  );
			}else {
				answer = "Unkwown commad "+cmd;
			}
			printToOutput(answer);
		}
	}
	
	/**
	 * Starts the CLI client.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0){
			String sonarBaseUrl = args[0];
			SonarCommandLineInterface sif = new SonarCommandLineInterface();
			SonarAPI api = SonarAPI.PRE_4_3;
			if (args.length > 1){
				api = SonarAPI.valueOf(args[1]);
			}
			switch (api) {
			case POST_6_7:
				sif.registerCommandHandler(SonarMetricsHandler67.KEY, new SonarMetricsHandler67(sonarBaseUrl));
				sif.registerCommandHandler(SonarResourceHandler67.KEY, new SonarResourceHandler67(sonarBaseUrl));
				sif.registerCommandHandler(SonarResourceMetricsHandler67.KEY, new SonarResourceMetricsHandler67(sonarBaseUrl));
				sif.registerCommandHandler(SonarIssueFinder.KEY, new SonarIssueFinder(sonarBaseUrl));
				sif.registerCommandHandler(SonarRuleFinder.KEY, new SonarRuleFinder(sonarBaseUrl));
				break;
			case PRE_4_3:
			default:
				sif.registerCommandHandler(SonarMetricsHandler.KEY, new SonarMetricsHandler(sonarBaseUrl));
				sif.registerCommandHandler(SonarResourceHandler.KEY, new SonarResourceHandler(sonarBaseUrl));
				sif.registerCommandHandler(SonarResourceMetricsHandler.KEY, new SonarResourceMetricsHandler(sonarBaseUrl));
				break;
			}
			try {
				sif.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			printToOutput("Please specify SONAR base URL as first propgram argument!");
		}
	}
}
