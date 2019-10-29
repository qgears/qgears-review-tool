package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.eclipse.ui.actions.ExportStatisticsAction;
import hu.qgears.review.eclipse.ui.actions.RefreshViewerAction;
import hu.qgears.review.eclipse.ui.views.AbstractReviewToolView;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
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

/**
 * Defines a table viewer that show statistics about review sources (code
 * coverage metrics, progress / etc.). Generating statistics is done using
 * {@link ReportGenerator}
 * 
 * @author agostoni
 * 
 */
public class ReviewToolStatisticsView extends AbstractReviewToolView implements
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

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		statisticsTable = createTableViewer(parent);
		getViewSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		createContextMenus(statisticsTable);
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
		Object sourceSetCandidate = getSingleSelection(selection);
		if (sourceSetCandidate != null) {
			if (sourceSetCandidate instanceof ReviewSourceSetView) {
				ReviewSourceSetView reviewSourceSetView = (ReviewSourceSetView) sourceSetCandidate;
				ReviewInstance riCandidate = getReviewInstance();
				if (riCandidate != null) {
					statisticsTable.setInput(new StatisticsTableInput(
							riCandidate,
							reviewSourceSetView
									.getModelElement(), false));
				}
			} else if (sourceSetCandidate instanceof SourceTreeElement) {
				SourceTreeElement ste = (SourceTreeElement) sourceSetCandidate;
				ReviewInstance riCandidate = getReviewInstance();
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

	@Override
	protected void fillMenuManager(IMenuManager manager) {
		manager.setRemoveAllWhenShown(true);
		manager.add(new RefreshViewerAction(statisticsTable));
		manager.add(new ReloadStatisticsAction(statisticsTable.getInput()));
		if (statisticsTable.getInput() instanceof StatisticsTableInput){
			manager.add(new ExportStatisticsAction((StatisticsTableInput) statisticsTable.getInput(),true));
			manager.add(new ExportStatisticsAction((StatisticsTableInput) statisticsTable.getInput(),false));
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		getSite().setSelectionProvider(null);
	}

	@Override
	protected void reviewModelChanged() {
		if (getReviewInstance() == null){
			//clearing statistics table content if configuration was reloaded
			statisticsTable.setInput(null);
		}
	}

}
