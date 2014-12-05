package hu.qgears.review.tool;

import hu.qgears.commons.UtilString;
import hu.qgears.review.tool.ConfigParsingResult.Problem;
import hu.qgears.review.tool.ConfigParsingResult.Problem.Type;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.Element;

public class PomFileSet {
	private static final Logger logger = Logger.getLogger(PomFileSet.class.getName());
	
	public static final String POMFILESET_ID = "pomfileset";

	static public class Params
	{
		/**
		 * All source files (from version control or generated code)
		 * that do the application
		 */
		public List<String> fileset=new ArrayList<String>();
		/**
		 * Parameters for the pomFileBytes file
		 */
		public Map<String, String> params=new HashMap<String, String>();
		public byte[] pomFileBytes;
	}

	private int moduleSourceFileCounter = 0;
	private int exclusionCounter = 0;
	/**
	 * Filters a the list of files extracted form POM file set definition
	 * against the criteria defined in the POM itself.
	 * @param problems 
	 */
	public List<String> filter(final Params params,
			final File fileSetDefFile, final File pomXmlFile, 
			final List<Problem> problems) throws Exception {
		final Document pomDocument = UtilDom4j.read(
				new ByteArrayInputStream(params.pomFileBytes));
		
		UtilDom4j.cleanNamespace(pomDocument);
		
		final PatternCollection patternCollection = 
				parsePatternCollection(pomDocument);
		final List<String> modulepaths = parseModulePaths(pomDocument, 
				params.params, problems, fileSetDefFile, pomXmlFile);
		final Set<String> sourceFileSet = new HashSet<String>(params.fileset);
		final List<String> retfileset = filter(sourceFileSet, 
				modulepaths, patternCollection, pomXmlFile, problems);
		
		return retfileset;
	}

	/**
	 * Filters a file set against a pattern, retaining files only if they are
	 * not excluded by a filter rule and are within one of modules specified in
	 * the POM.  
	 * @param sourceFileSet the set of files to be filtered
	 * @param modulePaths module paths, according to which files will be 
	 * filtered, so that files will be excluded which do not belong to any of
	 * the modules
	 * @param patternCollection a collection of inclusion and exclusion rules 
	 * @return a filtered list of files
	 */
	private List<String> filter(final Set<String> sourceFileSet,
			final List<String> modulePaths, final PatternCollection patternCollection,
			final File pomXmlFile, final List<Problem> problems) {
		for (final String modulepath : modulePaths) {
			if (!sourceFileSet.contains(modulepath)) {
				logger.warning("module does not exist: " + modulepath);
				
				problems.add(new Problem(Type.ERROR, "The module '" +
						modulepath + "' defined in " + pomXmlFile + 
						" cannot be found in the source file set. The " +
						"resulting list of files, that are subject to " +
						"review, may not be complete."));
			}
		}
		
		final List<String> filteredFileSet=new ArrayList<String>();
		
		for (final String sourceFileName : sourceFileSet) {
			if (isSubjectToReview(sourceFileName, modulePaths, 
					patternCollection)) {
				filteredFileSet.add(sourceFileName);
			}
		}
		
		/* Count of files excluded by the pattern collection */
		final int remainingCount = moduleSourceFileCounter - exclusionCounter;
		
		if (remainingCount == 0 || remainingCount < (moduleSourceFileCounter / 10)) {
			final String amountHint = remainingCount == moduleSourceFileCounter ?
					"All " : "More than 90% of the ";
			final String problemDetails = "Unfiltered source file count: " +
					moduleSourceFileCounter + "; Hits: " + exclusionCounter;
			
			problems.add(new Problem(Type.WARNING, amountHint + " source " +
					"files where discarded by the filtering rules and " +
					"non-matching module paths defined in " + pomXmlFile,
					problemDetails.toString()));
		}
		
		return filteredFileSet;
	}

