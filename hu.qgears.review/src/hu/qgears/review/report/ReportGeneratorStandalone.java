package hu.qgears.review.report;

import hu.qgears.review.action.ConfigParsingResult;
import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.action.ConfigParsingResult.Problem;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSourceSet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import joptsimple.annot.AnnotatedClass;
import joptsimple.annot.JOHelp;
import joptsimple.annot.JOSimpleBoolean;

/**
 * Command line tool for running {@link ReportGenerator} and generation a HTML
 * report using {@link ReportGeneratorTemplate}.
 * 
 * @author agostoni
 * 
 */
public class ReportGeneratorStandalone {

	/**
	 * System property for specifying an error log file.
	 */
	private static final String SYS_PROP_ERROR_INDICATOR = "hu.qgears.report.error.indicator";
	
	/**
	 * Helper class for parsing command line arguments of this application.
	 * 
	 * @author agostoni
	 *
	 */
	public static class ApplicationParameters {

		@JOHelp("The mapping file used by review tool to load configuration.")
		public String mappingFile;

		@JOHelp("The path of output folder, where the generated resources will be placed.")
		public String out;
	
		@JOHelp("Comma (,) separated list of review source set identifiers. The program will generate report for these sets. If parameter is not specified, all review source set defined in configuration will be processed.")
		public List<String> reviewSourceSets;
		
		@JOSimpleBoolean
		@JOHelp("Prints help text and parameter desctiption to console.")
		public boolean help;
		
		private final AnnotatedClass c;

		public ApplicationParameters() throws Exception {
			c = new AnnotatedClass();
			c.parseAnnotations(this);
		}
		
		public void printHelpOn(PrintStream out) throws IOException {
			c.printHelpOn(out);
		}
		
	}

	private File mapping;
	private File outputFolder;
	private List<String> reviewSourceSets;
	
	public ReportGeneratorStandalone() {
	}

	public static void main(String[] args) throws Exception {
		ApplicationParameters params = new ApplicationParameters();
		try {
			params.c.parseArgs(args);
			ReportGeneratorStandalone app = new ReportGeneratorStandalone();
			app.run(params);
		} catch (Exception e) {
			error("Invalid parameters specified: " +e.getMessage());
			error("Valid parameters are:");
			params.printHelpOn(System.err);
			printErrorLog(e);
		}
	}

	private static void printErrorLog(Exception e) {
		String errorLogFile = System.getProperty(SYS_PROP_ERROR_INDICATOR);
		if (errorLogFile != null && !errorLogFile.isEmpty()){
			File error = new File (errorLogFile);
			try {
				PrintWriter pw = new PrintWriter(error,"UTF-8");
				pw.println("Report generation failed due to an exception!");
				if (e != null){
					e.printStackTrace(pw);
				}
				pw.close();
			} catch (Exception e1) {
				error("Cannot create error log file: " + e1.getMessage());
			}
		}
	}

	/**
	 * Prints an error message to console.
	 * 
	 * @param string
	 */
	private static void error(String string) {
		System.err.println(string);
	}
	/**
	 * Prints a message to console.
	 * 
	 * @param string
	 */
	private static void info(String string) {
		System.out.println(string);
	}

	/**
	 * Executes this report generator with specified parameters. See annotations
	 * on fields of {@link ApplicationParameters} class.
	 * 
	 * @param parameters
	 * @throws Exception
	 */
	public void run(ApplicationParameters parameters) throws Exception {
		if (parameters.help){
			printHelpOnConsole(parameters);
		} else {
			parseParameters(parameters);
			ReviewInstance c = loadConfiguration();
			ReviewModel model = c.getModel();
			if (reviewSourceSets == null || reviewSourceSets.isEmpty()){
				reviewSourceSets = new ArrayList<String>(model.sourcesets.keySet());
			}
			ReportGeneratorHtml genHtml = new ReportGeneratorHtml();
			for (String sourceSet : reviewSourceSets){
				if (model.sourcesets.containsKey(sourceSet)){
					try {
						File outputFile = getOutputFile(sourceSet);
						info("Generating report file for "+sourceSet+ " into "+outputFile.getAbsolutePath());
						genHtml.generateReport(new ReportGenerator(model, model.sourcesets.get(sourceSet)),outputFile,true,true,true);
						info("Generation finished without errors.");
					} catch (Exception e){
						error("Generationg report for "+sourceSet+ " failed.");
						e.printStackTrace();
					}
				} else {
					error("Skipping unknown review source set :"+sourceSet);
				}
			}
			genHtml.copyStyle(outputFolder);
		}
	}
	
	private void printHelpOnConsole(ApplicationParameters parameters)
			throws IOException {
		info("Command line tool for generation HTML reports from code review, and SONAR analisys results.");
		parameters.printHelpOn(System.out);
	}


	/**
	 * Returns the file where the report must be saved.
	 * 
	 * @param sourceSet
	 *            Identifies the {@link ReviewSourceSet}, output file name will
	 *            include this String.
	 * @return
	 */
	private File getOutputFile(String sourceSet) {
		return new File(outputFolder,sourceSet+".html");
	}

	/**
	 * Loads the review tool configuration using {@link LoadConfiguration}
	 * utility.
	 * 
	 * @return
	 */
	private ReviewInstance loadConfiguration() {
		try {
			final LoadConfiguration loadConfig = new LoadConfiguration();
			final ConfigParsingResult configParsingResult = 
					loadConfig.loadConfiguration(mapping);
			for (Problem p :configParsingResult.getProblems()){
				error(p.getType() +" " + p.getMessage() + " : "+p.getDetails());
			}
			final ReviewInstance c = configParsingResult.getReviewInstance();
			
			return c;
		} catch (Exception e) {
			error("Loading configuration failed! "+e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parses application parameters and performs validation on them.
	 * 
	 * @param parameters
	 */
	private void parseParameters(ApplicationParameters parameters) {
		notNull("out", parameters.out);
		outputFolder = new File(parameters.out); 
		if (outputFolder.exists()){
			if (!outputFolder.isDirectory()){
				throw new RuntimeException("Paramter 'out' must be a directory! "+parameters.out);
			}
		} else {
			if (!outputFolder.mkdirs()){
				throw new RuntimeException("Cannot create output directory "+parameters.out);
			}
		}
		notNull("mappingFile", parameters.mappingFile);
		mapping = new File(parameters.mappingFile);
		if (!mapping.isFile()){
			throw new RuntimeException("Parameter 'mappingFile' must point to existing file!");
		}
		reviewSourceSets = parameters.reviewSourceSets;
	}
	
	
	/**
	 * Throws {@link RuntimeException} with an error message if o is <code>null</code>.
	 * 
	 * @param param The parameter name to include in error message.
	 * @param o The object to null-check
	 */
	private void notNull(String param, Object o){
		if (o == null){
			throw new RuntimeException("Parameter "+param+" should not be null");
		}
	}
}
