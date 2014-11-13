package hu.qgears.sonar.client.commands;

import hu.qgears.sonar.client.model.SonarResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This handler is able to list resources (projects, packages, files ...) from sonar.
 * <h4>Usage</h4>
 * <code>res -id='resourceid' -s='scope'</code>
 * <p>
 * <li>If no parameter specified all SONAR top project will be listed.
 * <li>-id : Query child resources of specified resource
 * <li>-s : the scope PRJ -> only projects will be listed, DIR -> only directories, FIL -> only files  
 * @author agostoni
 *
 */
public class SonarResourceHandler extends AbstractSonarQueryHandler{

	private String resourceId;
	private String scopes;
	
	public SonarResourceHandler(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	public static final String KEY="res";
	
	
	@Override
	protected String processSonarResponse(Document xmlResponse) {
		StringBuilder bld = new StringBuilder();
		for (SonarResource r : getSonarResourcesFromXML(xmlResponse)){
			bld.append(r.getResurceName()).append(" [").append(r.getScope()).append("]\n");
		}
		return bld.toString();
	}
	
	protected List<SonarResource> getSonarResourcesFromXML(Document xmlResponse){
		NodeList nodes = xmlResponse.getElementsByTagName("resource");
		List<SonarResource> res = new ArrayList<SonarResource>();
		for (int i = 0; i< nodes.getLength();i++){
			Element e = (Element) nodes.item(i);
			SonarResource r = SonarResource.readFromXml(e);
			res.add(r);
		}
		return res;
	}

	@Override
	protected Map<String, String> getQueryParameters() {
		Map<String,String> qp = new HashMap<String, String>();
		qp.put("depth", "-1");
		if (resourceId != null){
			qp.put("resource", resourceId);
		}
		if (scopes != null){
			qp.put("scopes", scopes);
		}
		return qp;
	}

	@Override
	protected String getServiceName() {
		return "resources";
	}

	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
		resourceId = null;
		scopes = null;
		for (String p :cmdParameters){
			if(p.startsWith("-id")){
				resourceId = p.split("=")[1];
			}else if (p.startsWith("-s")){
				scopes = p.split("=")[1];
			}
		}
	}

}
