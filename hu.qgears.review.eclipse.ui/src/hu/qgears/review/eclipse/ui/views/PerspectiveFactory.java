package hu.qgears.review.eclipse.ui.views;

import hu.qgears.review.eclipse.ui.ReviewToolUI;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Default perspective for Review tool UI.
 * 
 * @author agostoni
 *
 */
public class PerspectiveFactory implements IPerspectiveFactory {

	private static final String CENTER_RIGHT = "center right";
	private static final String TOP_RIGHT = "top right";
	private static final String BOTTOM_RIGHT = "bottom right";
	private static final String BOTTOM_LEFT = "bottomLeft";
	private static final String TOP_LEFT = "topLeft";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();

		// Top left: Package Explorer view
		IFolderLayout topLeft = layout.createFolder(TOP_LEFT,
				IPageLayout.LEFT, 0.20f, editorArea);
		topLeft.addView(ReviewToolUI.ID_MAIN_VIEW);
		topLeft.addView(JavaUI.ID_PACKAGES);

		
		IFolderLayout bottomLeft = layout.createFolder(BOTTOM_LEFT, IPageLayout.BOTTOM, 0.66f, editorArea);
		bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

		IFolderLayout bottomRight = layout.createFolder(BOTTOM_RIGHT, IPageLayout.RIGHT, 0.5f, BOTTOM_LEFT);
		bottomRight.addView(ReviewToolUI.ID_STATISTICS_VIEW);

		IFolderLayout topRight = layout.createFolder(TOP_RIGHT, IPageLayout.RIGHT, 0.75f, editorArea);
		topRight.addView(IPageLayout.ID_OUTLINE);
		
		IFolderLayout centerRight = layout.createFolder(CENTER_RIGHT, IPageLayout.BOTTOM, 0.5f, TOP_RIGHT);
		centerRight.addView(ReviewToolUI.ID_TODO_LIST_VIEW);
	}

}
