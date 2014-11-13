package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.util.Preferences;

import java.io.File;

import org.eclipse.jdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Action for linking viewer selection with current editor content.
 * 
 * @author agostoni
 * 
 */
/*
 * Using internal API of JDT
 */
@SuppressWarnings("restriction")
public abstract class LinkWithEditorToggleAction extends AbstractToggleLinkingAction implements DisposeListener, ISelectionListener {

	private final ISelectionService selectionProvider;
	private final String viewId;

	/**
	 * 
	 * @param target
	 *            The target viewer. The action will be disposed when this
	 *            viewer disposes.
	 * @param selectionProvider
	 *            The selection provider to catch editor activation events.
	 * @param viewId
	 *            The viewid is used to save the toggle state of this action in
	 *            preference store. See
	 *            {@link Preferences#getLinkWithEditorToggleState(String)}.
	 */
	public LinkWithEditorToggleAction(Viewer target,ISelectionService selectionProvider,String viewId) {
		this.selectionProvider = selectionProvider;
		this.viewId = viewId;
		target.getControl().addDisposeListener(this);
		selectionProvider.addSelectionListener(this);
		setChecked(Preferences.getLinkWithEditorToggleState(viewId));
	}
	
	@Override
	public final void run() {
		Preferences.setLinkWithEditorToggleState(viewId,isChecked());
		if (isChecked()){
			updateSelection(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
		}
	}

	private void updateSelection(IEditorPart editor){
		if (isChecked() && editor != null){
			if (editor.getEditorInput() instanceof FileEditorInput){
				FileEditorInput fi = (FileEditorInput) editor.getEditorInput();
				if (fi.getURI() != null){
					File f = new File(fi.getURI());
					selectFile(f);
				}
			}
		}
	}
	
	/**
	 * The file specified as parameter is selected in an editor. Subclasses
	 * should find corresponding viewer elements and update the selection.
	 * 
	 * @param file
	 */
	protected abstract void selectFile(File file);
	
	@Override
	public void widgetDisposed(DisposeEvent e) {
		selectionProvider.removeSelectionListener(this);
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isChecked()){
			if (part instanceof IEditorPart){
				updateSelection((IEditorPart) part);
			}
		}
	}

	
}
