package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.actions.LinkWithEditorToggleAction;
import hu.qgears.review.eclipse.ui.views.model.AbstractViewModel;
import hu.qgears.review.eclipse.ui.views.model.IReviewModelVisitor;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.ISelectionService;
import org.osgi.framework.Bundle;

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
			final Bundle bundle = Platform.getBundle(ReviewToolUI.PLUGIN_ID);
			final ILog log = Platform.getLog(bundle);

			try {
				/*
				 * Resolving the canonical path of the file selected in a source 
				 * code editor.
				 */
				final File canonicalEditorFile = file.getCanonicalFile();
				Object input = (Object) viewer.getInput();
				final List<SourceTreeElement> matchingElements = new ArrayList<SourceTreeElement>();
				
				if (input != null && input instanceof AbstractViewModel<?>){
					AbstractViewModel<?> abstractViewModel = (AbstractViewModel<?>) input;
					abstractViewModel.visit(new IReviewModelVisitor() {
						@Override
						public boolean visit(AbstractViewModel<?> reviewModelElement) {
							if(reviewModelElement instanceof SourceTreeElement){
								final SourceTreeElement ste = 
										(SourceTreeElement) reviewModelElement;
								final File fileInWorkingCopy = 
										ste.getSource().getFileInWorkingCopy();
								try {
									/* 
									 * Canonical path of the file in the main
									 * view of the review tool.
									 */
									final File canonicalWorkingCopyFile = 
											fileInWorkingCopy.getCanonicalFile();
									if (canonicalWorkingCopyFile.equals(canonicalEditorFile)){
										matchingElements.add(ste);
									}
								} catch (final IOException e) {
									log.log(new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, 
											"Failed to resolve the canonical path of reviewed " +
											"file: " + fileInWorkingCopy, e));
								}
								return false;
							}
							return true;
						}
					});
				}
				viewer.setSelection(new StructuredSelection(matchingElements),true);
			} catch (final Exception e) {
				log.log(new Status(IStatus.ERROR, ReviewToolUI.PLUGIN_ID, 
						"Failed to synchronize selection between the review " +
						"tool main view and the source code editor. Affected" +
						"file: " + file, e));
			}
		}
	}

}
