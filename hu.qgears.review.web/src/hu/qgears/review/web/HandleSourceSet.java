package hu.qgears.review.web;

import hu.qgears.commons.UtilString;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewProgress;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.web.AnnotationTableRender.Column;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HandleSourceSet extends AbstractRender {
	public static final String prefix = "sourceset";

	public HandleSourceSet(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		List<String> pieces=UtilString.split(query.getAfterModule(), "/");
		String id=pieces.get(0);
		String command=null;
		if(pieces.size()>1)
		{
			command=pieces.get(1);
		}
		if("todo".equals(command))
		{
			renderTodo(id, command);
		}
		else
		{
			renderNormal(id, command);
		}
	}

	private void renderNormal(String id, String command) throws Exception {
		boolean oldColumn=true;
		boolean invalidColumn=true;
		boolean filterReview_=false;
		boolean sha1EqColumn=true;
		if("review".equals(command))
		{
			oldColumn=false;
			invalidColumn=false;
			filterReview_=true;
		}
		renderHeader("QReview fileset "+id);
		renderSelfHeader(id);
		ReviewSourceSet rss=instance.getModel().sourcesets.get(id);
		List<String> files=new ArrayList<String>(rss.sourceFiles);
		final List<ReviewSource> l=new ArrayList<ReviewSource>();
		for(String fileid: files)
		{
			ReviewSource rs=instance.getModel().getSource(fileid);
			l.add(rs);
		}
		final List<Column> columns=new ArrayList<AnnotationTableRender.Column>();
		final boolean filterReview=filterReview_;
		columns.add(new Column() {
			
			@Override
			public String getTitle() {
				return "current review";
			}

			@Override
			public void render(ReviewSource rs, PrintWriter out) {
				Collection<ReviewEntry> entries=instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl());
				for(ReviewEntry e:entries)
				{
					if(e.matches(rs)&&!instance.getModel().isInvalidated(e.getSha1Sum()))
					{
						//FIXME is getAnnotation().startsWith() necessary????
						if(!filterReview || e.getAnnotation().name().startsWith("review"))
						{
							rtcout.write(formatAnnotation(e)+"<br/>");
						}
					}
				}
			}
		});
		if(oldColumn)
		{
			columns.add(new Column() {
				
				@Override
				public void render(ReviewSource rs, PrintWriter out) {
					Collection<ReviewEntry> entries=instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl());
					for(ReviewEntry e:entries)
					{
						if(!e.matches(rs)&&e.matchesPrevious(rs) && !instance.getModel().isInvalidated(e.getSha1Sum()))
						{
							rtcout.write(formatAnnotation(e)+"<br/>");
						}
					}
				}
				@Override
				public String getTitle() {
					return "old review";
				}
			});
		}
		if(invalidColumn)
		{
			columns.add(new Column() {
				
				@Override
				public void render(ReviewSource rs, PrintWriter out) {
					Collection<ReviewEntry> entries=instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl());
					for(ReviewEntry e:entries)
					{
						if(instance.getModel().isInvalidated(e.getSha1Sum()))
						{
							rtcout.write(formatAnnotation(e)+"<br/>");
						}
					}
				}
				
				@Override
				public String getTitle() {
					return "invalid";
				}
			});
		}
		if(sha1EqColumn)
		{
			columns.add(new Column() {
				
				@Override
				public void render(ReviewSource rs, PrintWriter out) {
					Collection<ReviewEntry> entriesBySha1=null;
					if(rs.getSha1()!=null)
					{
						entriesBySha1=instance.getModel().reviewEntryByFileSha1.getMappedObjects(rs.getSha1());
					}
					if(entriesBySha1!=null)
					{
						for(ReviewEntry e:entriesBySha1)
						{
							rtcout.write(formatAnnotation(e)+"<br/>");
						}
					}
				}
				
				@Override
				public String getTitle() {
					return "sha1Eq";
				}
			});
		}
		new AnnotationTableRender(query, instance, out) {
			
			@Override
			protected List<ReviewSource> getSources() {
				return l;
			}

			@Override
			protected List<Column> getColumns() {
				return columns;
			}
		}.render();
		
		renderFooter();
	}

	private void renderSelfHeader(String id) {
		rtout.write("<h1>Source set: ");
		rtcout.write(id);
		rtout.write("</h1>\n<a href=\"/");
		rtcout.write(prefix);
		rtout.write("/");
		rtcout.write(id);
		rtout.write("\">default view</a><br/>\n<a href=\"/");
		rtcout.write(prefix);
		rtout.write("/");
		rtcout.write(id);
		rtout.write("/review\">review view</a><br/>\n<a href=\"/");
		rtcout.write(prefix);
		rtout.write("/");
		rtcout.write(id);
		rtout.write("/todo\">todo view</a><br/>\n");
		renderSelfProgress(id);
		rtout.write("<h2>Source files</h2>\n");
	}

	/**
	 * Renders the progress of all user.
	 * @param id 
	 */
	private void renderSelfProgress(String id) {
		ReviewModel model = instance.getModel();
		ReviewSourceSet sourceSet = model.sourcesets.get(id);
		int sourceFileCount = sourceSet.sourceFiles.size();
		rtout.write("<div class=\"accordion\">\n<h3>Progress of users</h3>\n<div>\n");
		for (String currentUser : model.getUsers()){
			ReviewProgress p = ReviewProgress.create(model, sourceSet, currentUser);
			int okCurrentCount = p.getReviewEntryCount(EReviewAnnotation.reviewOk, false);
			int todoCurrentCount = p.getReviewEntryCount(EReviewAnnotation.reviewTodo, false);
			int offCurrentCount = p.getReviewEntryCount(EReviewAnnotation.reviewOff, false);
			int overallCurrentCount = p.getOverallReviewEntryCount(false);
			int missingCurrentCount = p.getMissingReviewEntryCount(false);
			int okOldCount = p.getReviewEntryCount(EReviewAnnotation.reviewOk, true);
			int todoOldCount= p.getReviewEntryCount(EReviewAnnotation.reviewTodo, true);
			int offOldCount= p.getReviewEntryCount(EReviewAnnotation.reviewOff, true);
			int overallOldCount = p.getOverallReviewEntryCount(true);
			int missingOldCount = p.getMissingReviewEntryCount(true);
			int okCount = okCurrentCount + okOldCount;
			int todoCount = todoCurrentCount + todoOldCount;
			int offCount = offCurrentCount + offOldCount;
			int overallCount = overallCurrentCount + overallOldCount;
			int missingCount = missingCurrentCount + missingOldCount;
			rtout.write("<table>\n\t<tr>\n\t\t<th/>");
			rtcout.write(currentUser);
			rtout.write("<th>OK</th><th>TODO</th><th>OFF</th><th>OVERALL</th><th>MISSING</th>\n\t</tr>\n\t<tr>\n\t\t<th>OVERALL</th><td>");
			rtcout.write(e(okCount,sourceFileCount));
			rtout.write("</td><td>");
			rtcout.write(e(todoCount,sourceFileCount));
			rtout.write("</td><td>");
			rtcout.write(e(offCount,sourceFileCount));
			rtout.write("</td><td>");
			rtcout.write(e(overallCount,sourceFileCount));
			rtout.write("</td><td>");
			rtcout.write(e(missingCount,sourceFileCount));
			rtout.write("</td>\n\t</tr>\n\t<tr>\n\t\t<th>CURRENT</th><td>");
			rtcout.write(e(okCurrentCount,okCount));
			rtout.write("</td><td>");
			rtcout.write(e(todoCurrentCount,todoCount));
			rtout.write("</td><td>");
			rtcout.write(e(offCurrentCount,offCount));
			rtout.write("</td><td>");
			rtcout.write(e(overallCurrentCount,overallCount));
			rtout.write("</td><td>");
			rtcout.write(e(missingCurrentCount,missingCount));
			rtout.write("</td>\n\t</tr>\n\t<tr>\n\t\t<th>OLD</th><td>");
			rtcout.write(e(okOldCount,okCount));
			rtout.write("</td><td>");
			rtcout.write(e(todoOldCount,todoCount));
			rtout.write("</td><td>");
			rtcout.write(e(offOldCount,offCount));
			rtout.write("</td><td>");
			rtcout.write(e(overallOldCount,overallCount));
			rtout.write("</td><td>");
			rtcout.write(e(missingOldCount,missingCount));
			rtout.write("</td>\n\t</tr>\n</table>\n<p/>\n");
		}//end for
			rtout.write("</div>\n</div>\n<script type=\"text/javascript\">\n\tinitAccordion();\n</script>\n");
	}

	private String e(int val, int max){
		return String.format("<p style=\"text-align : center;\"> %d / %d <br>(%05.2f %%)</p>", val ,max ,max == 0 ? 0 :(100f * val) /max);
	}
	
	private void renderTodo(String id, String command) throws Exception {
		renderHeader("QReview TODO fileset "+id);
		renderSelfHeader(id);
		ReviewSourceSet rss=instance.getModel().sourcesets.get(id);
		List<String> files=new ArrayList<String>(rss.sourceFiles);
		final Map<String, ReviewSorter> reviews=ReviewSorter.parseAll(instance, files);
		final List<ReviewSource> l=new ArrayList<ReviewSource>();
		for(String fileid: files)
		{
			ReviewSorter rs=reviews.get(fileid);
			boolean isOk=rs.current.containsKey(EReviewAnnotation.reviewOk);
			boolean isOff=rs.notInvalid.containsKey(EReviewAnnotation.reviewOff);
			boolean isTodo=rs.notInvalid.containsKey(EReviewAnnotation.reviewTodo);
			if((!isOk&&!isOff) || isTodo)
			{
				l.add(rs.source);
			}else
			{
				// Nothing to do
			}
		}
		final List<Column> columns=new ArrayList<Column>();
		columns.add(new AnnotationTableRender.Column()
		{
			@Override
			public String getTitle() {
				return "current";
			}

			@Override
			public void render(ReviewSource rs, PrintWriter out) {
				for(ReviewEntry e: reviews.get(rs.modelUrl()).currentList)
				{
					rtcout.write(formatAnnotation(e)+"<br/>");
				}
			}
		});
		
		columns.add(new AnnotationTableRender.Column()
		{
			@Override
			public String getTitle() {
				return "old";
			}

			@Override
			public void render(ReviewSource rs, PrintWriter out) {
				for(ReviewEntry e: reviews.get(rs.modelUrl()).oldList)
				{
					rtcout.write(formatAnnotation(e)+"<br/>");
				}
			}
		});
		new AnnotationTableRender(query, instance, out) {
			
			@Override
			protected List<ReviewSource> getSources() {
				return l;
			}

			@Override
			protected List<Column> getColumns() {
				return columns;
			}
		}.render();
		renderFooter();
	}
}
