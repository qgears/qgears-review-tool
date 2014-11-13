package hu.qgears.review.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Custom property page implementation for Review tool Eclipse UI Views.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolPropertyPage implements
IPropertySheetPage{

	private TreeViewer treeViewer;
	private Label label;
	private Composite container;

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NO);
		container.setLayout(new GridLayout());
		label=new Label(container, SWT.NO);
		label.setText("");
		label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
		
		final Tree tableTree = new Tree(container, SWT.FULL_SELECTION|
				SWT.HIDE_SELECTION);
		GridData layoutData=new GridData(SWT.FILL, SWT.FILL, true, true);
		tableTree.setLayoutData(layoutData);
		treeViewer = new TreeViewer(tableTree);
		TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.NONE, 0);
		column.getColumn().setText("Property");
		column.getColumn().setWidth(300);
		column.getColumn().setResizable(true);
		
		TreeViewerColumn column2 = new TreeViewerColumn(treeViewer, SWT.NONE, 1);
		column2.getColumn().setText("Value");
		column2.getColumn().setWidth(100);
		column2.getColumn().setResizable(true);
		tableTree.setHeaderVisible(true);
		tableTree.setLinesVisible(true);

		treeViewer.setContentProvider(
			new ReviewToolPropertyPageContentProvider());
		treeViewer.setLabelProvider(
			new ReviewToolPropertyPageLabelProvider());
		treeViewer.setInput(null);
		treeViewer.setColumnProperties(new String[] { "cica", "mica" });
		selectionChanged(null, null);
		
	}

	@Override
	public void dispose() {
	}

	@Override
	public Control getControl() {
		return container;
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
		
	}

	@Override
	public void setFocus() {
//		treeViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection){
			StructuredSelection sel = (StructuredSelection) selection;
			treeViewer.setInput(sel.getFirstElement());
			treeViewer.expandAll();
		}
	}

}
