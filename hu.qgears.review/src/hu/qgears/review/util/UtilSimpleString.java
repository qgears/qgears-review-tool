package hu.qgears.review.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that can be used to simply store and restore strings
 * in a file separated by separator characters.
 * 
 * Separator: the separator sequence - this can not be present in the escaped string
 * Escape: the escape sequence - in case this is present in the escaped string then it is escaped by duplicating itself
 * Escapeseparatorending: if the separator sequence is present in the source then it is replaced with this string.
 * 
 * @author rizsi
 *
 */
public class UtilSimpleString {
	private String separator;
	private String escape;
	private String escapeSeparatorEnding;
	/**
	 * Helper class that can be used to simply store and restore strings
	 * in a file separated by separator characters.
	 * 
	 * @param separator the separator sequence - this can not be present in the escaped string
	 * @param escape the escape sequence - in case this is present in the escaped string then it is escaped by duplicating itself
	 * @param escapeSeparatorEnding if the separator sequence is present in the source then it is replaced with this string.
	 */
	public UtilSimpleString(String separator, String escape,
			String escapeSeparatorEnding) {
		super();
		this.separator = separator;
		this.escape = escape;
		this.escapeSeparatorEnding = escapeSeparatorEnding;
	}

	/**
	 * Escape a string to contain none of the separator and the escape characters:
	 * @return
	 */
	public String escape(String input)
	{
		if(input.indexOf(separator)<0)
		{
			if(input.indexOf(escape)<0)
			{
				return input;
			}
		}
		StringBuilder ret=new StringBuilder();
		int at=0;
		while(at<input.length())
		{
			int indexofseparator=input.indexOf(separator, at);
			int indexofescape=input.indexOf(escape, at);
			boolean hasSeparator=indexofseparator>=0;
			boolean hasEscape=indexofescape>=0;
			if(hasSeparator&&hasEscape)
			{
				if(indexofescape<indexofseparator)
				{
					hasSeparator=false;
				}else
				{
					hasEscape=false;
				}
			}
			if(hasSeparator)
			{
				ret.append(input.substring(at, indexofseparator));
				at+=separator.length()+indexofseparator-at;
				ret.append(escape);
				ret.append(escapeSeparatorEnding);
			}else if(hasEscape)
			{
				ret.append(input.substring(at, indexofescape));
				at+=escape.length()+indexofescape-at;
				ret.append(escape);
				ret.append(escape);
			}else
			{
				ret.append(input.substring(at));
				at=input.length();
			}
		}
		return ret.toString();
	}

	public String unescape(String input)
	{
		if(input.indexOf(escape)<0)
		{
			return input;
		}
		StringBuilder ret=new StringBuilder();
		int at=0;
		while(at<input.length())
		{
			int indexofescape=input.indexOf(escape, at);
			boolean hasEscape=indexofescape>=0;
			if(hasEscape)
			{
				if(input.startsWith(escape, indexofescape+escape.length()))
				{
					ret.append(input.substring(at, indexofescape));
					ret.append(escape);
					at+=indexofescape-at+escape.length()*2;
				}else if(input.startsWith(escapeSeparatorEnding, indexofescape+escape.length()))
				{
					ret.append(input.substring(at, indexofescape));
					ret.append(separator);
					at+=indexofescape-at+escape.length()+escapeSeparatorEnding.length();
				}else
				{
					ret.append(input.substring(at, indexofescape+escape.length()));
					at=indexofescape+escape.length();
//					throw new ParseException(input, at);
				}
			}else
			{
				ret.append(input.substring(at));
				at=input.length();
			}
		}
		return ret.toString();
	}
	public List<String> splitAndUnescape(String source)
	{
//		List<String> pieces=UtilString.split(source, separator);
		List<String> ret=new ArrayList<String>();
		int at=0;
		while(at<source.length())
		{
			int idx=source.indexOf(separator, at);
			if(idx<0)
			{
				String piece=source.substring(at);
				at+=piece.length();
				addPieceUnescaped(ret, piece);
			}else
			{
				String piece=source.substring(at, idx);
				at=idx+separator.length();
				addPieceUnescaped(ret, piece);
			}
		}
		return ret;
	}

	private void addPieceUnescaped(List<String> ret, String piece) {
		if(piece.length()>0)
		{
			ret.add(unescape(piece));
		}
	}

	public String escapeAndConcat(List<String> list) {
		StringBuilder ret=new StringBuilder();
		for(String s: list)
		{
			ret.append(escape(s));
			ret.append(separator);
		}
		return ret.toString();
	}

	public String getSeparator() {
		return separator;
	}

	public static String filterReturnChar(String annot) {
		int nextindex=annot.indexOf("\r");
		if(nextindex>=0)
		{
			StringBuilder filtered=new StringBuilder();
			int at=0;
			while(at<annot.length())
			{
				if(nextindex<0)
				{
					filtered.append(annot.substring(at));
					at=annot.length();
				}else
				{
					filtered.append(annot.substring(at, nextindex));
					at=nextindex+1;
				}
				nextindex=annot.indexOf("\r", at);
			}
			return filtered.toString();
		}
		return annot;
	}
}
