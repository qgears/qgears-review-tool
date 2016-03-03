package hu.qgears.review.web;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.UtilSha1;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HandleMarkReviewed extends AbstractRender {

	public static final String prefix="markReviewed";
	private static final String invalidatePrefix="invalidate_";

	public HandleMarkReviewed(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		String method=query.request.getMethod();
		renderHeader("Mark reviewed");
		if("POST".equals(method))
		{
			createAnnotation();
			renderFileLinks(query.getAfterModule());
		}else
		{
			renderNormal();
			renderFileLinks(query.getAfterModule());
		}
		renderFooter();
	}



	private void createAnnotation() throws IOException {
		try {
			String url=query.getAfterModule();
			ReviewSource rs=instance.getModel().getSource(url);
			EReviewAnnotation annot= null;
			String annotString = query.request.getParameter("annotation");
			for (EReviewAnnotation a :EReviewAnnotation.values()){
				if (a.toString().equals(annotString)){
					annot = a;
				}
			}
			String user=query.request.getParameter("user");
			String comment=query.request.getParameter("comment");
			String sha1=query.request.getParameter("sha1");
			long date=Long.parseLong(query.request.getParameter("timestamp"));
			List<String> invalidates=new ArrayList<String>();
			for(ReviewEntry re: instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl()))
			{
				String key=invalidatePrefix+re.getSha1Sum();
				String inv=query.request.getParameter(key);
				if(key.equals(inv))
				{
					invalidates.add(re.getSha1Sum());
				}
			}
			System.out.println("Annotation: "+annot+" "+user+" "+comment+" "+date+" "+sha1+" invalidates: "+invalidates);
			if(null == annot)
			{
				throw new RuntimeException("annotation type field must be selected!");
			}

			ReviewEntry entry=new ReviewEntry(rs.getSourceFolderId(),
					rs.getSourceFolderUrl(), rs.getFolderVersion(),
					sha1,
					rs.getFileVersion(), 
					rs.getSourceUrl(), 
					comment,
					annot, user, "all", date, invalidates);
			if(instance.getModel().getAnnotationsBySha1(entry.getSha1Sum()).size()>0)
			{
				throw new RuntimeException("Annotation already exists - or sha1sum hit.");
			}
			instance.saveEntry(entry);
			rtout.write("<h1>Annotation entry created!</h1>\n<pre>");
			rtcout.write(""+entry);
			rtout.write("</pre>");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			rtout.write("<h1>Annotation entry creation error</h1>\n");
			e.printStackTrace(new PrintWriter(out));
			e.printStackTrace();
		}
	}

	private void renderNormal() throws IOException {
		String url=query.getAfterModule();
		ReviewSource rs=instance.getModel().getSource(url);
		File f=instance.getModel().getFile(url);
		byte[] content=UtilFile.loadFile(f);
		String user=instance.getDefaultUser();
		String sha1=UtilSha1.getSHA1(content);
		long date=System.currentTimeMillis();
//		ReviewEntry entry=new ReviewEntry(rs.sourceFolderId,
//				rs.sourceFolderUrl, rs.folderVersion,
//				sha1,
//				rs.fileVersion, 
//				rs.sourceUrl, 
//				"",
//				"testReviewed", "rizsi", "all", System.currentTimeMillis());
//		String block=entry.toString();
		
//		rtout.write("<form method='post'><textarea name='annotationentry' rows=\"30\" cols=\"80\">");
//		rtcout.write(block);
//		rtout.write("</textarea><input type='submit'/></form>\n");
		rtout.write("<h2>Create annotation</h2>\n<form method='post'>\nUrl: ");
		rtcout.write(rs.getSourceFolderId()+"/"+rs.getSourceUrl());
		rtout.write("<br/>\nversion: ");
		rtcout.write(rs.getFileVersion());
		rtout.write("<br/>\nsha1: <input type=\"text\" name=\"sha1\" readonly=\"readonly\" value=\"");
		rtcout.write(sha1);
		rtout.write("\"/><br/>\n<select name=\"annotation\">\n <option value=\"none\">Please select!</option>\n");
		for(EReviewAnnotation annot : EReviewAnnotation.values()){
			rtout.write(" <option value=\"");
			rtcout.write(annot.name());
			rtout.write("\">");
			rtcout.write(annot.getDescription());
			rtout.write("</option>\n");
		}
		rtout.write("</select>\n<br/>\nuser: <input type=\"text\" name=\"user\" value=\"");
		rtcout.write(user);
		rtout.write("\"/> <br/>\ncomment:<br/>\n<textarea name='comment' rows=\"8\" cols=\"80\"></textarea><br/>\nTimestamp: <input type=\"text\" name=\"timestamp\" readonly=\"readonly\" value=\"");
		rtcout.write(""+date);
		rtout.write("\"/> ");
		rtcout.write(formatDate(date));
		rtout.write(" <br/>\n");
		Collection<ReviewEntry> res=instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl());
		for(ReviewEntry entry: res)
		{
			if(!instance.getModel().isInvalidated(entry.getSha1Sum()))
			{
				rtout.write("<input type=\"checkbox\" name=\"");
				rtcout.write(invalidatePrefix+entry.getSha1Sum());
				rtout.write("\" value=\"");
				rtcout.write(invalidatePrefix+entry.getSha1Sum());
				rtout.write("\">Invalidate: ");
				rtcout.write(formatAnnotation(entry));
				rtout.write("</input><br/>\n");
			}
		}
 		rtout.write("<input type='submit'/>\n</form>\n<hr/>\n");
	}
}
