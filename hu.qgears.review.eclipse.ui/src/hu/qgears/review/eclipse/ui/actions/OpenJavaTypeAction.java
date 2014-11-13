package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewSource;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Action that opens {@link OpenTypeSelectionDialog}. Can be used to open the
 * Java source from Eclipse workspace, that belnogs to a given
 * {@link ReviewSource}.
 * 
 * @author agostoni
 */
/*
 * OpenTypeSelection dialog is part of Eclipse's inner API, suppressing
 * generated warning
 */
@SuppressWarnings("restriction")
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
				OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(
						shell, false, PlatformUI.getWorkbench().getProgressService(),
						SearchEngine.createWorkspaceScope(), IJavaSearchConstants.TYPE);
				dialog.setInitialPattern(ste.getSource().getFullyQualifiedJavaName());
				if (OpenTypeSelectionDialog.OK == dialog.open()){
					Object[] types = dialog.getResult();
					try {
						JavaUI.openInEditor((IJavaElement)types[0], true, true);
					} catch (Exception x) {
						UtilLog.showErrorDialog("Cannot open Java type", x);
					}
				} 
			}
		}
	}
	
	@Override
	public void run() {
		openTypeSearchDialog();
	}
}
