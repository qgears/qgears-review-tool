package hu.qgears.sonar.client.commands.post67;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hu.qgears.sonar.client.model.SonarResource;

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
public class SonarMetricsHandler67 extends AbstractSonarJSONQueryHandler{

	public static final String KEY = "metrics";
	private Logger logger = Logger.getLogger(getClass().getName());
	private String filter;
	public SonarMetricsHandler67(String sonarBaseUrl) {
		super(sonarBaseUrl);
	}

	@Override
	protected String processSonarResponse(JsonObject jsonRespose) {
		String ans;
		
		JsonArray jarray = jsonRespose.getAsJsonArray("metrics");
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < jarray.size();i++){
			JsonObject e = jarray.get(i).getAsJsonObject();
			String name = readStringfield(e,"name");
			if (filter == null || name.contains(filter)){
				String desc = readStringfield(e,"description"); 
				String key = readStringfield(e,"key"); 
				bld.append(name).append(" [").append(key).append("]").append(" : ").append(desc).append("\n");
			}
		}
		ans = bld.toString();
		return ans;
	}

	@Override
	protected String getServiceName() {
		return "metrics/search";
	}

	@Override
	protected void addQueryParameters(Map<String, String> params) {
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
