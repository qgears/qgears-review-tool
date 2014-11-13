package hu.qgears.sonar.client.commands;

import hu.qgears.sonar.client.util.DomHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This handler is able to query all metrics that is known by SONAR.
 * <h4>Usage</h4>
 * <code>metrics filter</code>
 * <p>
 * filter : if specified, only those metrics will be listed that match filter.
 * 
 * 
 * @author agostoni
 *
 */
public class SonarMetricsHandler extends AbstractSonarQueryHandler{

	public static final String KEY = "metrics";
	private Logger logger = Logger.getLogger(getClass().getName());
	private String filter;
	public SonarMetricsHandler(String sonarBaseUrl) {
		super(sonarBaseUrl);
	}

	@Override
	protected String processSonarResponse(Document xmlRespose) {
		String ans;
		NodeList nodes = xmlRespose.getElementsByTagName("metric");
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < nodes.getLength();i++){
			Element e = (Element) nodes.item(i);
			String name = DomHelper.getChildElementByTagName(e,"name").getTextContent();
			if (filter == null || name.contains(filter)){
				String desc = DomHelper.getChildElementByTagName(e,"description").getTextContent();
				Element key = DomHelper.getChildElementByTagName(e,"key");
				bld.append(name).append(" [").append(key.getTextContent()).append("]").append(" : ").append(desc).append("\n");
			}
		}
		ans = bld.toString();
		return ans;
	}

	@Override
	protected String getServiceName() {
		return KEY;
	}

	@Override
	protected Map<String, String> getQueryParameters() {
		return Collections.emptyMap();
	}

	@Override
	protected void setCommandParameters(List<String> parameters) {
		filter = null;
		for (int i = 0; i<parameters.size();i++){
			String param = parameters.get(i);
			switch (i) {
			case 0:
				filter = param;
				break;
			default:
				logger.log(Level.SEVERE,"Unkown parameter : "+param);
				break;
			}
		}
	}
}
