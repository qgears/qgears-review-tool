package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.util.UtilWorkspace;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewSource;

import java.io.File;

import org.eclipse.core.resources.IFile;
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
	
	private static final String DEFAULT_TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";
	
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
					IFile wsFile = UtilWorkspace.getFileInWorkspace(file);
					if (wsFile != null && wsFile.exists()){
						IEditorDescriptor ed = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(wsFile.getName());
						if (ed == null){
							ed = PlatformUI.getWorkbench().getEditorRegistry().findEditor(DEFAULT_TEXT_EDITOR_ID);
						}
						if (ed == null) {
							UtilLog.showErrorDialog("No editor found for opening "+wsFile.getName(),null);
						} else {
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(wsFile), ed.getId());
						}
					} else {
						openAutoImportQuestionDialog(file,ste);
					}
				} catch (Exception e1) {
					UtilLog.showErrorDialog("Error during file open: "+file.getAbsolutePath(), e1);
				}
			}
		}
	}

	private void openAutoImportQuestionDialog(File file, SourceTreeElement ste) {
		ReviewSourceSetView sourceset = ste.getParent();
		String rset = sourceset.getModelElement().id;
		boolean doIt = MessageDialog.openQuestion(shell, "Open review source", "The file "+file+" is currently not imported into workspace.\nDo you wan't to import projects of source set '"+rset+"' now?");
		if (doIt) {
			new ImportProjectForSourcesetAction(sourceset, viewer).run();
		}
	}

	@Override
	public void run() {
		openTypeSearchDialog();
	}
}
