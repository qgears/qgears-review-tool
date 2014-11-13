package hu.qgears.sonar.client.commands;

import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.model.SonarResourceScope;
import hu.qgears.sonar.client.util.ExportToCSVHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

	/**
	 * @param sonarBaseURL
	 */
	public SonarResourceMetricsHandler(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	@Override
	protected String processSonarResponse(Document xmlResponse) {
		String ans = "OK";
		List<SonarResource> resouceList = readResourceListRecursive(xmlResponse);
		if (fileName != null){
			ans = saveToFile(resouceList);
		} else {
			ans = resouceList.size() +" elements read.";
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
				case PRJ: newScope = SonarResourceScope.DIR;
				break;
				}
				if (newScope != null){
					super.setCommandParameters(Arrays.asList("-id="+r.getResurceName(),"-s="+newScope.toString()));
					try {
						Document doc = read(buildQuery(getQueryParameters()));
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
	protected Map<String, String> getQueryParameters() {
		Map<String, String> qp =  super.getQueryParameters();
		if (metrics != null){
			qp.put("metrics", metrics);
		}
		return qp;
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
}
