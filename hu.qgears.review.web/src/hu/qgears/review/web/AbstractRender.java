package hu.qgears.review.web;

import hu.qgears.commons.EscapeString;
import hu.qgears.commons.UtilFile;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.util.UtilHtml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

abstract public class AbstractRender {
	protected WebQuery query;
	protected ReviewInstance instance;
	protected PrintWriter out, rtout, rtcout;
	public AbstractRender(WebQuery query, ReviewInstance instance) {
		this.query=query;
		this.instance=instance;
	}
	final public void doHttpReply() throws Exception
	{
		String ctype=getContentType();
		query.response.setContentType(ctype);
		query.response.setCharacterEncoding("utf-8");
		query.response.setStatus(HttpServletResponse.SC_OK);

		PrintWriter wri=query.response.getWriter();
		out=rtout=rtcout=wri;
		try
		{
			render();
		}finally
		{
			wri.close();
		}
	}
	protected String getContentType() {
		return "text/html";
	}
	abstract public void render() throws Exception;
	protected void writeHtmlExcaped(String content) {
		String escaped=EscapeString.escapeHtml(content);
		out.write(escaped);
	}
	/**
	 * 	 * Helper method to render a standard footer.
	 */
	protected void renderFooter() {
		rtout.write("</body></html>\n");
	}


	/**
	 * Helper method to render a standard header.
	 * 
	 * @param title
	 *            The page title
	 * @throws IOException
	 */
	protected void renderHeader(String title) throws IOException {
		rtout.write("<html><head>\n<title>");
		rtcout.write(title);
		rtout.write("</title>\n<style type=\"text/css\"><!--\n");
		rtcout.write(UtilFile.loadAsString(UtilHtml.getStyle()));
		rtout.write("\n-->\n</style>\n</head><body>\n<script>");
		rtcout.write(UtilFile.loadAsString(getClass().getResource("jquery.min.js")));
		rtout.write("</script>\n<script>");
		rtcout.write(UtilFile.loadAsString(getClass().getResource("logic.js")));
		rtout.write("</script>\n");
	}
	/**
	 * Render links for a file:
	 *  source page
	 *  all annotations
	 * @param fileUrl
	 */
	protected void renderFileLinks(String fileUrl) {
		ReviewSource rs=instance.getModel().getSource(fileUrl);
		String simpleName=rs.getSimpleName();
		File f=instance.getModel().getFile(fileUrl);
		String filesystemPath=f.getAbsolutePath();
		rtout.write("<h2><a href=\"/source/");
		rtcout.write(fileUrl);
		rtout.write("\">");
		rtcout.write(simpleName);
		rtout.write("</a></h2>\nLink to file: <a href=\"/source/");
		rtcout.write(fileUrl);
		rtout.write("\">");
		rtcout.write(fileUrl);
		rtout.write("</a><br/>\nFilesystem path: ");
		rtcout.write(filesystemPath);
		rtout.write(" <br/>\nSVN link: ");
		rtcout.write(rs.getSourceFolderUrl());
		rtout.write("<br/>\nRevision: ");
		rtcout.write(rs.getFileVersion());
		rtout.write("<br/>\nSVN workspace revision: ");
		rtcout.write(rs.getFolderVersion());
		rtout.write("<br/>\n<h2>Container source sets</h2>\n");
		for(ReviewSourceSet rss: instance.getModel().getContainingSourceSets(rs.modelUrl()))
		{
			rtcout.write(formatSourceSet(rss));
		}
		rtout.write("<h2>Reviews</h2>\n");
		for(ReviewEntry e:rs.getMatchingReviewEntries(instance.getModel()))
		{
			rtout.write(" ");
			rtcout.write(formatAnnotation(e));
			rtout.write(" <br/>\n");
		}
		rtout.write("<h2>Reviews of older versions</h2>\n");
		for(ReviewEntry e:rs.getMatchingReviewEntriesPreviousVersion(instance.getModel()))
		{
			rtout.write(" PREV! ");
			rtcout.write(formatAnnotation(e));
			rtout.write(" <br/>\n");
		}
	}

	/**
	 * System.currentTimeMillis to user format of date
	 * @param date
	 * @return
	 */
	protected String formatDate(long date) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy.MM.dd HH:mm");
		return ""+sdf.format(new Date(date));
	}
	protected String formatAnnotation(ReviewEntry e) {
		String comment=e.getComment();
		int l=comment.length();
		int firstChars=200;
		String commentFistchars=comment.substring(0, Math.min(firstChars, l))+(l>firstChars?"...":"");
		String ret="<a href='"+"/"+HandleAnnotation.prefix+"/"+e.getSha1Sum()+
			"' title='"+EscapeString.escapeHtml(comment)+"'>"+e.getUser()+": "+e.getAnnotation()+" at "+formatDate(e.getDate())+
			" </a> '"+commentFistchars+"'\n";
		return ret;
	}

	/**
	 * Creates links for details page of specified {@link ReviewSourceSet}.
	 * 
	 * @param rss
	 * @return
	 */
	protected String formatSourceSet(ReviewSourceSet rss) {
		String l1 = UtilHtml.link("/"+HandleSourceSet.prefix+"/"+rss.id, rss.id);
		String l2 = UtilHtml.link("/"+HandleReport.URL_PREXIX+"/"+rss.id,"report");
		return l1 + " [" +l2+"] ";
	}
	
	
	protected String formatPercentage(float val){
		return String.format("%05.2f %%", val);
	}
	
}
