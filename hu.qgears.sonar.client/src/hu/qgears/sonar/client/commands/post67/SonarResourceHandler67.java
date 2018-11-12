package hu.qgears.sonar.client.commands.post67;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hu.qgears.sonar.client.model.SonarResource;

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
public class SonarResourceHandler67 extends AbstractSonarJSONQueryHandler{

	private String resourceId;
	private String qualifiers;
	
	public SonarResourceHandler67(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	public static final String KEY="res";
	
	
	@Override
	protected String processSonarResponse(JsonObject jsonResponse) {
		
		StringBuilder bld = new StringBuilder();
		for (SonarResource r : getSonarResourcesFromXML(jsonResponse)){
			bld.append(r.getResurceName()).append(" [").append(r.getScope()).append("]\n");
		}
		return bld.toString();
	}
	
	protected List<SonarResource> getSonarResourcesFromXML(JsonObject jsonResponse) {
		List<SonarResource> res = new ArrayList<SonarResource>();
		do {
			JsonArray nodes = jsonResponse.getAsJsonArray("components");
			if (nodes != null) {
				for (int i = 0; i< nodes.size();i++){
					JsonObject e = nodes.get(i).getAsJsonObject();
					SonarResource r = readFromJson(e);
					res.add(r);
				}
			}
			jsonResponse = nextPage();
		} while (jsonResponse != null);
		return res;
	}

	
	
	
	@Override
	protected void addQueryParameters(Map<String,String> qp) {
		super.addQueryParameters(qp);
		if (resourceId != null){
			qp.put("q", resourceId);
		}
		if (qualifiers != null){
			qp.put("qualifiers", qualifiers);
		}
	}

	@Override
	protected String getServiceName() {
		return "components/search";
	}

	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
//		resourceId = null;
		qualifiers = "TRK";
		for (String p :cmdParameters){
			if(p.startsWith("-id")){
				resourceId = p.split("=")[1];
			}else 
				if (p.startsWith("-s")){
				qualifiers = p.split("=")[1];
			}
		}
	}

}
