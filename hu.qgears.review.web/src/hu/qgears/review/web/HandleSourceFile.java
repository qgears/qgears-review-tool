package hu.qgears.review.web;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.model.ReviewInstance;

import java.io.File;

public class HandleSourceFile extends AbstractRender {

	public HandleSourceFile(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		String url=query.getAfterModule();
		File f=instance.getModel().getFile(url);
		String content=UtilFile.loadAsString(f);
		renderHeader(""+f.getName());
		renderFileLinks(query.getAfterModule());
		rtout.write("<h2><a href=\"/");
		rtcout.write(HandleMarkReviewed.prefix);
		rtout.write("/");
		rtcout.write(url);
		rtout.write("\">Mark as reviewed form</a></h2>\n<h2>Content</h2>\n<pre>\n");
		writeHtmlExcaped(content);
		rtout.write("</pre>\n");
		renderFooter();
	}
}
