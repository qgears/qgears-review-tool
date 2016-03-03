package hu.qgears.review.web;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.util.UtilHtml;

public class HandleAllAnnotations extends AbstractRender {
	public HandleAllAnnotations(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		rtout.write("<html><head>\n<title>QReview all annotations</title>\n<style type=\"text/css\"><!--\n");
		rtcout.write(UtilFile.loadAsString(UtilHtml.getStyle()));
		rtout.write("\n-->\n</style>\n</head><body>\n<script>");
		rtcout.write(UtilFile.loadAsString(getClass().getResource("jquery.min.js")));
		rtout.write("\n");
		rtcout.write(UtilFile.loadAsString(getClass().getResource("logic.js")));
		rtout.write("</script>\n<h1>QReview source review tool web server</h1>\n<h2>All annotations</h2>\n");
		for(ReviewEntry entry: instance.getModel().entries)
		{
			rtcout.write(formatAnnotation(entry));
			rtout.write(":<br/><pre>\n");
			rtcout.write(""+entry);
			rtout.write("</pre><br/>\n");
		}
		rtout.write("</body></html>\n");
	}
}
