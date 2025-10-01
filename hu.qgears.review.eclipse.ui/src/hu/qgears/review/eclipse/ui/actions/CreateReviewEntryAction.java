package hu.qgears.review.eclipse.ui.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.eclipse.ui.wizard.CreateReviewEntryWizard;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;

/**
 * Action that opens {@link CreateReviewEntryWizard}.
 * 
 * @author agostoni
 * 
 */
public class CreateReviewEntryAction extends Action{

	private final SourceTreeElement currentSelection;
	private final StructuredViewer viewerToRefresh;
	private final ReviewInstance reviewInstance;

	public CreateReviewEntryAction(SourceTreeElement currentSelection, StructuredViewer viewerToRefresh, ReviewInstance reviewInstance) {
		this.currentSelection = currentSelection;
		this.viewerToRefresh = viewerToRefresh;
		this.reviewInstance = reviewInstance;
		setText("Create new review entry");
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
	}

	@Override
	public void run() {
		CreateReviewEntryWizard w = new CreateReviewEntryWizard(currentSelection.getSource(),reviewInstance.getModel());
		WizardDialog d = new WizardDialog(getShell(), w);
		d.setHelpAvailable(false);
		if (WizardDialog.OK == d.open()){
			try {
				final ReviewEntry newReviewEntry = w.getNewReviewEntry();
				reviewInstance.saveEntry(newReviewEntry);
				ReviewEntryView rev = currentSelection.getMatchingView(newReviewEntry);
				if (rev != null) {
					viewerToRefresh.setSelection(new StructuredSelection(rev));
				}
			} catch (IOException e) {
				UtilLog.showErrorDialog("Cannot create new entry", e);
			}
		}
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
}
