package hu.qgears.review.web;

import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;

public class HandleMainPage extends AbstractRender {
	public HandleMainPage(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		renderHeader("QReview");
		rtout.write("<h1>QReview source review tool web server</h1>\n<a href=\"/allannotations\">All annotations</a>\n<h2>Source sets</h2>\n");
		for(ReviewSourceSet rss: instance.getModel().sourcesets.values())
		{
			rtcout.write(formatSourceSet(rss));
			rtout.write("<br/>\n");
		}
			rtout.write("<h2>Source files</h2>\n");
		for(ReviewSource source: instance.getModel().getSources())
		{
			rtout.write("<a href=\"");
			rtcout.write("");
			rtout.write("\">");
			rtcout.write(source.toStringLong());
			rtout.write("</a><br/>\n");
		}
		renderFooter();
		rtout.write("</body></html>\n");
	}
}
