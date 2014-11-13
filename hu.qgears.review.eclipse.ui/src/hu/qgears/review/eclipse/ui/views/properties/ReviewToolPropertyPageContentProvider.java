package hu.qgears.review.eclipse.ui.views.properties;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.report.ReviewStatsSummary;
import hu.qgears.review.report.ReviewStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider of review tool property page.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolPropertyPageContentProvider implements ITreeContentProvider {
	
	private static final String PERCENTAGE_TEMPLATE = "%.2f %%";

	private class Group {
		private List<Object> children = new ArrayList<Object>();
		private String name;
		
		public Group(String name) {
			super();
			this.name = name;
		}

		private List<Object> getChildren(){
			return children;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	public class PropertyPageContent{
		
		private String name;
		private String value;
		private Object parent;
		
		public PropertyPageContent(String name,String value) {
			this.name = name;
			this.value = value;
		}

		public String getPropertyName() {
			return name;
		}
		
		public String getPropertyValue(){
			return value;
		}
		
		public Object getParent(){
			return parent;
		}
		
		public void setParent(Object parent) {
			this.parent = parent;
		}
	}
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	private Object[] createReviewEntryProperties(ReviewEntry element) {
		DateFormat f = SimpleDateFormat.getDateTimeInstance();
		Object [] props = new Object[]{
				new PropertyPageContent("Comment",element.getComment()),
				new PropertyPageContent("File version",element.getFileVersion()),
				new PropertyPageContent("User",element.getUser()),
				new PropertyPageContent("File sha1 sum",element.getFileSha1sum()),
				new PropertyPageContent("File URL",element.getFileUrl()),
				new PropertyPageContent("Sha1 sum",element.getSha1Sum()),
				new PropertyPageContent("Timestamp",f.format(new Date(element.getDate()))),
				new PropertyPageContent("Invalidates",element.getInvalidates().toString())
		};
		
		return props;
	}

	private Object[] createReviewSourceProperties(ReviewSource element) {
		return new Object[]{
				new PropertyPageContent("Model Url",element.modelUrl()),
				new PropertyPageContent("File version",element.getFileVersion()),
				new PropertyPageContent("FQ Java name",element.getFullyQualifiedJavaName()),
				new PropertyPageContent("SHA1 sum",element.getSha1()),
				new PropertyPageContent("Source folder id",element.getSourceFolderId()),
				new PropertyPageContent("Source folder URL",element.getSourceFolderUrl()),
				new PropertyPageContent("Source URL",element.getSourceUrl()),
				new PropertyPageContent("File in working copy",element.getFileInWorkingCopy().getAbsolutePath())
		};
	}

	private Object[] createReviewStatsSummaryProperties(ReviewStatsSummary element) {
		List<Object> props = new ArrayList<Object>();
		props.add(new PropertyPageContent("Overall progress",String.format(PERCENTAGE_TEMPLATE, element.getOverallProgress())));
		Group g = new Group("Reviews");
		props.add(g);
		for (Entry<ReviewStatus, Integer> review : element.getReviewStatusSummary().entrySet()){
			PropertyPageContent p = new PropertyPageContent(
					review.getKey() +" (avg)",
					String.format(PERCENTAGE_TEMPLATE,element.asPercentage(review.getValue())));
			p.setParent(g);
			g.getChildren().add(
			p);
			
		}
		g = new Group("Metrics");
		props.add(g);
		for (Entry<String, Float> metric : element.getMetricsAVGs().entrySet()){
			PropertyPageContent p = new PropertyPageContent(metric.getKey() +" (avg)",
					String.format(PERCENTAGE_TEMPLATE, metric.getValue()));
			p.setParent(g);
			g.getChildren().add(p);
		}
		return props.toArray();
	}
	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof SourceTreeElement){
			return createReviewSourceProperties(((SourceTreeElement)element).getSource());
		}
		if (element instanceof ReviewEntryView){
			return createReviewEntryProperties(((ReviewEntryView)element).getModelElement());
		}
		if (element instanceof ReviewStatsSummary){
			return createReviewStatsSummaryProperties((ReviewStatsSummary)element);
		}
		if (element instanceof Group){
			return ((Group) element).getChildren().toArray();
		}
		return new Object[]{};
	}


	@Override
	public Object getParent(Object element) {
		if (element instanceof PropertyPageContent){
			return ((PropertyPageContent) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

}
