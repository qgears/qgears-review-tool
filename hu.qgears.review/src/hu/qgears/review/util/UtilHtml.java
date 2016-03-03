package hu.qgears.review.util;

import java.net.URL;

public final class UtilHtml {

	private static final String linkTemplate = "<a href=\"%s\">%s</a>";
	private static final String STYLE_CSS = "style.css";
	
	public static final String Q_PARAM_ORDER_ASC = "asc";
	public static final String Q_PARAM_ORDER_BY = "orderBy";
	
	private UtilHtml() {
		// disable constructor of utility class
	}
	
	/**
	 * Creates a link (<code>&lt;a></code>) with specified target URL and content.
	 */
	public static String link(String targetURL, String content){
		return String.format(linkTemplate, targetURL,content);
	}

	public static URL getStyle() {
		return UtilHtml.class.getResource(STYLE_CSS);
	}
}
