package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Importer that loads projects into eclipse workspace.
 */
public class ImportProject implements IWorkspaceRunnable {

	private ReviewSourceSetView reviewSourceSet;
	/**
	 * Flag that indicates if one of descriptors cannot be loaded. Determines
	 * the return Status of the job.
	 */
	private boolean loadError = false;

	public ImportProject(ReviewSourceSetView reviewSourceSet) {
		super();
		this.reviewSourceSet = reviewSourceSet;
	}
	
	/**
	 * Creates an Eclipse Job which does do the project import. Call
	 * {@link Job#schedule()} to start the operation.
	 * 
	 * @return
	 */
	public Job createImportJob() {
		Job j = new Job("Importing projects for source set: "+reviewSourceSet.getModelElement().id) {
			@Override
			protected IStatus run(IProgressMonitor pm) {
				loadError = false;
				final IWorkspace workspace = ResourcesPlugin.getWorkspace();
				try {
					workspace.run(ImportProject.this, pm);
					if (loadError){
						return new Status(Status.WARNING, ReviewToolUI.PLUGIN_ID, "Import was incomplete! See error log for details!");
					} else {
						return Status.OK_STATUS;
					}
				} catch (CoreException e) {
					return new Status(Status.ERROR, ReviewToolUI.PLUGIN_ID, "Import failed!",e);
				}
			}
		};
		j.setUser(true);
		return j;
	}

	private File findProjectDescriptor(File f, SubMonitor pm) {
		File parent = f.getParentFile();
		while (parent != null) {
			checkCancel(pm);
			String[] chs = parent.list();
			if (chs != null){
				for (String s : chs){
					if (".project".equals(s)){
						return new File (parent,s);
					}
				}
			}
			parent = parent.getParentFile();
		}
		return null;
	}

	private void checkCancel(IProgressMonitor pm) {
		if (pm.isCanceled()) {
			throw new OperationCanceledException("Operation cancelled.");
		}
	}

	@Override
	public void run(IProgressMonitor arg0) throws CoreException {
		SubMonitor sm = SubMonitor.convert(arg0);
		sm.setWorkRemaining(3);
		Set<File> projects = collectProjectDescriptors(sm.newChild(1));
		importProjects(projects,sm.newChild(2));
	}

	private void importProjects(Set<File> pDescriptors, SubMonitor pm) {
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			pm.setTaskName("Importing projects...");
			pm.setWorkRemaining(pDescriptors.size());
			List<File> projectDescriptorsOrdered = new ArrayList<File>(pDescriptors);
			Collections.sort(projectDescriptorsOrdered);
			for (File f : projectDescriptorsOrdered) {
				pm.setTaskName("Importing "+f.getAbsolutePath());
				checkCancel(pm);
				SubMonitor loader = pm.newChild(1);
				loader.setWorkRemaining(3);
				try {
					IProjectDescription pd = workspace.loadProjectDescription(new Path(f.getCanonicalPath()));

					IProject project = workspace.getRoot().getProject(pd.getName());
        			if (!project.exists()) {
        				project.create(pd, loader.newChild(1));
        			}
        			if (!project.isOpen()){
        				project.open(loader.newChild(1));
        			}
        			project.refreshLocal(IProject.DEPTH_INFINITE, loader.newChild(1));
				} catch (Exception e) {
					loadError = true;
					UtilLog.logError("Cannot load descriptor "+f.getAbsolutePath(), e);
				}
				loader.done();
			}
		} finally {
			pm.done();
		}
	}

	private Set<File> collectProjectDescriptors(SubMonitor pm) {
		try {
			List<SourceTreeElement> children = reviewSourceSet.getChildren();
			pm.setTaskName("Searching for project descriptors...");
			pm.setWorkRemaining(children.size());
			Set<File> projectFiles = new HashSet<File>();
			for (SourceTreeElement ss : children) {
				checkCancel(pm);
				File f = ss.getModelElement().getFileInWorkingCopy();
				File pd = findProjectDescriptor(f,pm);
				if (pd != null) {
					projectFiles.add(pd);
				}
				pm.worked(1);
			}
			return projectFiles;
		} finally {
			pm.done();
		}
	}
	
}
