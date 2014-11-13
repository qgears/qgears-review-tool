package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.eclipse.ui.actions.LinkWithEditorToggleAction;
import hu.qgears.review.eclipse.ui.views.model.AbstractViewModel;
import hu.qgears.review.eclipse.ui.views.model.IReviewModelVisitor;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISelectionService;

/**
 * {@link LinkWithEditorToggleAction} implementation that supports browsing
 * {@link SourceTreeElement}.
 * 
 * @author agostoni
 * 
 */
public class LinkWithEditorSTEAction extends LinkWithEditorToggleAction{

	private StructuredViewer viewer;

	/**
	 * See
	 * {@link LinkWithEditorToggleAction#LinkWithEditorToggleAction(org.eclipse.jface.viewers.Viewer, ISelectionService, String)}
	 */
	public LinkWithEditorSTEAction(StructuredViewer target,
			ISelectionService selectionProvider, String viewId) {
		super(target, selectionProvider, viewId);
		this.viewer = target;
	}

	/* (non-Javadoc)
	 * @see hu.qgears.review.eclipse.ui.actions.LinkWithEditorToggleAction#selectFile(java.io.File)
	 */
	@Override
	protected void selectFile(final File file) {
		if (viewer != null && file != null){
			Object input = (Object) viewer.getInput();
			final List<SourceTreeElement> matchingElements = new ArrayList<SourceTreeElement>();
			if (input != null && input instanceof AbstractViewModel<?>){
				AbstractViewModel<?> abstractViewModel = (AbstractViewModel<?>) input;
				abstractViewModel.visit(new IReviewModelVisitor() {
					@Override
					public boolean visit(AbstractViewModel<?> reviewModelElement) {
						if(reviewModelElement instanceof SourceTreeElement){
							SourceTreeElement ste = (SourceTreeElement) reviewModelElement;
							if (ste.getSource().getFileInWorkingCopy().equals(file)){
								matchingElements.add(ste);
							}
							return false;
						}
						return true;
					}
				});
			}
			viewer.setSelection(new StructuredSelection(matchingElements),true);
		}
	}

}
