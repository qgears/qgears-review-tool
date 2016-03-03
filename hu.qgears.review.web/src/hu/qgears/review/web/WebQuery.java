package hu.qgears.review.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebQuery {
	public String target;
	public HttpServletRequest request;
	public HttpServletResponse response;
	private String module;
	private String afterModule;

	public WebQuery(String target, HttpServletRequest request,
			HttpServletResponse response) {
		super();
		this.target = target;
		this.request = request;
		this.response = response;
		String pathInfo = request.getPathInfo();
		if (pathInfo.startsWith("/")) {
			int idx = pathInfo.indexOf("/", 1);
			if (idx > 0) {
				module = pathInfo.substring(1, idx);
				afterModule = pathInfo.substring(idx + 1);
			} else {
				module = pathInfo.substring(1);
			}
		}
	}

	public String getModule() {
		return module;
	}

	public String getAfterModule() {
		return afterModule;
	}
}
