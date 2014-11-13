package hu.qgears.review.tool;

import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;

public class PomFileSet {
	static public class Params
	{
		/**
		 * All source files (from version control or generated code)
		 * that do the application
		 */
		public List<String> fileset=new ArrayList<String>();
		/**
		 * Parameters for the pom file
		 */
		public Map<String, String> params=new HashMap<String, String>();
		public byte[] pom;
	}

	public List<String> reduce(Params params) throws Exception{
		Document d=UtilDom4j.read(new ByteArrayInputStream(params.pom));
		UtilDom4j.cleanNamespace(d);
		Pattern pattern=parsePattern(d);
		List<String> modulepaths=parseModulePaths(d, params.params);
		Set<String> fs=new HashSet<String>(params.fileset);
		List<String> retfileset=matchModuleDefinition(fs, modulepaths, pattern);
		return retfileset;
	}

	private List<String> matchModuleDefinition(Set<String> sourcefileset,
			List<String> modulepaths, Pattern pattern) {
		for(String modulepath:modulepaths)
		{
			if(!sourcefileset.contains(modulepath))
			{
				// TODO
				System.err.println("module does not exist: "+modulepath);
			}
		}
		List<String> retfileset=new ArrayList<String>();
		for(String s:sourcefileset)
		{
			if(matches(s, modulepaths, pattern))
			{
				retfileset.add(s);
			}
		}
		return retfileset;
	}

	private boolean matches(String fname, List<String> modulepaths, Pattern pattern) {
		for(String modulepath: modulepaths)
		{
			if(fname.startsWith(modulepath)&&pattern.checkMatch(fname))
			{
				return true;
			}
		}
		return false;
	}

	private List<String> parseModulePaths(Document d, Map<String, String> params) {
		List<String> modulepaths=new ArrayList<String>();
		List<Element> es=UtilDom4j.selectElements(d.getRootElement(), "//modules/module");
		for(Element e: es)
		{
			String modulePath=e.getText();
			int paramindex=modulePath.indexOf("${");
			while(paramindex>=0)
			{
				int endindex=modulePath.indexOf("}", paramindex);
				if(endindex<0)
				{
					throw new RuntimeException("Unmatched bracket: "+modulePath);
				}
				String paramname=modulePath.substring(paramindex+2, endindex);
				String replacement=params.get(paramname);
				if(replacement==null)
				{
					throw new RuntimeException("Unset parameter: "+paramname);
				}
				modulePath=modulePath.substring(0, paramindex)+
						replacement+modulePath.substring(endindex+1);
				paramindex=modulePath.indexOf("${");
			}
			modulepaths.add(modulePath);
		}
		return modulepaths;
	}

	private Pattern parsePattern(Document d) {
		Pattern ret=new Pattern("**/*.java");
		List<Element> es=UtilDom4j.selectElements(d.getRootElement(), "//sonar.exclusions");
		for(Element e:es)
		{
			String s=e.getText();
			List<String> pieces=UtilString.split(s, ",");
			for(String piece:pieces)
			{
				piece=piece.trim();
				ret.addNegPattern(piece);
			}
		}
		return ret;
	}

	private static List<String> findSourceFileset(String[][] paths) throws Exception {
		final List<String> ret=new ArrayList<String>();
		for(String[] pair: paths)
		{
			final String prefix=pair[0];
			File f=new File(pair[1]);
			if(f.exists())
			{
				new UtilFileVisitor()
				{
					@Override
					protected boolean visited(File dir, String localPath)
							throws Exception {
						if(dir.isFile())
						{
							ret.add(prefix+localPath);
						}
						return true;
					}
				}
			.visit(f);
			}
		}
		return ret;
	}

}
