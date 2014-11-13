package hu.qgears.sonar.client;

import java.util.List;

/**
 * A command handle is able to process and execute a particular command read
 * from command line interface. The handler must implement this interface, and
 * register itself using
 * {@link SonarCommandLineInterface#registerCommandHandler(String, ICommandHandler)}
 * .
 * 
 * @author agostoni
 * 
 */
public interface ICommandHandler {

	/**
	 * Executes the logic of command.
	 * 
	 * @param parameters The command parameters, that was parsed from input message.  
	 * @return An answer message that will be printed to user.
	 */
	String handleCommand(List<String> parameters);

}
