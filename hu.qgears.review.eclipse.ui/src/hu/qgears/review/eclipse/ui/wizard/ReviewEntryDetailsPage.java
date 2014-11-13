package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for gathering required parameters for a new {@link ReviewEntry}.
 * 
 * @author agostoni
 * 
 */
public class ReviewEntryDetailsPage extends WizardPage {

	private static final String PAGE_NAME = "Review entry details";
	private Composite container;
	private Text commentText;
	private Combo annotationType;
	private CheckboxTableViewer table;
	private final Collection<ReviewEntry> existingEntries;

	protected ReviewEntryDetailsPage(Collection<ReviewEntry> existingEntries) {
		super(PAGE_NAME);
		this.existingEntries = existingEntries;
		setTitle(PAGE_NAME);
		setDescription("Select annotation type, and type a comment!");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		int nrOfColumns = 2;
		layout.numColumns = nrOfColumns;
		{
			new Label(container,SWT.NONE).setText("Annotation type");
			annotationType = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
			annotationType.add("Please select");
			for (EReviewAnnotation a : EReviewAnnotation.values()){
				annotationType.add(a.toString());
			}
			annotationType.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					checkPageComplete();
				}
			});
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			annotationType.setLayoutData(gd);
		}
		{
			//Comment section
			Label label1 = new Label(container, SWT.NONE);
			label1.setText("Comment ");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = nrOfColumns;
			label1.setLayoutData(gd);
			commentText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			commentText.setText("");
			commentText.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					checkPageComplete();
				}
	
	
			});
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 150;
			gd.widthHint = 500;
			gd.horizontalSpan = nrOfColumns;
			commentText.setLayoutData(gd);
		}
		{
			//invalidates Section
			Label label1 = new Label(container, SWT.NONE);
			label1.setText("Invalidates");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = nrOfColumns;
			label1.setLayoutData(gd);
			table = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			String [] cols = new String []{"Review","Comment"};
			for (String s : cols){
				TableColumn column = new TableViewerColumn(table, SWT.NONE).getColumn();
				column.setWidth(150);
				column.setResizable(true);
				column.setText(s);
			}
			table.getTable().setHeaderVisible(true);
			table.setContentProvider(ArrayContentProvider.getInstance());
			table.setLabelProvider(new ReviewEntryLabelProvider());
			table.setInput(existingEntries.toArray());
			gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = nrOfColumns;
			table.getTable().setLayoutData(gd);
		}
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);

	}
	protected void checkPageComplete() {
		boolean complete = true;
		if (getAnnotationType() == null){
			setErrorMessage("Annotation type is empty!");
			complete = false;
		}
		if (getComment() == null || getComment().isEmpty()){
			if (getAnnotationType() != null &&getAnnotationType().equals(EReviewAnnotation.reviewOk)){
				setMessage("Comment field is empty!", WARNING);
			} else {
				setErrorMessage("Comment field is empty!");
				complete = false;
			}
			
		} else {
			setMessage(null, WARNING);
		}
		if (complete){
			setErrorMessage(null);
		}
		setPageComplete(complete);
	}
	
	public String getComment(){
		return commentText.getText();
	}
	
	public EReviewAnnotation getAnnotationType() {
		int index = annotationType.getSelectionIndex();
		if (index >= 0 && index < annotationType.getItemCount()){
			String selected = annotationType.getItems()[index];
			for (EReviewAnnotation a :EReviewAnnotation.values()){
				if (a.toString().equals(selected)){
					return a;
				}
			}
		}
		return null;
	}
	
	public List<String> getInvalidates(){
		List<String> inv = new ArrayList<String>();
		for (Object o : table.getCheckedElements()){
			if (o instanceof ReviewEntry){
				inv.add(((ReviewEntry) o).getSha1Sum());
			}
		}
		return inv;
	}
	
	protected CheckboxTableViewer getTable() {
		return table;
	}
	
	protected Text getCommentText() {
		return commentText;
	}
	
	protected Combo getAnnotationTypeCombo(){
		return annotationType;
	}
	
	protected Collection<ReviewEntry> getExistingEntries() {
		return existingEntries;
	}
}
