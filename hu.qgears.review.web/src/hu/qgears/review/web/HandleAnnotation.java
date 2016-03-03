package hu.qgears.review.web;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.vct.IVersionControlTool;
import hu.qgears.review.util.vct.VersionControlToolManager;

import java.io.File;
import java.util.List;

public class HandleAnnotation extends AbstractRender {
	public static final String prefix="annotation";
	private boolean launchCompare;
	public HandleAnnotation(WebQuery query, ReviewInstance instance) {
		super(query, instance);
		String compare=query.request.getParameter("compare");
		launchCompare="true".equals(compare);
	}

	@Override
	public void render() throws Exception {
		String sha1=query.getAfterModule();
		List<ReviewEntry> entries=instance.getModel().getAnnotationsBySha1(sha1);
		String title="Annotation";
		for(ReviewEntry e: entries)
		{
			try {
				File f=instance.getModel().getFile(e.getFileUrl());
				title=f.getName()+" annotation";
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		renderHeader(title);
		if(entries.size()==0)
		{
			rtout.write("No such annotation entry in model!\n");
		}else
		{
			if(entries.size()>1)
			{
				rtout.write("Data duplication or SHA1 hit (WOW)!\n");
			}
			for(ReviewEntry e:entries)
			{
				rtout.write("<h2>Annotation</h2>\n<hr/>\n");
				for(ReviewEntry i:instance.getModel().getInvalidates(e.getSha1Sum()))
				{
					rtout.write("Invalidated by: ");
					rtcout.write(formatAnnotation(i));
					rtout.write("\n");
				}
				for(String s:e.getInvalidates())
				{
					for(ReviewEntry inv:instance.getModel().getAnnotationsBySha1(s))
					{
						rtout.write("Invalidates: ");
						rtcout.write(formatAnnotation(inv));
						rtout.write("\n");
					}
				}
				rtout.write("<b><pre>");
				rtcout.write(e.getComment());
				rtout.write("</pre></b>\n<hr/>\n<h2><a href=\"?compare=true\">Compare with current version</a></h2>\n");
				renderFileLinks(e.getFullUrl());
				rtout.write("<h2>Annotation in raw text:</h2>\n<pre>");
				rtcout.write(e.toString());
				rtout.write("</pre>\n");
				if(launchCompare)
				{
//					ReviewSource rs=instance.getModel().getSource(e.getFullUrl());
					File g=instance.getModel().getFile(e.getFullUrl());
					String svnurl=e.getFolderUrl()+"/"+e.getFileUrl();
					String revision=e.getFileVersion();
					ReviewSource rs = e.getReviewSource(instance.getModel());
					IVersionControlTool loader = VersionControlToolManager.getInstance().getImplementationFor(rs.getVersionControlTool());
					byte[] content=loader.downloadResource(svnurl, revision);
					File f=File.createTempFile(g.getName(), "");
					UtilFile.saveAsFile(f, content);
					Runtime.getRuntime().exec(new String[]{"/usr/bin/meld",
							g.getAbsolutePath(),
							f.getAbsolutePath()});
				}
			}
			rtout.write("\n");
		}
//		renderFileLinks(query.getAfterModule());
		renderFooter();
	}
}
