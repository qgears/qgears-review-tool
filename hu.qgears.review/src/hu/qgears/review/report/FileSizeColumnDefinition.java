package hu.qgears.review.report;

import java.io.File;
import java.util.Comparator;

/**
 * {@link ColumnDefinition} implementation that displays file size in bytes.
 * 
 * @author agostoni
 * 
 */
public class FileSizeColumnDefinition implements ColumnDefinition,Comparator<ReportEntry>{

	@Override
	public String getPropertyValue(ReportEntry obj) {
		File f = getFile(obj);
		if (f != null){
			return format(f.length());
		}
		return ReportEntryCSSHelper.NO_DATA;
	}

	private File getFile(ReportEntry obj){
		if (obj != null && obj.getSourceFile() != null && obj.getSourceFile().getFileInWorkingCopy() != null){
			return obj.getSourceFile().getFileInWorkingCopy();
		}
		return null;
	}
	
	private String format(long length) {
		if (length > 100* 1024){
			return String.format("%,d KB", length / 1024);
		} else {
			return String.format("%,d bytes", length);
		}
	}

	@Override
	public String getTitle() {
		return "File size";
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		return null;
	}

	@Override
	public Comparator<ReportEntry> getComparator() {
		return this;
	}
	
	@Override
	public int compare(ReportEntry o1, ReportEntry o2) {
		File f1 = getFile(o1);
		File f2 = getFile(o2);
		long l1 = f1 == null ? 0 : f1.length();
		long l2 = f2 == null ? 0 : f2.length();
		return Long.valueOf(l1).compareTo(l2);
	}

}
