package hu.qgears.review.eclipse.ui.views.main;

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
import hu.qgears.review.eclipse.ui.actions.filters.FilterActionOption;
import hu.qgears.review.eclipse.ui.actions.filters.FilterByReviewAnnotation;
import hu.qgears.review.eclipse.ui.actions.filters.FilterByReviewStatus;
import hu.qgears.review.eclipse.ui.actions.filters.FilterBySourceSet;
import hu.qgears.review.eclipse.ui.actions.filters.FilterByUser;
import hu.qgears.review.eclipse.ui.preferences.Preferences;
import hu.qgears.review.eclipse.ui.views.AbstractReviewToolView;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.ReviewModelView;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.report.ReviewStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


/**
 * This is the main View of the Q-gears review tool Eclipse UI. Lists available
 * source sets, the sources and the existing review entries as a tree.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolMainView extends AbstractReviewToolView {

	/**
	 * A menu entry for filter options, that enables / disables displaying work
	 * of particular users.
	 * 
	 * @author agostoni
	 * 
	 */
	private class UserFilterOption extends FilterActionOption {
	
		public UserFilterOption(String text) {
			super(text,usersFilter.enabled(text));
			
		}
	
		@Override
		protected void actionRun(String text, boolean checked) {
			usersFilter.enableUser(text, checked);
			viewer.refresh();
		}
		
	}

	/**
	 * A menu entry for filter options, that enables / disables displaying a
	 * particulary review annotaiton type.
	 * 
	 * @author agostoni
	 * 
	 */
	private class ReviewAnnotationFilterOption extends FilterActionOption {
		public ReviewAnnotationFilterOption(String text) {
			super(text,annotFilter.enabled(text));
			
		}
		@Override
		protected void actionRun(String text, boolean checked) {
			annotFilter.enableAnnotation(text, checked);
			viewer.refresh();
		}
	}

	/**
	 * A menu entry for filter options, that enables / disables displaying
	 * review sources based on its revew status.
	 * 
	 * @author agostoni
	 * 
	 */
	private class ReviewStatusFilterOption extends FilterActionOption {
		public ReviewStatusFilterOption(String text) {
			super(text,statusFilter.enabled(ReviewStatus.valueOf(text)));
			
		}
		@Override
		protected void actionRun(String text, boolean checked) {
			statusFilter.enableStatus(ReviewStatus.valueOf(text), checked);
			viewer.refresh();
		}
	}

	/**
	 * A menu entry for filter options, that enables / disables displaying a
	 * particular sourcset.
	 * 
	 * @author agostoni
	 * 
	 */
	private class SourceSetFilterOption extends FilterActionOption {
		public SourceSetFilterOption(String text) {
			super(text,sourceSetFilter.enabled(text));
		}
		@Override
		protected void actionRun(String text, boolean checked) {
		    sourceSetFilter.enableSourceSet(text, checked);
		    viewer.refresh();
		}
	}

	private TreeViewer viewer;
	private FilterByUser usersFilter;
	private FilterByReviewAnnotation annotFilter;
	private FilterBySourceSet sourceSetFilter;
	private FilterByReviewStatus statusFilter;
	private OpenReviewEntryDetailsAction openReviewDetailsAction;
	private OpenJavaTypeAction openJavaTypeAction; 
	
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ReviewSourceLabelProvider lp = new ReviewSourceLabelProvider();
		viewer.setContentProvider(new ReviewSourceContentProvier());
		viewer.setLabelProvider( new DecoratingLabelProvider(lp,lp));
		/* Sorting by fqn by default */
		viewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(final Viewer viewerParam, final Object e1, final Object e2) {
				if (e1 instanceof SourceTreeElement && e2 instanceof SourceTreeElement) {
					final SourceTreeElement sourceTreeElement1 = (SourceTreeElement) e1;
					final String fqn1 = sourceTreeElement1.getModelElement().getFullyQualifiedJavaName();
					final SourceTreeElement sourceTreeElement2 = (SourceTreeElement) e2;
					final String fqn2 = sourceTreeElement2.getModelElement().getFullyQualifiedJavaName();
					
					return fqn1 != null && fqn2 != null ? fqn1.compareTo(fqn2) : 0;
				} else {
					return super.compare(viewerParam, e1, e2);
				}
			}
		});
		
		getSite().setSelectionProvider(viewer);
		openJavaTypeAction = new OpenJavaTypeAction(viewer);
		openReviewDetailsAction = new OpenReviewEntryDetailsAction(viewer);
		viewer.addDoubleClickListener(new OpenJavaTypeDoubleClickListener(openJavaTypeAction));
		viewer.addDoubleClickListener(new OpenReviewEntryDetailsDoubleClickListener(openReviewDetailsAction));
		viewer.setFilters(new ViewerFilter[] {
				sourceSetFilter = new FilterBySourceSet(),
				usersFilter = new FilterByUser(),
				annotFilter = new FilterByReviewAnnotation(),
				statusFilter = new FilterByReviewStatus()
		});
		createContextMenus(viewer);
		createToolBar();
		loadConfiguration();
	}

	/**
	 * Loads configuration on a BG thread.
	 */
	private void loadConfiguration() {
		viewer.setInput(new Status(IStatus.INFO, ReviewToolUI.PLUGIN_ID, LoadConfigurationJob.TITLE));
		final LoadConfigurationJob job = new LoadConfigurationJob();
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void done(final IJobChangeEvent event) {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						ReviewInstance reviewInstance = job.getReviewInstance();
						if (reviewInstance != null){
							viewer.setInput(new ReviewModelView(reviewInstance));
						} else {
							viewer.setInput(event.getResult());
						}
						setReviewInstance(reviewInstance);
					}
				});
			}
		});		
		setReviewInstance(null);
		job.schedule();
	}

	/*
	 * Using internal API of JDT
	 */
	@SuppressWarnings("restriction")
	private void createToolBar() {
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new org.eclipse.jdt.internal.ui.actions.CollapseAllAction(viewer));
		toolbar.add(new LinkWithEditorSTEAction(viewer,getSite().getWorkbenchWindow().getSelectionService(),ReviewToolUI.ID_MAIN_VIEW));
	}

	@Override
	protected void fillMenuManager(IMenuManager menuManager) {
		menuManager.setRemoveAllWhenShown(true);
		createActions(menuManager);
		if (getReviewInstance() != null){
			IMenuManager filters = createFilters();
			menuManager.add(filters);
		}
		
	}

	private void createActions(IMenuManager menuManager) {
		Object sel = getSingleSelection(viewer.getSelection());
		List<Object> selection = getSelection(viewer.getSelection());
		if (sel instanceof SourceTreeElement){
			menuManager.add(openJavaTypeAction);
			menuManager.add(new CreateReviewEntryAction((SourceTreeElement) sel, viewer,getReviewInstance()));
		}
		if (sel instanceof ReviewEntryView){
			menuManager.add(openReviewDetailsAction);
			CompareWithHeadAction compareWithHeadAction = new CompareWithHeadAction((ReviewEntryView) sel);
			compareWithHeadAction.setEnabled(selection.size() == 1);
			menuManager.add(compareWithHeadAction);
			if (selection.size() == 2 && selection.get(1) instanceof ReviewEntryView){
				CompareWithEachOtherAction cweaa = new CompareWithEachOtherAction((ReviewEntryView)sel, (ReviewEntryView)selection.get(1));
				menuManager.add(cweaa);
			}
		}
		if (sel instanceof ReviewSourceSetView){
			ReviewSourceSetView reviewSourceSetView = (ReviewSourceSetView) sel;
			menuManager.add(new ExportStatisticsAction(reviewSourceSetView.getReviewModel(),reviewSourceSetView.getModelElement(),true));
			menuManager.add(new ExportStatisticsAction(reviewSourceSetView.getReviewModel(),reviewSourceSetView.getModelElement(),false));
			menuManager.add(new ImportProjectForSourcesetAction(reviewSourceSetView,viewer));
		}
		menuManager.add(new RefreshViewerAction(viewer));
		menuManager.add(new Action("Reload configuration"){
			@Override
			public void run() {
				loadConfiguration();
			}
		});
	}
	
	private IMenuManager createFilters() {
		MenuManager m = new MenuManager("Filter");
		MenuManager subMenu = new MenuManager("User");
		m.add(subMenu);
		Set<String> users =  new HashSet<String>(getReviewInstance().getModel().getUsers());
		users.add(FilterByUser.NONE_USER);
		for (String u : users){
			usersFilter.enableUser(u, Preferences.getFilterStatus(u));
			subMenu.add(new UserFilterOption(u));
		}
		subMenu = new MenuManager("Source set");
		m.add(subMenu);
		for (String u : getReviewInstance().getModel().sourcesets.keySet()){
			sourceSetFilter.enableSourceSet(u, Preferences.getFilterStatus(u));
			subMenu.add(new SourceSetFilterOption(u));
		}
		subMenu = new MenuManager("Review annotation");
		m.add(subMenu);
		for (EReviewAnnotation a : EReviewAnnotation.values()){
			annotFilter.enableAnnotation(a.toString(), Preferences.getFilterStatus(a.toString()));
			subMenu.add(new ReviewAnnotationFilterOption(a.toString()));
		}
		annotFilter.enableAnnotation(FilterByReviewAnnotation.NOT_REVIEWED, Preferences.getFilterStatus(FilterByReviewAnnotation.NOT_REVIEWED));
		subMenu.add(new ReviewAnnotationFilterOption(FilterByReviewAnnotation.NOT_REVIEWED));
		subMenu = new MenuManager("Review status");
		m.add(subMenu);
		for (ReviewStatus a : ReviewStatus.values()){
			statusFilter.enableStatus(a, Preferences.getFilterStatus(a.toString()));
			subMenu.add(new ReviewStatusFilterOption(a.toString()));
		}
		return m;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	protected void reviewModelChanged() {
		if (getReviewInstance() != null){
			//must be called to update viewer filters
			createFilters();
		}
		viewer.refresh();
	}

}
