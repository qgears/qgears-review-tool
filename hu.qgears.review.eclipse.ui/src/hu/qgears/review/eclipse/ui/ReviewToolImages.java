package hu.qgears.review.eclipse.ui;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Utility class for accessing images contained by this bundle.
 * 
 * @author agostoni
 * 
 */
public final class ReviewToolImages {

	public static final String DECORATION_REVIEW_OK = "review_ok.png";
	public static final String DECORATION_REVIEW_OK_2 = "review_ok_2.png";
	public static final String DECORATION_REVIEW_TODO = "review_todo.png";
	public static final String DECORATION_REVIEW_OLD = "review_old.png";
	public static final String DECORATION_REVIEW_MISSING = "review_missing.png";
	public static final String DECORATION_REVIEW_OFF = "review_off.png";
	public static final String ICON_REFRESH = "refresh.png";
	
	private ReviewToolImages() {}

	public static void init(ImageRegistry reg) {
		addIcon(reg,DECORATION_REVIEW_OK);
		addIcon(reg,DECORATION_REVIEW_OK_2);
		addIcon(reg,DECORATION_REVIEW_TODO);
		addIcon(reg,DECORATION_REVIEW_MISSING);
		addIcon(reg,DECORATION_REVIEW_OFF);
		addIcon(reg,DECORATION_REVIEW_OLD);
		addIcon(reg,ICON_REFRESH);
	}

	private static void addIcon(ImageRegistry reg, String icon) {
		reg.put(icon, AbstractUIPlugin.imageDescriptorFromPlugin(ReviewToolUI.PLUGIN_ID, "icons/"+icon));
	}
	
	public static Image getImage(String id){
		return ReviewToolUI.getDefault().getImageRegistry().get(id);
	}
	public static ImageDescriptor getImageDescriptor(String id){
		return ReviewToolUI.getDefault().getImageRegistry().getDescriptor(id);
	}
	
}
