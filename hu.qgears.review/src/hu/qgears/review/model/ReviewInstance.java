package hu.qgears.review.model;

import hu.qgears.review.action.LoadConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An instance of a running review server.
 * 
 * One Java process may contain multiple instances of review servers.
 * @author rizsi
 *
 */
public class ReviewInstance {
	private ReviewModel model;
	private File outputfile;
	public ReviewInstance(ReviewModel model, File outputfile) {
		super();
		this.model = model;
		this.outputfile=outputfile;
		outputfile.getParentFile().mkdirs();
	}
	public ReviewModel getModel() {
		return model;
	}
	public void saveEntry(ReviewEntry entry) throws IOException {
		model.addEntry(entry);
		String block=LoadConfiguration.ussBlocks.escape(entry.toString());
		FileOutputStream fos=new FileOutputStream(outputfile, true);
		try
		{
			Writer wri=new OutputStreamWriter(fos, "UTF-8");
			wri.write(LoadConfiguration.ussBlocks.getSeparator());
			wri.write(block);
			wri.close();
		}finally
		{
			fos.close();
		}
	}
	public String getDefaultUser() {
		 return System.getProperty("user.name");
	}
}
