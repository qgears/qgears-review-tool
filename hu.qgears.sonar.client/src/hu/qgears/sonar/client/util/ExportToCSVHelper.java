package hu.qgears.sonar.client.util;

import hu.qgears.sonar.client.model.ResourceMetric;
import hu.qgears.sonar.client.model.SonarResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to export {@link SonarResource}s and its metrics into a CSV
 * file.
 * 
 * @author agostoni
 * 
 */
public class ExportToCSVHelper {
	
	private static final String separator = ";";
	private static List<String> metricKeys;
	
	/**
	 * Constructs a CSV from given resources and saves it into a file.
	 * 
	 * @param targetFile The target file, where the results will be written
	 * @param resouceList The list of resources to process.
	 * @throws IOException
	 */
	public static void saveToCSV(File targetFile,List<SonarResource> resouceList) throws IOException {
		StringBuilder bld = new StringBuilder();
		if (resouceList.size() > 0){
			printHeader(resouceList.get(0),bld);
		}
		for (SonarResource proj : resouceList){
			printLine(proj,bld);
			for (SonarResource dir : proj.getContainedResources()){
				printLine(dir,bld);
				for (SonarResource file : dir.getContainedResources()){
					printLine(file,bld);
				}
			}
		}
		metricKeys= null;
		FileOutputStream fos=new FileOutputStream(targetFile);
		try
		{
			OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
			osw.write(bld.toString());
			osw.close();
		}finally
		{
			fos.close();
		}
	}
	
	private static void printHeader(SonarResource sonarResource,
			StringBuilder bld) {
		metricKeys = new ArrayList<String>();
		bld.append("Resource").append(separator)
		.append("Scope").append(separator);
		boolean needComma = false;
		for (ResourceMetric m : sonarResource.getMetrics()){
			metricKeys.add(m.getMetricKey());
			bld.append(needComma ? separator : "").append(m.getMetricKey());
			if (!needComma){
				needComma = true;
			}
		}
		bld.append("\n");
	}

	private static void printLine(SonarResource file,StringBuilder bld){
//		String[] resName = file.getResurceName().split(":");
		bld.append(file.getResurceName()).append(separator)
		.append(file.getScope()).append(separator);
		boolean needComma = false;
		for (String key : metricKeys ){
			ResourceMetric m = file.getMetric(key);
			bld.append(needComma ? separator : "").append(m != null ? m.getFormattedValue() : "N/A");
			if (!needComma){
				needComma = true;
			}
		}
		bld.append("\n");
		
	}
}
