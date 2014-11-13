package hu.qgears.review.web;

import hu.qgears.review.model.ReviewInstance;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.AbstractHandler;

public class WebHandler extends AbstractHandler {
	private ReviewInstance instance;
	public WebHandler(ReviewInstance instance) {
		this.instance=instance;
	}

	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		WebQuery query=new WebQuery(target, request, response, dispatch);
		String module=query.getModule();
		try {
			AbstractRender r;
			if("source".equals(module))
			{
				r=new HandleSourceFile(query, instance);
			}else if(HandleSourceSet.prefix.equals(module))
			{
				r=new HandleSourceSet(query, instance);
			}else if(HandleMarkReviewed.prefix.equals(module))
			{
				r=new HandleMarkReviewed(query, instance);
			}else if(HandleAnnotation.prefix.equals(module))
			{
				r=new HandleAnnotation(query, instance);
			}
			else if("allannotations".equals(module))
			{
				r=new HandleAllAnnotations(query, instance);
			} else if (HandleReport.URL_PREXIX.equals(module)){
				r = new HandleReport(query, instance);
			}
			else
			{
				r=new HandleMainPage(query, instance);
			}
			r.doHttpReply();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

