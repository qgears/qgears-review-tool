package hu.qgears.sonar.client.commands.post67;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import hu.qgears.sonar.client.model.SonarIssue;
import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.util.ExportToCSVHelper;

/**
 * Queries issues of a given resource from SONAR.
 * 
 * Params:
 * -id : the resource id to query (requied)
 * -f : save output to file (optional)
 * -g : enable grouping option in file save (optional)
 * @author agostoni
 *
 */
public class SonarIssueFinder extends AbstractSonarJSONQueryHandler {

	public static final String KEY = "issues";
	private String fileName;
	private String id;
	private boolean group;
	private List<SonarIssue> results;
	
	public SonarIssueFinder(String sonarBaseURL) {
		super(sonarBaseURL);
	}


	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
		id = null;
		for (String p :cmdParameters){
			if(p.startsWith("-f")){
				fileName = p.split("=")[1];
			} else if (p.startsWith("-id")){
				id = p.split("=")[1];
			} else if (p.startsWith("-g")){
				group = true;
			}
		}
		if (id == null){
			throw new RuntimeException("Resource id is missing");
		}
	}

	@Override
	protected void addQueryParameters(Map<String,String> qp) {
		super.addQueryParameters(qp);
		if (id != null){
			qp.put("componentKeys", id);
		}
	}
	
	@Override
	protected String getServiceName() {
		return "issues/search";
	}

	@Override
	protected String processSonarResponse(JsonObject jResponse) {
		String ans = "OK";
		results = loadResources(jResponse);
		if (fileName != null){
			ans = saveToFile(results);
		} else {
			ans = results.size() +" issues found.";
		}
		return ans;
	}

	private String saveToFile(List<SonarIssue> results) {
		try {
			ExportToCSVHelper.saveIssuesToCSV(new File(fileName), results, group);
			return "" + results.size()+ " issues exproted into "+fileName;
		} catch (Exception e){
			e.printStackTrace();
			return "Error happened during saving results to "+fileName;
		}
	}


	protected List<SonarIssue> loadResources(JsonObject jResponse) {
		List<SonarIssue> resouceList = new ArrayList<>();
		do {
			JsonArray cs = jResponse.getAsJsonArray("issues");
			for (JsonElement e :cs){
				resouceList.add(readIssue(e.getAsJsonObject()));
			}
			jResponse = nextPage();
		} while (jResponse != null);
		return resouceList;
	}

	public List<SonarIssue> getResults() {
		return results;
	}
}
