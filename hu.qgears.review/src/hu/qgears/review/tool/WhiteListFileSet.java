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
	private List<Matcher> validPatterns = new ArrayList<Matcher>();
	
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
			if (!ID.equals(l) && !l.startsWith(COMMENT_MARK) && !l.isEmpty()){
				validPatterns.add(Pattern.compile(l).matcher(""));
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
		for (String s : stringList){
			for (Matcher m : validPatterns){
				m.reset(s);
				if (m.matches()){
					mList.add(s);
					break;
				}
			}
		}
		return mList;
	}
	
	
	
}
