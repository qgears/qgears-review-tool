package hu.qgears.review.util.vct;

import hu.qgears.review.util.vct.gitimpl.GitImpl;
import hu.qgears.review.util.vct.svnimpl.SvnStatus;

/**
 * Version control tool manager that returns the {@link IVersionControlTool} for
 * a {@link EVersionControlTool}.
 * 
 * @author agostoni
 * @since 2.0
 *
 */
public class VersionControlToolManager {

	private static VersionControlToolManager instance = new VersionControlToolManager();

	public static final VersionControlToolManager getInstance(){
		return instance;
	}
	
	public IVersionControlTool getImplementationFor(EVersionControlTool tool){
		switch (tool) {
		case SVN:
			return new SvnStatus();
		case GIT:
			return new GitImpl();
		default:
			throw new RuntimeException("Unimplemented version control tool: "+tool);
		}
		
	}
}
