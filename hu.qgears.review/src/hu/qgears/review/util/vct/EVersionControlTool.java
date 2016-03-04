package hu.qgears.review.util.vct;


/**
 * Enumeration of supported VC tools.
 * 
 * @author agostoni
 *
 */
public enum EVersionControlTool {
	
	SVN ("svn "),
	GIT ("git ");

	private String code;
	
	private EVersionControlTool(String code) {
		this.code = code;
	}
	
	public boolean supports(String line){
		return line != null && line.startsWith(code);
	}
	
	public String getSourceFolder(String line){
		if (supports(line)){
			return line.substring(code.length());
		}
		return null;
	}

}
