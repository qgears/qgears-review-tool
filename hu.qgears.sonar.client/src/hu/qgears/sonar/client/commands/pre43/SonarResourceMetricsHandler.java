package hu.qgears.sonar.client.commands.pre43;

import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.model.SonarResourceScope;
import hu.qgears.sonar.client.util.ExportToCSVHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Extends {@link SonarResourceHandler}, queries also metrics to resources.
 * <h4>Usage</h4>
 * mres -f='fileName' -r -m='metrics' (-id='resource' -s='scope')
 * <li>See {@link SonarResourceHandler} for -id and -s
 * <li>-f : A file path, if specified the results will be written into this file in CSV format.
 * <li>-r : if present the query is recursively executed to child resources
 * <li>-m : comma separated list of the metrics to query. See {@link SonarMetricsHandler} for list of valid values. 
 * @author agostoni
 *
 */
public class SonarResourceMetricsHandler extends SonarResourceHandler {

	public static final String KEY = "resm";
	
	private String metrics;
	private boolean recursive;
	private String fileName;

	private List<SonarResource> results;

	/**
	 * @param sonarBaseURL
	 */
	public SonarResourceMetricsHandler(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	@Override
	protected String processSonarResponse(Document xmlResponse) {
		String ans = "OK";
		results = readResourceListRecursive(xmlResponse);
		if (fileName != null){
			ans = saveToFile(results);
		} else {
			ans = results.size() +" elements read.";
		}
		return ans;
	}

	protected List<SonarResource> readResourceListRecursive(Document xmlResponse) {
		List<SonarResource> resouceList = getSonarResourcesFromXML(xmlResponse);
		if (recursive){
			List<SonarResource> todoList = new ArrayList<SonarResource>(resouceList);
			while (!todoList.isEmpty()){
				SonarResource r = todoList.remove(0);
				SonarResourceScope newScope = null;
				switch (r.getScope()){
				case DIR: newScope = SonarResourceScope.FIL;
				break;
				case FIL: newScope = null;
				break;
				case TRK: newScope = SonarResourceScope.DIR;
				break;
				case BRC:
				case UTS:
					//BRC and UTS not supported in old APIS
				default:
					break;
				}
				if (newScope != null){
					super.setCommandParameters(Arrays.asList("-id="+r.getResurceName(),"-s="+newScope.scopeName(api)));
					try {
						Map<String,String> qp = new HashMap<>();
						addQueryParameters(qp);
						Document doc = read(buildQuery(qp));
						List<SonarResource> childRes = getSonarResourcesFromXML(doc);
						r.getContainedResources().addAll(childRes);
						todoList.addAll(childRes);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
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
	protected void addQueryParameters(Map<String, String> qp) {
		super.addQueryParameters(qp);
		if (metrics != null){
			qp.put("metrics", metrics);
		}
	}
	
	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
		super.setCommandParameters(cmdParameters);
		metrics = null;
		recursive = false;
		fileName = null;
		for (String p :cmdParameters){
			if ("-r".equals(p)){
				recursive = true;
			}else if(p.startsWith("-f")){
				fileName = p.split("=")[1];
			}else if (p.startsWith("-m")){
				metrics = p.split("=")[1];
			}
		}
	}

	public List<SonarResource> getResults() {
		return results;
	}
}
