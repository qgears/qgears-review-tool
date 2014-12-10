package hu.qgears.review.eclipse.ui.views.todolist;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.actions.CompareWithEachOtherAction;
import hu.qgears.review.eclipse.ui.actions.CompareWithHeadAction;
import hu.qgears.review.eclipse.ui.actions.CreateReviewEntryAction;
import hu.qgears.review.eclipse.ui.actions.OpenJavaTypeAction;
import hu.qgears.review.eclipse.ui.actions.OpenJavaTypeDoubleClickListener;
import hu.qgears.review.eclipse.ui.actions.OpenReviewEntryDetailsAction;
import hu.qgears.review.eclipse.ui.actions.OpenReviewEntryDetailsDoubleClickListener;
import hu.qgears.review.eclipse.ui.actions.RefreshViewerAction;
import hu.qgears.review.eclipse.ui.actions.filters.TodoListFilter;
import hu.qgears.review.eclipse.ui.util.Preferences;
import hu.qgears.review.eclipse.ui.views.main.LinkWithEditorSTEAction;
import hu.qgears.review.eclipse.ui.views.main.ReviewSourceContentProvier;
import hu.qgears.review.eclipse.ui.views.main.ReviewSourceLabelProvider;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.eclipse.ui.views.properties.ReviewToolPropertyPage;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * This view shows the {@link ReviewSource}s from selected
 * {@link ReviewSourceSet}, that haven't been reviewed by current user (
 * {@link Preferences#getDefaultUserName()}) yet.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolTodoListView extends ViewPart implements ISelectionListener{

	private TreeViewer viewer;
	private ReviewInstance reviewInstance;
	private ReviewToolPropertyPage propertySheetPage;
	private OpenJavaTypeAction openJavaTypeAction;
	private OpenReviewEntryDetailsAction openReviewDetailsAction;
	
	@Override
	public void createPartControl(Composite parent) {
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
		
		createMenus();
		createToolBar();
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	protected void createMenus() {
		MenuManager menu = new MenuManager();
		IMenuListener listener = new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillMenuManager(manager);
			}
		};
		menu.addMenuListener(listener);
		Menu m = menu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(m);
		getSite().registerContextMenu(menu, viewer);
		IMenuManager viewMenu = getViewSite().getActionBars().getMenuManager();
		viewMenu.addMenuListener(listener);
		viewMenu.setVisible(true);
		fillMenuManager(viewMenu);
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
	
	private void fillMenuManager (IMenuManager m) {
		//TODO code duplication with ReviewToolMainView: The same actions should be created here and there.
		m.setRemoveAllWhenShown(true);
		Object s = getSingleSelection();
		List<Object> selection = getSelection();
		if (s != null && s instanceof SourceTreeElement){
			m.add(openJavaTypeAction);
			CreateReviewEntryAction action = new CreateReviewEntryAction((SourceTreeElement) s, viewer, reviewInstance);
			m.add(action);
		}
		if (s instanceof ReviewEntryView){
			m.add(openReviewDetailsAction);
			m.add(new CompareWithHeadAction((ReviewEntryView) s));
			if (selection.size() == 2 && selection.get(1) instanceof ReviewEntryView){
				m.add(new CompareWithEachOtherAction((ReviewEntryView)s, (ReviewEntryView)selection.get(1)));
			}
		}
		m.add(new RefreshViewerAction(viewer));
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
				ReviewInstance riCandidate = (ReviewInstance) part.getAdapter(ReviewInstance.class);
				if (riCandidate != null){
					updateViewer((ReviewSourceSetView)sourceSetCandidate,riCandidate);
				}
			}
		}
	}

	private void updateViewer(ReviewSourceSetView sourceSetCandidate,
			ReviewInstance riCandidate) {
		if (riCandidate == null || sourceSetCandidate == null){
			viewer.setInput(null);
			viewer.refresh();
			setPartName("Select a review source set!");
		} else {
			if (this.reviewInstance == null || !this.reviewInstance.equals(riCandidate)){
				reviewInstance = riCandidate;
			}
			viewer.setInput(sourceSetCandidate);
			viewer.refresh();
			String id = sourceSetCandidate.getModelElement().id;
			setPartName("Todo list for "+getUserName()+ " in source set '"+id+"'");
		}
	}
	
	private String getUserName() {
		return Preferences.getDefaultUserName();
	}

	public Object getSingleSelection(){
		ISelection s = viewer.getSelection();
		if (s != null && !s.isEmpty() && s instanceof StructuredSelection){
			return ((StructuredSelection)s).getFirstElement();
		}
		return null;
	}
	public List<Object> getSelection(){
		ISelection s = viewer.getSelection();
		if (s != null && !s.isEmpty() && s instanceof StructuredSelection){
			return  Arrays.asList(((StructuredSelection)s).toArray());
		}
		return Collections.emptyList();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(IPropertySheetPage.class)){
			if(propertySheetPage==null)
			{
				propertySheetPage = 
					new ReviewToolPropertyPage();
			}
			return propertySheetPage;
		}
		if (adapter.equals(ReviewInstance.class)){
			return reviewInstance;
		}
		return null;
	}

}
