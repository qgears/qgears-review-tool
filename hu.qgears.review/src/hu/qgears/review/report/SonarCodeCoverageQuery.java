package hu.qgears.review.report;

import hu.qgears.commons.UtilComma;
import hu.qgears.sonar.client.commands.SonarResourceMetricsHandler;
import hu.qgears.sonar.client.model.SonarResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * Utility for querying data from SONAR. See {@link #loadResorucesFromSonar()}.
 * <p>
 * Instances can be obtained via {@link #getInstance(String, String, List)}.
 * 
 * @author agostoni
 *
 */
public class SonarCodeCoverageQuery extends SonarResourceMetricsHandler{

	private List<SonarResource> results;
	private final String sonarProjectId;
	private final List<String> requiredMetrics;

	private SonarCodeCoverageQuery(String sonarBaseURL,String sonarProjectId,List<String> requiredMetrics) {
		super(sonarBaseURL);
		this.sonarProjectId = sonarProjectId;
		this.requiredMetrics = requiredMetrics;
	}

	@Override
	protected String processSonarResponse(Document xmlResponse) {
		results = readResourceListRecursive(xmlResponse);
		return "Query finished";
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
				if (results == null){
					handleCommand(
							Arrays.asList(
									"-m="+getMetricsAsString(),
									"-r",//recursive
									"-id="+getSonarProjectId(),
									"-s=PRJ"));
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
			String sonarProjectId, List<String> requiredMetrics) {
		String key = sonarBaseURL + "###" + sonarProjectId + "###"
				+ requiredMetrics.toString();
		if (!cache.containsKey(key)) {

			cache.put(key, new SonarCodeCoverageQuery(sonarBaseURL, sonarProjectId,
					requiredMetrics));
		}
		return cache.get(key);
	}
	
}
