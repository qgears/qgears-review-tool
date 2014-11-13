package hu.qgears.sonar.client.model;

/**
 * The possible scopes (or types) of SONAR resources. See comments on literals.
 * 
 * @author agostoni
 *
 */
public enum SonarResourceScope {
	/**
	 * Project scope (for instance an eclipse project)
	 */
	PRJ,
	/**
	 * Directory (package) scope (for instance a JAva package).
	 */
	DIR,
	/**
	 * File (java) scope (For instance a Java class).
	 */
	FIL
}
