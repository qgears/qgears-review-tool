package hu.qgears.sonar.client.commands.post67;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import hu.qgears.sonar.client.commands.AbstractSonarQueryHandler;
import hu.qgears.sonar.client.model.ResourceMetric;
import hu.qgears.sonar.client.model.SonarAPI;
import hu.qgears.sonar.client.model.SonarIssue;
import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.model.SonarResourceScope;
import hu.qgears.sonar.client.model.SonarRule;

/**
 /**
 * Parses the SONAR server response as XML message using GSON. Subclasses has to process the
 * parsed JSON message {@link JsonObject}.
 * 
 * @author agostoni
 */
public abstract class AbstractSonarJSONQueryHandler  extends AbstractSonarQueryHandler {

	private boolean hasNextPage;
	private int pageIndex;

	public AbstractSonarJSONQueryHandler(String sonarBaseURL) {
		super(sonarBaseURL);
		api = SonarAPI.POST_6_7;
	}
	
	@Override
	public String handleCommand(List<String> cmdParameters) {
		hasNextPage = false;
		pageIndex = 0;
		return super.handleCommand(cmdParameters);
	}

	
	protected String readStringfield(JsonObject e, String field) {
		JsonElement f = e.get(field);
		if (f != null){
			return f.getAsString();
		}
		return null;
	}
	protected int readIntfield(JsonObject e, String field) {
		JsonElement f = e.get(field);
		if (f != null){
			return f.getAsInt();
		}
		return -1;
	}
	
	@Override
	protected String processUrl(String address) throws Exception {
		JsonObject a = read(address);
		return processSonarResponse(a);
	}
	
	protected JsonObject nextPage() {
		if (hasNextPage){
			pageIndex++;
			Map<String,String> p = new HashMap<>();
			addQueryParameters(p);
			try {
				return read(buildQuery(p));
			} catch (Exception e) {
				throw new RuntimeException("Cannot fetch next page",e);
			}
		}
		return null;
	}
	
	protected abstract String processSonarResponse(JsonObject jsonRespose);
	
	/**
	 * Reads data from specified URL, and parses the answer as an XML document.
	 * 
	 * @param urlS
	 * @return
	 * @throws Exception
	 */
	protected JsonObject read(String urlS) throws Exception{
		System.out.println("Execute SONAR REST query: "+urlS);
		hasNextPage = false;
		URL url = new URL(urlS);
		
		try (InputStream is = url.openStream()){
			JsonElement jelement = new JsonParser().parse(new InputStreamReader(is,Charset.forName("UTF-8")));
			JsonObject  jobject = jelement.getAsJsonObject();
			JsonElement pages = jobject.get("paging");
			int total;
			int pSize;
			if (pages != null){
				//some API-s put page info into a dedicated paging subobject
				JsonObject p = pages.getAsJsonObject();
				total = readIntfield(p, "total");
				pageIndex = readIntfield(p, "pageIndex");
				pSize = readIntfield(p, "pageSize");
			} else {
				//some API-s put page info directly into root
				total = readIntfield(jobject, "total");
				pageIndex = readIntfield(jobject, "p");
				pSize = readIntfield(jobject, "ps");
			}
			if ((total > -1) &&  (total > pageIndex * pSize)){
				hasNextPage = true;
			}
			return jobject;
		}
	}

	public SonarResource readFromJson(JsonObject e) {
		SonarResource newI = new SonarResource(api);
		newI.setResourceId(readStringfield(e, "id"));
		newI.setResurceName(readStringfield(e, "key"));
		newI.setScope(SonarResourceScope.valueOf(readStringfield(e, "qualifier")));
		JsonElement m = e.get("measures");
		if (m != null) {
			for (JsonElement j : m.getAsJsonArray()){
				ResourceMetric rm = readMetric(j.getAsJsonObject());
				newI.getMetrics().add(rm);
			}
		}
		return newI;
	}


	protected ResourceMetric readMetric(JsonObject m) {
		String key = readStringfield(m, "metric");
		String val = readStringfield(m, "value");
		ResourceMetric rm = new ResourceMetric();
		rm.setMetricKey(key);
		rm.setValue(val);
		rm.setFormattedValue(val);
		return rm;
	}
	protected SonarIssue readIssue(JsonObject m) {
		String ruleId = readStringfield(m, "rule");
		String sev = readStringfield(m, "severity");
		String comp = readStringfield(m, "component");
		int line = readIntfield(m, "line");
		SonarIssue rm = new SonarIssue();
		rm.setRuleId(ruleId);
		rm.setSeverity(sev);
		rm.setComponentKey(comp);
		rm.setLine(line);
		return rm;
	}
	protected SonarRule readRule(JsonObject m) {
		String ruleId = readStringfield(m, "key");
		String name = readStringfield(m, "name");
		String description = readStringfield(m, "htmlDesc");
		String sev = readStringfield(m, "severity");
		SonarRule rm = new SonarRule();
		rm.setRuleId(ruleId);
		rm.setName(name);
		rm.setDescription(description);
		rm.setSeverity(sev);
		return rm;
	}

	@Override
	protected void addQueryParameters(Map<String, String> qParams) {
		qParams.put("ps", "500");
		if (pageIndex > 0){
			qParams.put("p", String.valueOf(pageIndex));
		}
	}
	
}
