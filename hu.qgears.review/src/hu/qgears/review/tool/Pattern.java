package hu.qgears.review.tool;

import java.util.ArrayList;
import java.util.List;

public class Pattern {
	List<String> patterns=new ArrayList<String>();
	List<Pattern> negPatterns=new ArrayList<Pattern>();
	
	
	public Pattern() {
	}
	public Pattern(String pattern) {
		addPattern(pattern);
	}
	public void addPattern(String pattern)
	{
		patterns.add(pattern);
	}
	public void addNegPattern(String negPattern)
	{
		negPatterns.add(new Pattern(negPattern));
	}
	
	public boolean checkMatch(String s)
	{
		for(Pattern neg:negPatterns)
		{
			if(neg.checkMatch(s))
			{
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
}