	/**
	 * Determines whether a file, given by its, name, is the part of the set of
	 * files subject to review or not.
	 * @param fileName path of the file to be checked
	 * @param modulepaths path of the modules to be reviewed
	 * @param patternCollection selection and exclusion criteria
	 * @return true if the file given by its name is part 
	 */
	private boolean isSubjectToReview(final String fileName, 
			final List<String> modulepaths,
			final PatternCollection patternCollection) {
		for (final String modulepath: modulepaths) {
			if (fileName.startsWith(modulepath)) {
				moduleSourceFileCounter++;
				
				if (patternCollection.checkMatch(fileName)) {
					return true;
				} else {
					exclusionCounter++;
				}
			}
		}
		return false;
	}

	/**
	 * Resolves all module paths from the POM, substituting the given 
	 * parameters.
	 * @param pomDocument the POM document
	 * @param the map of parameters that, if any, will be substituted into the
	 * paths parsed from the POM document
	 * @return the module paths resolved
	 */
	private List<String> parseModulePaths( final Document pomDocument, 
			final Map<String, String> params, final List<Problem> problems,
			final File fileSetDefFile, final File pomXmlFile) {
		final List<String> modulepaths=new ArrayList<String>();
		final List<Element> modulePathElements =  UtilDom4j.selectElements(
				pomDocument.getRootElement(), "//modules/module");
		
		if (modulePathElements.isEmpty()) {
			problems.add(new Problem(Type.WARNING, "No module paths or module" +
					" path templates could be enumerated in " + pomXmlFile +
					" referred in file set definition file " + fileSetDefFile));
		}
		
		for (final Element modulePathElement: modulePathElements) {
			String modulePathElemText = modulePathElement.getText();
			int paramindex = modulePathElemText.indexOf("${");
			
			while (paramindex >=0) {
				final int endindex = modulePathElemText.indexOf("}", paramindex);
				
				if (endindex < 0) {
					throw new RuntimeException("Unmatched bracket in module" +
							"path element: '" + modulePathElemText + "' in " +
									"POM xml file " + pomXmlFile);
				}
				
				final String paramname = modulePathElemText.substring(paramindex+2, endindex);
				final String replacement = params.get(paramname);
				
				if (replacement == null) {
					throw new RuntimeException("Unset parameter '" + paramname +
							"' in module path '" + modulePathElemText + "' in " + 
							pomXmlFile + ". Record the value of this " +
							"parameter in " + fileSetDefFile + ".");
				}
				
				modulePathElemText = modulePathElemText.substring(0, paramindex) +
						replacement + modulePathElemText.substring(endindex+1);
				paramindex = modulePathElemText.indexOf("${");
			}

			/*
			 * Module paths can be specified two ways: by an identifier which 
			 * is equal to a directory, or the name a custom pom.xml file, the
			 * containing directory of which is eventually the module name. 
			 * Note that the second seems to be an undocumented feature of the 
			 * POM. 
			 */
			if (modulePathElemText.endsWith(".xml")) {
				final String moduleDir = new File(modulePathElemText).getParent();

				modulepaths.add(moduleDir);
			} else {
				modulepaths.add(modulePathElemText);
			}
		}
		return modulepaths;
	}

	private PatternCollection parsePatternCollection(final Document pomDocument) {
		final PatternCollection patternCollection = 
				new PatternCollection("**/*.java");
		final List<Element> exclusionPatterns = UtilDom4j.selectElements(
				pomDocument.getRootElement(), "//sonar.exclusions");
		
		for (Element e:exclusionPatterns)
		{
			String s=e.getText();
			List<String> pieces=UtilString.split(s, ",");
			for(String piece:pieces)
			{
				piece=piece.trim();
				/* 
				 * Avoiding adding the empty string as an exclusion pattern, as
				 * it filters out every source files.
				 */
				if (!piece.isEmpty()) {
					patternCollection.addNegPattern(piece);
				}
			}
		}
		return patternCollection;
	}

}
