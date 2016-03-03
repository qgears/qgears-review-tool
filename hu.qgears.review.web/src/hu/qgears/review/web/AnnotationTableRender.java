package hu.qgears.review.web;

import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;

import java.io.PrintWriter;
import java.util.List;

abstract public class AnnotationTableRender extends AbstractRender
{
	static public interface Column
	{
		String getTitle();

		void render(ReviewSource rs, PrintWriter out);
		
	}

	public AnnotationTableRender(WebQuery query, ReviewInstance instance,
			PrintWriter pw) {
		super(query, instance);
		rtout=rtcout=out=pw;
	}

	@Override
	public void render() throws Exception {
		rtout.write("<table border='1'>\n<tr>\n<th>source url</th>\n<th>SVN folder revision</th>\n<th>revision</th>\n");
		List<Column> columns=getColumns();
		for(Column c:columns)
		{
			rtout.write("<th>");
			rtcout.write(c.getTitle());
			rtout.write("</th>\n");
		}
 		rtout.write("</tr>\n");
		
		for(ReviewSource rs:getSources())
		{
			String fileid=rs.modelUrl();
			rtout.write("<tr>\n<td><a href=\"");
			rtcout.write("/source/"+fileid);
			rtout.write("\">");
			rtcout.write(fileid);
			rtout.write("</a> ");
			rtcout.write(rs.getSha1());
			rtout.write("</td>\n");
			rtout.write("<td>");
			rtcout.write(""+rs.getFolderVersion());
			rtout.write("</td>\n<td>");
			rtcout.write(""+rs.getFileVersion());
			rtout.write("</td>\n");
			for(Column c:columns)
			{
				rtout.write("<td>\n");
				try
				{
					c.render(rs, out);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				rtout.write("</td>\n");
			}
			rtout.write("</tr>\n");
		}
		rtout.write("</table>\n");
	}
	

	protected abstract List<Column> getColumns();

	protected abstract List<ReviewSource> getSources();
}
