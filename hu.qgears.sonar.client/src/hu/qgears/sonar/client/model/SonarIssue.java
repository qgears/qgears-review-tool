package hu.qgears.sonar.client.model;

public class SonarIssue {

	private String ruleId;
	private String severity;
	private String componentKey;
	private int line;
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getComponentKey() {
		return componentKey;
	}
	public void setComponentKey(String componentKey) {
		this.componentKey = componentKey;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	
	
}
