package hu.qgears.review.report;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.qgears.commons.UtilComma;
import hu.qgears.sonar.client.commands.post67.SonarResourceMetricsHandler67;
import hu.qgears.sonar.client.commands.pre43.SonarResourceMetricsHandler;
import hu.qgears.sonar.client.model.SonarAPI;
import hu.qgears.sonar.client.model.SonarResource;

/**
 * Utility for querying data from SONAR. See {@link #loadResorucesFromSonar()}.
 * <p>
 * Instances can be obtained via {@link #getInstance(String, String, List)}.
 * 
 * @author agostoni
 *
 */
public class SonarCodeCoverageQuery {

	private List<SonarResource> results;
	private final String sonarProjectId;
	private final List<String> requiredMetrics;
	private SonarAPI api;
	private String sonarBaseURL;

	private SonarCodeCoverageQuery(String sonarBaseURL,String sonarProjectId,List<String> requiredMetrics,SonarAPI api) {
		this.sonarBaseURL = sonarBaseURL;
		this.sonarProjectId = sonarProjectId;
		this.requiredMetrics = requiredMetrics;
		this.api = api;
	}



	/**
	 * Executes SONAR query and returns the results as a List of
	 * {@link SonarResource}. The required metrics and its values can be
	 * accessed by {@link SonarResource#getMetric(String)}.
	 * <p>
	 * Expensive function due to lot of network-based communication with SONAR.
	 * A cache mechanism is implemented so the first call may run
	 * significantly longer, than further calls.
	 * 
	 * 
	 * @return
	 */
	public List<SonarResource> loadResorucesFromSonar(){
		if (results == null)
		{	
			synchronized (this) {
				if (results == null) {
					List<String> params = Arrays.asList(
							"-m="+getMetricsAsString(),
							"-r",//recursive
							"-id="+getSonarProjectId());
					switch (api) {
					case POST_6_7:
						SonarResourceMetricsHandler67 h = new SonarResourceMetricsHandler67(sonarBaseURL);
						h.handleCommand(params);
						results = h.getResults();
						break;
					case PRE_4_3:
						SonarResourceMetricsHandler h2 = new SonarResourceMetricsHandler(sonarBaseURL);
						h2.handleCommand(params);
						results = h2.getResults();
						break;
					default:
						break;
					}
				}
			}
		}
		return results;
	}

	/**
	 * Clears the cache, next call of {@link #loadResorucesFromSonar()} will
	 * regenerate automatically.
	 */
	public void reset(){
		results = null;
	}
	
	/**
	 * Concatenates the required SONAR metric identifiers separated by a comma.
	 * 
	 * @return
	 */
	private String getMetricsAsString() {
		UtilComma c = new UtilComma(",");
		StringBuilder sb = new StringBuilder();
		for (String m : requiredMetrics){
			sb.append(c.getSeparator()).append(m);
		}
		return sb.toString();
	}

	private String getSonarProjectId() {
		return sonarProjectId;
	}
	
	private static Map<String, SonarCodeCoverageQuery> cache = new HashMap<String, SonarCodeCoverageQuery>();

	/**
	 * Returns an instance of {@link SonarCodeCoverageQuery} for specified
	 * parameters. If an instance already exist with these parameters, than it
	 * will be returned (in most cases with preloaded data). Otherwise a new
	 * instance will be returned, that will be initialized on first
	 * {@link #loadResorucesFromSonar()} call.
	 * 
	 * @param sonarBaseURL
	 * @param sonarProjectId
	 * @param requiredMetrics
	 * @return
	 */
	public static SonarCodeCoverageQuery getInstance(String sonarBaseURL,
			String sonarProjectId, List<String> requiredMetrics,SonarAPI api) {
		String key = sonarBaseURL + "###" + sonarProjectId + "###"
				+ requiredMetrics.toString();
		if (!cache.containsKey(key)) {

			cache.put(key, new SonarCodeCoverageQuery(sonarBaseURL, sonarProjectId,
					requiredMetrics,api));
		}
		return cache.get(key);
	}
	
}
