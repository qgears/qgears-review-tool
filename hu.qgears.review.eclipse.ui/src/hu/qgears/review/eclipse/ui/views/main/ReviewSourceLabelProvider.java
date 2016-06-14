package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.eclipse.ui.ReviewToolImages;
import hu.qgears.review.eclipse.ui.util.UtilWorkspace;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryGroup;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReviewStatus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ReviewSourceLabelProvider extends LabelProvider  implements ILabelDecorator, IColorProvider {

	private Map<ReviewStatus, Image> imageCache = new HashMap<ReviewStatus, Image>();
	@Override
	public String getText(Object element) {
		if (element instanceof SourceTreeElement){
			SourceTreeElement ste = (SourceTreeElement) element;
			return ste.getSource().getFullyQualifiedJavaName();
		}
		if (element instanceof ReviewEntryGroup){
			return ((ReviewEntryGroup) element).getName();
		}
		if (element instanceof ReviewEntryView){
			ReviewEntry re = ((ReviewEntryView) element).getModelElement();
			return re.getAnnotation() + " by "+re.getUser(); 
		}
		return element == null ? null :element.toString();
	}
	
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof SourceTreeElement){
			return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS);
		}
		if (element instanceof ReviewEntryGroup){
			
			ReviewEntryGroup vte = (ReviewEntryGroup) element;
			if (vte.getName().contains("Old") || vte.getName().contains("Invalid")){
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED_DISABLED);
			} else {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED);
			}
		}
		if (element instanceof ReviewEntryView){
			ReviewEntry re = ((ReviewEntryView) element).getModelElement();
			switch (re.getAnnotation()) {
				case reviewOff:
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
				case reviewOk:
					return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PUBLIC);
				case reviewTodo:
					return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PROTECTED);
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		if (element instanceof ReviewSourceSetView){
			ReviewSourceSetView reviewSourceSetView = (ReviewSourceSetView) element;
			if (reviewSourceSetView.getChildren().isEmpty()){
				return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_EMPTY_PACKAGE);
			} else {
				return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
			}
		}
		return null;
	}


	@Override
	public Image decorateImage(Image image, Object element) {
		if (element instanceof SourceTreeElement){
			SourceTreeElement ste = (SourceTreeElement) element;
			ReviewSource s = ste.getSource();
			ReviewStatus st = ReportGenerator.getReviewStatus(s, ste.getReviewModel());
			return getImageFor(image,st);
		}
		return null;
	}


	private Image getImageFor(Image image, ReviewStatus st) {
		if (!imageCache.containsKey(st)){
			ImageDescriptor imageDesc = null;
			switch (st){
			case MISSING:
				imageDesc = ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_MISSING);
				break;
			case OFF:
				imageDesc =ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_OFF);
				break;
			case OK_MORE_REVIEWERS:
				imageDesc = ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_OK);
				break;
			case OK_ONE_REVIEWER:
				imageDesc =ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_OK_2);
				break;
			case OLD:
				imageDesc =ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_OLD);
				break;
			case TODO:
				imageDesc =ReviewToolImages.getImageDescriptor(ReviewToolImages.DECORATION_REVIEW_TODO);
				break;
			}
			if (imageDesc != null){
				DecorationOverlayIcon icon = new DecorationOverlayIcon(
						image,imageDesc,IDecoration.BOTTOM_RIGHT);
				imageCache.put(st, icon.createImage());
			}
		}
		return imageCache.get(st);
	}



	@Override
	public String decorateText(String text, Object element) {
		if (missingSource(element)){
			return text + " [missing]"; 
		}
		return text;
	}
	
	private boolean missingSource(Object element){
		if (element instanceof SourceTreeElement){
			SourceTreeElement ste = (SourceTreeElement) element;
			File file = ste.getModelElement().getFileInWorkingCopy();
			IFile iFile = UtilWorkspace.getFileInWorkspace(file);
			if (iFile == null || !iFile.exists()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		for (Image i :imageCache.values()){
			if (!i.isDisposed()){
				i.dispose();
			}
		}
		imageCache.clear();
	}

	@Override
	public Color getForeground(Object element) {
		if (missingSource(element)){
			return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
		}
		return null;
	}


	@Override
	public Color getBackground(Object element) {
		if (missingSource(element)){
			return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
		}
		return null;
	}

}

