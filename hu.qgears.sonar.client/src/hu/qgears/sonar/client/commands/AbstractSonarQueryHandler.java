package hu.qgears.sonar.client.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.sonar.client.ICommandHandler;
import hu.qgears.sonar.client.model.SonarAPI;

/**
 * Abstract handler that is able to call one service of Sonar REST API.
 * 
 * @author agostoni
 * 
 */
public abstract class AbstractSonarQueryHandler implements ICommandHandler{

	
	private String sonarBaseURL;
	protected SonarAPI api;
	/**
	 * @param sonarBaseURL The base URL of REST API. E.g. http://localhost/sonar/api.
	 */
	public AbstractSonarQueryHandler(String sonarBaseURL) {
		super();
		this.sonarBaseURL = sonarBaseURL;
	}


	@Override
	public final String handleCommand(List<String> cmdParameters) {
		String ans;
		setCommandParameters(cmdParameters);
		Map<String,String> qParams = new HashMap<>();
		addQueryParameters(qParams);
		String address = buildQuery(qParams);
		try {
			ans = processUrl(address);
		} catch (Exception e) {
			ans ="Cannot read URL : "+address+". "+e.getMessage();
		}
		return ans;
	}
	/**
	 * Collect additional URL parameters that must be passed to REST API. 
	 * 
	 * @return
	 */
	protected abstract void addQueryParameters(Map<String, String> qParams);


	protected abstract String processUrl(String address) throws Exception;
	
	/**
	 * Called when a new command is arrived from UI. Subclasses should parse and
	 * store command parameters at this point.
	 * 
	 * @param cmdParameters
	 */
	protected abstract void setCommandParameters(List<String> cmdParameters);



	/**
	 * Constructs a SONAR REST API query using the specified additional URL
	 * parameters.
	 * 
	 * @param qP
	 * @return
	 */
	protected String buildQuery(Map<String, String> qP) {
		StringBuilder bld = new StringBuilder();
		Map<String,String> qParams = new HashMap<String, String>(qP);
		boolean needComma = false;
		bld.append(sonarBaseURL).append("/").append(getServiceName()).append("?");
		for (String key : qParams.keySet()){
			bld.append(needComma ? "&" : "").append(key).append("=").append(qParams.get(key));
			if (!needComma){
				needComma = true;
			}
		}
		return bld.toString();
	}


	/**
	 * Returns the name of the Sonar REST API service, e.g. 'metrics' or 'resources'.
	 * 
	 * @return
	 */
	protected abstract String getServiceName();
	
}
