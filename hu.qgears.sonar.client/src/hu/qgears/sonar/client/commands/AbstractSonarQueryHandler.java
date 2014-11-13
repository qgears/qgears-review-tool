package hu.qgears.sonar.client.commands;

import hu.qgears.sonar.client.ICommandHandler;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Abstract handler that is able to call one service of Sonar REST API, and
 * parses the answer XML message using DOM4j. Subclasses has to process the
 * parsed XML {@link Document}.
 * 
 * @author agostoni
 * 
 */
public abstract class AbstractSonarQueryHandler implements ICommandHandler{

	private String sonarBaseURL;
	private DocumentBuilderFactory factory;
	
	/**
	 * @param sonarBaseURL The base URL of REST API. E.g. http://localhost/sonar/api.
	 */
	public AbstractSonarQueryHandler(String sonarBaseURL) {
		super();
		this.sonarBaseURL = sonarBaseURL;
		factory = DocumentBuilderFactory.newInstance();
	}


	@Override
	public final String handleCommand(List<String> cmdParameters) {
		String ans;
		setCommandParameters(cmdParameters);
		Map<String,String> qParams = getQueryParameters();
		String address = buildQuery(qParams);
		try {
			Document xmlRespose = read(address);
			ans =processSonarResponse(xmlRespose);
		} catch (Exception e) {
			ans ="Cannot read URL : "+address+". "+e.getMessage();
		}
		return ans;
	}

	
	/**
	 * Called when a new command is arrived from UI. Subclasses should parse and
	 * store command parameters at this point.
	 * 
	 * @param cmdParameters
	 */
	protected abstract void setCommandParameters(List<String> cmdParameters);


	/**
	 * Process the XML document retrieved from REST API here.
	 * 
	 * @param xmlRespose
	 * @return The answer that must be print to console
	 */
	protected abstract String processSonarResponse(Document xmlRespose);		


	/**
	 * Reads data from specified URL, and parses the answer as an XML document.
	 * 
	 * @param urlS
	 * @return
	 * @throws Exception
	 */
	protected Document read(String urlS) throws Exception{
		URL url = new URL(urlS);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = url.openStream();
		return builder.parse(is);
	}


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
		qParams.put("format", "xml");
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
	
	/**
	 * Collect additional URL parameters that must be passed to REST API. Return
	 * an empty map, if no need for additional params.
	 * 
	 * @return
	 */
	protected abstract Map<String, String> getQueryParameters();

}
