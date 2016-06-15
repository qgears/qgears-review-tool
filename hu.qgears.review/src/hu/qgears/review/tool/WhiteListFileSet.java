package hu.qgears.review.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simplified file set definition class, that enumerates a white list of
 * required components. The definition file must contain valid java regular
 * expressions, that will be evaluated on the source file URLs. Matching sources
 * are part of fileset.
 * 
 * @author agostoni
 * 
 */
public class WhiteListFileSet {

	public static final Object ID = "whitelistfileset";
	private static final String COMMENT_MARK = "#";
	private static final String EXCLUSION_MARK = "! ";
	private List<Matcher> inclusionPatterns = new ArrayList<Matcher>();
	private List<Matcher> exclusionPatterns = new ArrayList<Matcher>();
	
	/**
	 * Creates a new instance. See {@link WhiteListFileSet head comment}
	 * 
	 * @param fileSetDefLines
	 *            The content of definition file read into memory.
	 */
	public WhiteListFileSet(final List<String> fileSetDefLines) {
		parseValidPatterns(fileSetDefLines);
	}


	private void parseValidPatterns(List<String> lines) {
		for (String l : lines){
			l = l.trim();
			if (!l.isEmpty() && !ID.equals(l)) {
				if (l.startsWith(COMMENT_MARK)) {
					//nothing to do, this line is commented by the used
				} else if (l.startsWith(EXCLUSION_MARK)) {
					//exclusion pattern found
					String pattern = l.substring(EXCLUSION_MARK.length());
					exclusionPatterns.add(Pattern.compile(pattern).matcher(""));
				} else {
					//normal inclusion pattern found
					inclusionPatterns.add(Pattern.compile(l).matcher(""));
				}
			}
		}
	}

	/**
	 * Filters non matching sources from specified list, and returns a list
	 * holding only the matching ones.
	 * 
	 * @param stringList
	 * @param problems 
	 * @return
	 */
	public List<String> reduce(final List<String> stringList) {
		List<String> mList = new ArrayList<String>();
		for (String s : stringList) {
			boolean matches = false;
			//first find out whether this line matches at least one line
			for (Matcher m : inclusionPatterns){
				m.reset(s);
				if (m.matches()){
					matches = true;
					break;
				}
			}
			//if matches, than check that any exclusion pattern disables the source file 
			if (matches) {
				for (Matcher ex : exclusionPatterns){
					if (ex.reset(s).matches()){
						matches = false;
						break;
					}
				}
			}
			if (matches){
				mList.add(s);
			}
		}
		return mList;
	}
	
	
	
}
