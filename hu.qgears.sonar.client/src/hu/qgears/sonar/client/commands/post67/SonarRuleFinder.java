package hu.qgears.sonar.client.commands.post67;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import hu.qgears.commons.MultiMap;
import hu.qgears.commons.MultiMapTreeImpl;
import hu.qgears.commons.UtilFile;
import hu.qgears.sonar.client.model.SonarIssue;
import hu.qgears.sonar.client.model.SonarResourceScope;
import hu.qgears.sonar.client.model.SonarRule;

public class SonarRuleFinder extends AbstractSonarJSONQueryHandler {

	public static final String KEY = "rules";
	private String fileName;
	private String language;
	private String resid;
	
	public SonarRuleFinder(String sonarBaseURL) {
		super(sonarBaseURL);
	}

	@Override
	protected void setCommandParameters(List<String> cmdParameters) {
		fileName = null;
		language = null;
		resid = null;
		for (String p :cmdParameters){
			if(p.startsWith("-f")){
				fileName = p.split("=")[1];
			} else if (p.startsWith("-l")){
				language = p.split("=")[1];
			} else if (p.startsWith("-id")){
				resid = p.split("=")[1];
			}
		}
	}

	@Override
	protected String getServiceName() {
		return "rules/search";
	}

	@Override
	protected String processSonarResponse(JsonObject jResponse) {
		String ans = "OK";
		List<SonarRule> results;
		results = loadResources(jResponse);
		if (fileName != null){
			List<String> allLines;
			try {
				allLines = UtilFile.readLines(new File(fileName));
				for (String l :  allLines){
					l = l.trim();
					if (!l.isEmpty()) {
						for (SonarRule sr : results){
							if (sr.getDescription() != null && sr.getDescription().contains(l)){
								sr.getMatches().add(l);
							}
						}
					}
				}
				if (resid == null){
					printAllRules(results);
				} else {
					joinToMatchesWithIssues(allLines,results);
				}
			} catch (Exception e) {
				e.printStackTrace();
				ans = "Error while processing input file "+fileName;
			}
			
		} else {
			ans = results.size() +" rules found.";
		}
		return ans;
	}
	
	class JoinedResult {
		public JoinedResult(String issueId2) {
			issueId = issueId2;
		}

		/* The issue id found in description specified by input file
		 **/
		private String issueId;
		
		/*
		 * Internal sonar rule ids that are related to issueId
		 * */
		private List<SonarRule> sonarInternalRuleIds = new ArrayList<>();
		
		/*
		 * The issues that belong to given sonarInternal rule
		 **/
		private MultiMap<SonarRule, SonarIssue> sonarIssues= new MultiMapTreeImpl<>();

		public void checkIssue(SonarIssue i) {
			for (SonarRule sr : sonarInternalRuleIds){
				if (sr.getRuleId().equals(i.getRuleId())){
					sonarIssues.putSingle(sr, i);
					break;
				}
			}
		}
		
	}

	private void joinToMatchesWithIssues( List<String> allLines, List<SonarRule> results) {
		SonarIssueFinder sif = new SonarIssueFinder(getSonarBaseURL());
		sif.handleCommand(Arrays.asList( "-id="+resid));
		List<SonarIssue> issues = sif.getResults();
		
		List<JoinedResult> joinResults = new ArrayList<>();
		for( String issueId : allLines) {
			issueId = issueId.trim();
			if (!issueId.isEmpty()) {
				JoinedResult jr = new JoinedResult(issueId);
				joinResults.add(jr);
				
				for (SonarRule sr : results){
					if (sr.getMatches().contains(issueId)){
						jr.sonarInternalRuleIds.add(sr);
					}
				}
				Collections.sort(jr.sonarInternalRuleIds);
			}
		}
		
		for (SonarIssue i : issues){
			for (JoinedResult jr : joinResults){
				jr.checkIssue(i);
			}
		}
		StringBuilder bld = new StringBuilder();
		String sep = "ű";
		for (JoinedResult jr : joinResults ){
			bld.append(jr.issueId).append("\n");
			for (SonarRule sr : jr.sonarInternalRuleIds) {
				//appending an empty col
				bld.append(sep)
				.append(sr.getRuleId()).append(sep)
				.append(sr.getName()).append(sep)
				.append(sr.getSeverity()).append(sep)
				.append(jr.sonarIssues.get(sr).size()).append("\n");
			}
		}
		System.out.println(bld);
	}

	private void printAllRules(List<SonarRule> results) {
		for (SonarRule rule : results){
			if (!rule.getMatches().isEmpty()){
				System.out.println(rule.getRuleId() + " " +rule.getMatches());
			}
		}
	}

	@Override
	protected void addQueryParameters(Map<String, String> qParams) {
		super.addQueryParameters(qParams);
		if (language == null){
			qParams.put("languages","java");
		} else  {
			qParams.put("languages",language);
		}
		
	}

	protected List<SonarRule> loadResources(JsonObject jResponse) {
		List<SonarRule> resouceList = new ArrayList<>();
		do {
			JsonArray cs = jResponse.getAsJsonArray("rules");
			for (JsonElement e :cs){
				resouceList.add(readRule(e.getAsJsonObject()));
			}
			jResponse = nextPage();
		} while (jResponse != null);
		return resouceList;
	}

}
