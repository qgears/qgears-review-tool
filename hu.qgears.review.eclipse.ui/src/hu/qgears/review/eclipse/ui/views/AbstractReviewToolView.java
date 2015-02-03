package hu.qgears.review.eclipse.ui.views;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEventListener;
import hu.qgears.review.eclipse.ui.views.main.ReviewToolMainView;
import hu.qgears.review.eclipse.ui.views.properties.ReviewToolPropertyPage;
import hu.qgears.review.model.ModelChangedEvent;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * Abstract base class for Review Tool Eclipse views. Provides methods for
 * accessing current {@link ReviewInstance}, and fires events on model changes.
 * <p>
 * The property sheet, and context menus is also registered via this class.
 * <p>
 * Finally it defines some commonly used utility methods.
 * 
 * @author agostoni
 * 
 */
public abstract class AbstractReviewToolView extends ViewPart {

	private final class ModelChangeListener implements
			UtilEventListener<ModelChangedEvent> {
		@Override
		public void eventHappened(ModelChangedEvent msg) {
			reviewModelChanged();
		}
	}
	
	private final class ReviewInstanceListener implements UtilEventListener<Pair<ReviewInstance, ReviewInstance>> {
		@Override
		public void eventHappened(Pair<ReviewInstance, ReviewInstance> msg) {
			reviewInstanceUpdated(msg.getA(),msg.getB());
		}
	}

	private static ReviewInstanceManager manager = new ReviewInstanceManager();
	private ReviewToolPropertyPage propertySheetPage;
	private UtilEventListener<ModelChangedEvent> modelChangeListener;
	private ReviewInstanceListener reviewInstanceListener ;

	@Override
	public void init(IViewSite site) throws PartInitException {
		reviewInstanceListener = new ReviewInstanceListener();
		modelChangeListener = new ModelChangeListener();
		manager.getReviewInstanceChangedEvent().addListener(reviewInstanceListener);
		super.init(site);
	}
	
	@Override
	public void dispose() {
		manager.getReviewInstanceChangedEvent().removeListener(reviewInstanceListener);
	}
	
	/**
	 * Installs model change listener on new review instance, and removes it
	 * from old instance.
	 * 
	 * @param oldRi
	 *            the old {@link ReviewInstance} that was overridden. May be
	 *            <code>null</code>
	 * @param newRi
	 *            the new {@link ReviewInstance} that has been set in manager.
	 *            May be <code>null</code>
	 */
	private void reviewInstanceUpdated(ReviewInstance oldRi, ReviewInstance newRi){
		if (oldRi != null){
			oldRi.getModel().getReviewModelChangedEvent().removeListener(modelChangeListener);
		}
		if (newRi != null){
			newRi.getModel().getReviewModelChangedEvent().addListener(modelChangeListener);
		}
		reviewModelChanged();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter.equals(IPropertySheetPage.class)) {
			if (propertySheetPage == null) {
				//registering property sheet page
				propertySheetPage = new ReviewToolPropertyPage();
			}
			return propertySheetPage;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the current {@link ReviewInstance} or <code>null</code> if it
	 * hasn't been initialized yet.
	 * 
	 * @return
	 */
	protected final ReviewInstance getReviewInstance() {
		return manager.getReviewInstace();
	}

	/**
	 * Set a new {@link ReviewInstance} by passing it to
	 * {@link ReviewInstanceManager}. This review instance will be the new model
	 * root.
	 * <p>
	 * Calling this method will trigger the calling of
	 * {@link #reviewModelChanged()} method in all Review Tool view, so they can
	 * be updated automatically.
	 * <p>
	 * This method should be called only by {@link ReviewToolMainView}, because
	 * this view is the primary source of review model.
	 * 
	 * @param newRi
	 */
	protected final void setReviewInstance(ReviewInstance newRi){
		manager.setReviewInstace(newRi);
	}

	/**
	 * Returns the first element from specified {@link ISelection}. Returns
	 * <code>null</code> if selection is empty.
	 * 
	 * @param s
	 *            the {@link ISelection} to parse.
	 * @return
	 */
	protected final Object getSingleSelection(ISelection s){
		if (s != null && !s.isEmpty() && s instanceof StructuredSelection){
			return ((StructuredSelection)s).getFirstElement();
		}
		return null;
	}
	
	/**
	 * Returns the list of selected elements from specified {@link ISelection}.
	 * Returns an empty list, if selection is empty.
	 * 
	 * @param s the {@link ISelection} to parse.
	 * @return
	 */
	protected final List<Object> getSelection(ISelection s){
		if (s != null && !s.isEmpty() && s instanceof StructuredSelection){
			return Arrays.asList(((StructuredSelection)s).toArray());
		}
		return Collections.emptyList();
	}

	/**
	 * Initializes context menu on specified viewer, and the view menu (The same
	 * actions will be loaded into both containers). The menu elements must be
	 * specified in by subclasses in {@link #fillMenuManager(IMenuManager)}
	 * method.
	 * <p>
	 * This method must be called from
	 * {@link #createPartControl(org.eclipse.swt.widgets.Composite)} method!
	 * 
	 * @param viewer
	 *            the target viewer
	 */
	protected final void createContextMenus(Viewer viewer) {
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
	
	/**
	 * Subclasses must load the menu elements into given {@link IMenuManager}
	 * ,that are enabled for this view.
	 * <p>
	 * Note that the menus are recreated before each menu activation, and will
	 * be re-populated by calling this method. This way the specified actions
	 * may depend on current selection and similar inner state variables.
	 * 
	 * @param manager
	 *            The manager that stores menus
	 * @see #createContextMenus(Viewer)
	 */
	protected abstract void fillMenuManager(IMenuManager manager);

	/**
	 * Callback method that is called automatically in following cases:
	 * <li>{@link ReviewInstance} is reloaded
	 * <li>{@link ReviewModel#getReviewModelChangedEvent() Review model} has been changed.
	 * 
	 * <p>
	 * Implement GUI updates here.
	 */
	protected abstract void reviewModelChanged();
	
	
}
