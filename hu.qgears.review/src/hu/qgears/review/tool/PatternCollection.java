package hu.qgears.review.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File name patterns to include and exclude files to and from the list of all
 * files enumerated in the directories determined by file set definitions.  
 */
public class PatternCollection {
	private final List<String> patterns=new ArrayList<String>();
	private final List<PatternCollection> negPatterns = 
			new ArrayList<PatternCollection>();
	
	/**
	 * Stores hit counts for every exclusion patterns. 
	 */
	private final Map<String, Integer> exclusionHits = 
			new HashMap<String, Integer>();
	
	public PatternCollection() {
	}
	
	/**
	 * Convenience constructor for creating a pattern collection, adding the
	 * inclusion pattern specified as the parameter.
	 * @param pattern pattern by which files will be selected from a source file
	 * set, added to this {@link PatternCollection} by default.  
	 */
	public PatternCollection(String pattern) {
		addPattern(pattern);
	}
	
	public void addPattern(String pattern)
	{
		patterns.add(pattern);
	}
	
	public void addNegPattern(String negPattern)
	{
		negPatterns.add(new PatternCollection(negPattern));
	}
	
	private void increaseExclusionHits(final String patternId) {
		final Integer exlusionHitCount = exclusionHits.get(patternId);
		
		if (exlusionHitCount == null) {
			exclusionHits.put(patternId, 1);
		} else {
			exclusionHits.put(patternId, exlusionHitCount + 1);
		}
	}
	
	public boolean checkMatch(String s)
	{
		for(PatternCollection neg:negPatterns)
		{
			if(neg.checkMatch(s))
			{
				increaseExclusionHits(neg.getPatterns());
				return false;
			}
		}
		for(String p:patterns)
		{
			if(matches(p, s))
			{
				return true;
			}
		}
		return false;
	}
	
	protected boolean matches(String pattern, String fqn) {
		int idx=pattern.indexOf("**/*");
		if(idx>=0)
		{
			String pattern1=pattern.substring(0,idx);
			int idxpat1=fqn.indexOf(pattern1);
			if(idxpat1>=0)
			{
				return fqn.endsWith(pattern.substring(idx+4));
			}
			return false;
		}
		return fqn.endsWith(pattern);
	}

	public String getPatterns() {
		return Arrays.toString(patterns.toArray());
	}
	
	public Map<String, Integer> getExclusionHits() {
		return Collections.unmodifiableMap(new HashMap<String, Integer>(exclusionHits));
	}
}
