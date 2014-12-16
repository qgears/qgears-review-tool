package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.eclipse.ui.actions.ExportStatisticsAction;
import hu.qgears.review.eclipse.ui.actions.RefreshViewerAction;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.eclipse.ui.views.properties.ReviewToolPropertyPage;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Defines a table viewer that show statistics about review sources (code
 * coverage metrics, progress / etc.). Generating statistics is done using
 * {@link ReportGenerator}
 * 
 * @author agostoni
 * 
 */
public class ReviewToolStatisticsView extends ViewPart implements
		ISelectionListener {

	/**
	 * Action that forces reloading table input.
	 * 
	 * @author agostoni
	 * 
	 */
	public class ReloadStatisticsAction extends Action {

		private StatisticsTableInput input;

		public ReloadStatisticsAction(Object input) {
			if (input != null && input instanceof StatisticsTableInput) {
				this.input = (StatisticsTableInput) input;
			}
			updateState();
		}

		private void updateState() {
			if (input != null) {
				setText("Reload statistics of " + input.getSourceSet());
				setEnabled(true);
			} else {
				setText("Reload statistics ");
				setEnabled(false);
			}
		}

		@Override
		public void run() {
			statisticsTable.setInput(new StatisticsTableInput(input
					.getReviewInstance(), input.getSourceSet(), true));
		}
	}

	private TableViewer statisticsTable;
	private Object propertySheetPage;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		statisticsTable = createTableViewer(parent);
		getViewSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		createContextMenus();
	}

	protected TableViewer createTableViewer(Composite parent) {
		final TableViewer viewer = new TableViewer(parent);
		final ReviewToolStatisticsTableLabelProvider p = new ReviewToolStatisticsTableLabelProvider();
		int columnIndex = 0;
		for (String col : p.getColumnNames()) {
			TableViewerColumn tc = new TableViewerColumn(viewer, SWT.NONE);
			final TableColumn c = tc.getColumn();
			c.setText(col);
			c.setWidth(100);
			c.setMoveable(true);
			final int index = columnIndex;
			tc.getColumn().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					p.update(index);
					int i = 0;
					for (TableColumn c :viewer.getTable().getColumns()){
						c.setText(p.getHeaderText(i++));
					}
					viewer.refresh();
				}
			});
			columnIndex++;
		}
		viewer.getTable().setHeaderVisible(true);
		viewer.setLabelProvider(p);
		viewer.setSorter(p.getSorter());
		ReviewToolStatisticsTableContentProvider provider = new ReviewToolStatisticsTableContentProvider();
		viewer.setContentProvider(provider);
		getSite().setSelectionProvider(provider);
		viewer.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		return viewer;
	}

	@Override
	public void setFocus() {
		statisticsTable.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!selection.isEmpty() && selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object sourceSetCandidate = sel.getFirstElement();
			if (sourceSetCandidate instanceof ReviewSourceSetView) {
				ReviewInstance riCandidate = (ReviewInstance) part
						.getAdapter(ReviewInstance.class);
				if (riCandidate != null) {
					statisticsTable.setInput(new StatisticsTableInput(
							riCandidate,
							((ReviewSourceSetView) sourceSetCandidate)
									.getModelElement(), false));
				}
			} else if (sourceSetCandidate instanceof SourceTreeElement) {
				ReviewInstance riCandidate = (ReviewInstance) part
						.getAdapter(ReviewInstance.class);
				SourceTreeElement ste = (SourceTreeElement) sourceSetCandidate;
				if (riCandidate != null) {
					statisticsTable.setInput(new StatisticsTableInput(
							riCandidate, ste.getSet(), false));
				}
				selectReportEntry(ste);
			}
		}
	}

	/**
	 * Selects the report entry in table, that belongs to specified review
	 * source.
	 * 
	 * @param ste
	 *            The review source.
	 */
	private void selectReportEntry(SourceTreeElement ste) {
		if (statisticsTable.getInput() instanceof StatisticsTableInput) {
			StatisticsTableInput ip = (StatisticsTableInput) statisticsTable
					.getInput();
			if (ip.getReport() != null) {
				for (ReportEntry re : ip.getReport()) {
					if (re.getSourceFile() == ste.getSource()) {
						statisticsTable.setSelection(
								new StructuredSelection(re), true);
						break;
					}
				}
			}
		}
	}

	protected void createContextMenus() {
		IMenuListener listener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		};
		getViewSite().getActionBars().getMenuManager()
				.addMenuListener(listener);
		fillContextMenu(getViewSite().getActionBars().getMenuManager());

		MenuManager viewMenu = new MenuManager();
		fillContextMenu(viewMenu);
		viewMenu.addMenuListener(listener);
		statisticsTable.getControl().setMenu(
				viewMenu.createContextMenu(statisticsTable.getControl()));
		getSite().registerContextMenu(viewMenu, statisticsTable);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.setRemoveAllWhenShown(true);
		manager.add(new RefreshViewerAction(statisticsTable));
		manager.add(new ReloadStatisticsAction(statisticsTable.getInput()));
		if (statisticsTable.getInput() instanceof StatisticsTableInput){
			manager.add(new ExportStatisticsAction((StatisticsTableInput) statisticsTable.getInput()));
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		getSite().setSelectionProvider(null);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(IPropertySheetPage.class)) {
			if (propertySheetPage == null) {
				propertySheetPage = new ReviewToolPropertyPage();
			}
			return propertySheetPage;
		}
		return super.getAdapter(adapter);
	}

}
