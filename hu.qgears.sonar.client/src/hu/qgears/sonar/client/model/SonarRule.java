package hu.qgears.sonar.client.model;

import java.util.ArrayList;
import java.util.List;

public class SonarRule implements Comparable<SonarRule>{

	private String name; 
	private String ruleId; 
	private String description; 
	private String severity; 
	private List<String> matches = new ArrayList<String>();
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getMatches() {
		return matches;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSeverity() {
		return severity;
	}
	
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	@Override
	public int compareTo(SonarRule o) {
		return ruleId.compareTo(o.ruleId);
	}
	
}
