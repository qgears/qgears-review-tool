package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.wizard.OpenReviewEntryDetailsWizard;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Action that opens {@link OpenReviewEntryDetailsWizard}.
 * 
 * @author agostoni
 *
 */
public class OpenReviewEntryDetailsAction extends Action {

	private static final String TITLE = "Open review entry details";
	private final StructuredViewer viewer;

	public OpenReviewEntryDetailsAction(StructuredViewer viewer) {
		this.viewer = viewer;
		setText(TITLE);
		setImageDescriptor(JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_CFILE));
	}
	
	@Override
	public void run() {
		ReviewEntryView selection = getSelectedEntry(viewer.getSelection());
		if (selection != null){
			OpenReviewEntryDetailsWizard w = new OpenReviewEntryDetailsWizard(selection.getModelElement(),selection.getReviewModel());
			WizardDialog d = new WizardDialog(getShell(), w);
			d.setHelpAvailable(false);
			//read only view, ignoring return value
			d.open();
		}
	}
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	private ReviewEntryView getSelectedEntry(ISelection sel){
		if (!sel.isEmpty() && sel instanceof StructuredSelection){
			Object o = ((StructuredSelection)sel).getFirstElement();
			if (o instanceof ReviewEntryView){
				return (ReviewEntryView) o;
			}
		}
		return null;
	}
	

}
