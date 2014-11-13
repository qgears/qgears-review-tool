package hu.qgears.review.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilSimpleProperties {
	public static Map<String, String> parseProperties(List<String> lines)
	{
		Map<String, String> ret=new HashMap<String, String>();
		for(String line: lines)
		{
			int idx=line.indexOf(": ");
			if(idx>=0)
			{
				String key=line.substring(0, idx);
				String value=line.substring(idx+2);
				ret.put(key, value);
			}
		}
		return ret;
	}

	public static List<String> propertiesToList(Map<String, String> params) {
		List<String> ret=new ArrayList<String>();
		for(String key:params.keySet())
		{
			ret.add(key+": "+params.get(key));
		}
		return ret;
	}
}
