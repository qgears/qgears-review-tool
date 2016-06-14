package hu.qgears.review.eclipse.ui.views.todolist;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.actions.CompareWithEachOtherAction;
import hu.qgears.review.eclipse.ui.actions.CompareWithHeadAction;
import hu.qgears.review.eclipse.ui.actions.CreateReviewEntryAction;
import hu.qgears.review.eclipse.ui.actions.ExportStatisticsAction;
import hu.qgears.review.eclipse.ui.actions.ImportProjectForSourcesetAction;
import hu.qgears.review.eclipse.ui.actions.OpenJavaTypeAction;
import hu.qgears.review.eclipse.ui.actions.OpenJavaTypeDoubleClickListener;
import hu.qgears.review.eclipse.ui.actions.OpenReviewEntryDetailsAction;
import hu.qgears.review.eclipse.ui.actions.OpenReviewEntryDetailsDoubleClickListener;
import hu.qgears.review.eclipse.ui.actions.RefreshViewerAction;
import hu.qgears.review.eclipse.ui.actions.filters.TodoListFilter;
import hu.qgears.review.eclipse.ui.preferences.Preferences;
import hu.qgears.review.eclipse.ui.views.AbstractReviewToolView;
import hu.qgears.review.eclipse.ui.views.main.LinkWithEditorSTEAction;
import hu.qgears.review.eclipse.ui.views.main.ReviewSourceContentProvier;
import hu.qgears.review.eclipse.ui.views.main.ReviewSourceLabelProvider;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This view shows the {@link ReviewSource}s from selected
 * {@link ReviewSourceSet}, that haven't been reviewed by current user (
 * {@link Preferences#getDefaultUserName()}) yet.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolTodoListView extends AbstractReviewToolView implements ISelectionListener{

	private TreeViewer viewer;
	private OpenJavaTypeAction openJavaTypeAction;
	private OpenReviewEntryDetailsAction openReviewDetailsAction;
	private Label sourceSetLabel;
	private Label userLabel;
	private Label statusLabel;

	@Override
	public void createPartControl(Composite root) {
		root.setLayout(new GridLayout());
		createStatusGroup(root);
		createViewer(root);
		createContextMenus(viewer);
		createToolBar();
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	protected void createViewer(Composite root) {
		Composite parent = new Composite(root, SWT.NONE);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		parent.setLayout(new FillLayout(SWT.VERTICAL));
		viewer = new TreeViewer(parent,SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		ReviewSourceLabelProvider labelProvider = new ReviewSourceLabelProvider();
		viewer.setContentProvider(new ReviewSourceContentProvier());
		viewer.setLabelProvider( new DecoratingLabelProvider(labelProvider,labelProvider));
		getSite().setSelectionProvider(viewer);
		
		openJavaTypeAction = new OpenJavaTypeAction(viewer);
		openReviewDetailsAction = new OpenReviewEntryDetailsAction(viewer);
		viewer.addDoubleClickListener(new OpenJavaTypeDoubleClickListener(openJavaTypeAction));
		viewer.addDoubleClickListener(new OpenReviewEntryDetailsDoubleClickListener(openReviewDetailsAction));
		TodoListFilter todoListFilter = new TodoListFilter();
		todoListFilter.setUser(getUserName());
		viewer.addFilter(todoListFilter );
	}

	protected void createStatusGroup(Composite root) {
		Group statusGroup = new Group(root, SWT.NONE);
		statusGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statusGroup.setLayout(new GridLayout(2,false));
		new Label(statusGroup, SWT.NONE).setText("Source set : ");
		sourceSetLabel = new Label(statusGroup, SWT.NONE);
		sourceSetLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(statusGroup, SWT.NONE).setText("User : ");
		userLabel = new Label(statusGroup, SWT.NONE);
		userLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(statusGroup, SWT.NONE).setText("Status : ");
		statusLabel = new Label(statusGroup, SWT.NONE);
		statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/*
	 * Using internal API of JDT
	 */
	@SuppressWarnings("restriction")
	private void createToolBar() {
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new org.eclipse.jdt.internal.ui.actions.CollapseAllAction(viewer));
		toolbar.add(new LinkWithEditorSTEAction(viewer,getSite().getWorkbenchWindow().getSelectionService(),ReviewToolUI.ID_TODO_LIST_VIEW));

	}
	
	@Override
	protected void fillMenuManager(IMenuManager m) {
		//TODO code duplication with ReviewToolMainView: The same actions should be created here and there.
		m.setRemoveAllWhenShown(true);
		Object s = getSingleSelection(viewer.getSelection());
		List<Object> selection = getSelection(viewer.getSelection());
		if (s != null && s instanceof SourceTreeElement){
			m.add(openJavaTypeAction);
			SourceTreeElement ste = (SourceTreeElement) s;
			CreateReviewEntryAction action = new CreateReviewEntryAction(ste, viewer, getReviewInstance());
			m.add(action);
		}
		if (s instanceof ReviewEntryView){
			m.add(openReviewDetailsAction);
			m.add(new CompareWithHeadAction((ReviewEntryView) s));
			if (selection.size() == 2 && selection.get(1) instanceof ReviewEntryView){
				m.add(new CompareWithEachOtherAction((ReviewEntryView)s, (ReviewEntryView)selection.get(1)));
			}
		}
		if (s instanceof ReviewSourceSetView){
			ReviewSourceSetView reviewSourceSetView = (ReviewSourceSetView) s;
			m.add(new ExportStatisticsAction(reviewSourceSetView.getReviewModel(),reviewSourceSetView.getModelElement()));
			m.add(new ImportProjectForSourcesetAction(reviewSourceSetView,viewer));
		}
		m.add(new RefreshViewerAction(viewer){
			@Override
			public void run() {
				reviewModelChanged();
			}
		});
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!selection.isEmpty() && selection instanceof StructuredSelection){
			StructuredSelection sel = (StructuredSelection) selection;
			Object sourceSetCandidate = sel.getFirstElement();
			if (sourceSetCandidate instanceof ReviewSourceSetView){
				updateViewer((ReviewSourceSetView)sourceSetCandidate);
			}
		}
	}

	private void updateViewer(ReviewSourceSetView sourceSetCandidate) {
		ReviewInstance  ri = getReviewInstance();
		if (ri == null || sourceSetCandidate == null){
			viewer.setInput(null);
			viewer.refresh();
			String msg = "Select a review source set!";
			setPartName(msg);
			sourceSetLabel.setText(msg);
			statusLabel.setText("");
			userLabel.setText("");
		} else {
			viewer.setInput(sourceSetCandidate);
			viewer.refresh();
			String id = sourceSetCandidate.getModelElement().id;
			setPartName("Todo list for "+getUserName()+ " in source set '"+id+"'");
			statusLabel.setText(getStatusText(sourceSetCandidate));
			sourceSetLabel.setText(id);
			userLabel.setText(getUserName());
		}
	}
	
	private String getStatusText(ReviewSourceSetView sourceSetCandidate) {
		int remaining = viewer.getTree().getItemCount();
		int all = sourceSetCandidate.getChildren().size();
		int reviewed = all -remaining;
		int curr = all == 0 ? 0 : reviewed *100  / all;
		return String.format("%d%% (%d/%d reviewed, %d remaining)",curr, reviewed ,all,remaining);
	}

	private String getUserName() {
		//TODO create combo selector instead of userLabel
		return Preferences.getDefaultUserName();
	}

	@Override
	protected void reviewModelChanged() {
		if (getReviewInstance() == null){
			//clearing viewer if review instance was disposed
			updateViewer(null);
		} else {
			updateViewer((ReviewSourceSetView)viewer.getInput());
		}
	}
	
}
