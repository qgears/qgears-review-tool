package hu.qgears.sonar.client.commands.pre43;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import hu.qgears.sonar.client.commands.AbstractSonarQueryHandler;
import hu.qgears.sonar.client.model.SonarAPI;

/**
 * Parses the SONAR server response as XML message using DOM4j. Subclasses has to process the
 * parsed XML {@link Document}.
 * @author agostoni
 *
 */
public abstract class AbstractXMLSonarQuery extends AbstractSonarQueryHandler {
	


	private DocumentBuilderFactory factory;

	public AbstractXMLSonarQuery(String sonarBaseURL) {
		super(sonarBaseURL);
		factory = DocumentBuilderFactory.newInstance();
		api = SonarAPI.PRE_4_3;
	}
	
	@Override
	protected String processUrl(String address) throws Exception {
		Document xmlRespose = read(address);
		return processSonarResponse(xmlRespose);
	};
	
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
		System.out.println("Execute SONAR REST query: "+urlS);
		URL url = new URL(urlS);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = url.openStream();
		return builder.parse(is);
	}
	
	@Override
	protected void addQueryParameters(Map<String, String> qParams) {
		qParams.put("format", "xml");
	}
}
