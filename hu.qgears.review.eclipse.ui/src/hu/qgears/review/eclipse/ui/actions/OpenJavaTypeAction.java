package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewSource;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Action that opens a {@link ReviewSource source file} with default editor.
 * 
 * @author agostoni
 * 
 */
public class OpenJavaTypeAction extends Action{

	private Viewer viewer;
	private Shell shell;
	
	public OpenJavaTypeAction(Viewer viewer) {
		super();
		this.viewer = viewer;
		this.shell = viewer.getControl().getShell();
		setText("Open type");
		setImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CFILE));
	}

	private void openTypeSearchDialog() {
		ISelection s = viewer.getSelection();
		if (s != null && !s.isEmpty() && s instanceof StructuredSelection){
			Object e = ((StructuredSelection) s).getFirstElement();
			if (e instanceof SourceTreeElement){
				SourceTreeElement ste = (SourceTreeElement) e;
				File file = ste.getModelElement().getFileInWorkingCopy();
				try {
					IFile wsFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getCanonicalPath()));
					if (wsFile != null && wsFile.exists()){
						IEditorDescriptor ed = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(wsFile.getName());
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(wsFile), ed.getId());
					} else {
						//TODO automatically import??
						MessageDialog.openInformation(shell, "Open review source", "The file "+file+" is not imported into Eclipse workspace, cannot be opened.");
					}
				} catch (Exception e1) {
					UtilLog.showErrorDialog("Error during file open: "+file.getAbsolutePath(), e1);
				}
			}
		}
	}
	
	@Override
	public void run() {
		openTypeSearchDialog();
	}
}
