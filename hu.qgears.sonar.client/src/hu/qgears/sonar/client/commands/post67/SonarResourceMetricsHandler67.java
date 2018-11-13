package hu.qgears.sonar.client.commands.post67;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.util.ExportToCSVHelper;

/**
 * Extends {@link SonarResourceHandler67}, queries also metrics to resources.
 * <h4>Usage</h4>
 * mres -f='fileName' -r -m='metrics' (-id='resource' -s='scope')
 * <li>See {@link SonarResourceHandler67} for -id and -s
 * <li>-f : A file path, if specified the results will be written into this file in CSV format.
 * <li>-r : if present the query is recursively executed to child resources
 * <li>-m : comma separated list of the metrics to query. See {@link SonarMetricsHandler67} for list of valid values. 
 * @author agostoni
 *
 */
public class SonarResourceMetricsHandler67 extends AbstractSonarJSONQueryHandler {

	public static final String KEY = "resm";
	
	private String metrics;
	private boolean recursive;
	private String fileName;

	private String id;

	private String qualifiers;

	private List<SonarResource> results;

	/**
	 * @param sonarBaseURL
	 */
	public SonarResourceMetricsHandler67(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	@Override
	protected String processSonarResponse(JsonObject jResponse) {
		String ans = "OK";
		results = loadResources(jResponse);
		if (fileName != null){
			ans = saveToFile(results);
		} else {
			ans = results.size() +" elements read.";
		}
		return ans;
	}

	protected List<SonarResource> loadResources(JsonObject jResponse) {
		List<SonarResource> resouceList = new ArrayList<>();
		do {
			JsonArray cs = jResponse.getAsJsonArray("components");
			for (JsonElement e :cs){
				resouceList.add(readFromJson(e.getAsJsonObject()));
			}
			jResponse = nextPage();
		} while (jResponse != null);
		return resouceList;
	}

	private String saveToFile(List<SonarResource> resouceList) {
		String ans;
		try {
			ExportToCSVHelper.saveToCSV(new File(fileName), resouceList);
			ans = "Results saved into file "+fileName;
		} catch (IOException e) {
			ans = "Exception during file save "+e.getMessage();
		}
		return ans;
	}

	@Override
	protected void addQueryParameters(Map<String,String> qp) {
		super.addQueryParameters(qp);
		if (id != null){
			qp.put("component", id);;
		}
		if (metrics != null){
			qp.put("metricKeys", metrics);
		}
		if (recursive){
			qp.put("strategy","all");
		} else {
			qp.put("strategy","children");
		}
		if (qualifiers != null){
			qp.put("qualifiers",qualifiers);
		}
	}

	
	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
		metrics = null;
		recursive = false;
		fileName = null;
		id = null;
		qualifiers = "TRK,FIL";
		for (String p :cmdParameters){
			if ("-r".equals(p)){
				recursive = true;
			}else if(p.startsWith("-f")){
				fileName = p.split("=")[1];
			}else if (p.startsWith("-m")){
				metrics = p.split("=")[1];
			} else if (p.startsWith("-id")){
				id = p.split("=")[1];
			} else if (p.startsWith("-s")){
				qualifiers = p.split("=")[1];
			}
		}
	}

	@Override
	protected String getServiceName() {
		return "measures/component_tree";
	}
	
	public List<SonarResource> getResults() {
		return results;
	}
}
