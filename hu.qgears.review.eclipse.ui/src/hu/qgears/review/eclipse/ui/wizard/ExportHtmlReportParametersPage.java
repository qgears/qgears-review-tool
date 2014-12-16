package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.commons.UtilEventListener;
import hu.qgears.review.eclipse.ui.util.Preferences;
import hu.qgears.review.eclipse.ui.util.TwoPaneListSelector;
import hu.qgears.review.report.ColumnDefinition;
import hu.qgears.review.report.ReportGenerator;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page that contains editors for the parameters of HTML report
 * generator. All public methods must be accessed before the parent wizard has
 * been closed.
 * 
 * @author agostoni
 * 
 */
public class ExportHtmlReportParametersPage extends WizardPage {

	private static final String TITLE = "Specify HTML export parameters";
	private Text targetFilePath;

	private ReportGenerator theGenerator;
	private Button generateStyle;
	private Button orderDirection;
	private TwoPaneListSelector<ColumnDefinition> viewer;
	private ComboViewer orderByCombo;
	private Button generateReviewStats;
	private Button generateSonarStats;
	
	protected ExportHtmlReportParametersPage(ReportGenerator rg) {
		super(TITLE);
		setTitle(TITLE);
		theGenerator = rg;
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(1,true));

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group targetFolder = new Group(container, SWT.BORDER);
		targetFolder.setText("Target file");
		createTargetFolderEditor(targetFolder);
		targetFolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group columnSelection = new Group(container, SWT.BORDER);
		createcolumnSelectionEditor(columnSelection);
		columnSelection.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group orderByGroup = new Group(container, SWT.BORDER);
		createOrderByEditor(orderByGroup);
		orderByGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setControl(container);
		checkPageComplete();
	}

	private void createOrderByEditor(Group orderByGroup) {
		orderByGroup.setLayout(new GridLayout(2,false));
		orderByGroup.setText("Order by");
		
		orderByCombo = new ComboViewer(orderByGroup,SWT.SINGLE|SWT.READ_ONLY);
		orderByCombo.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		orderByCombo.setContentProvider(ArrayContentProvider.getInstance());
		orderByCombo.setLabelProvider(createLabelProvider());
		updateOrderByComboContent();
		orderByCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				checkPageComplete();
			}
		});
		orderDirection = new Button(orderByGroup, SWT.CHECK);
		orderDirection.setText("Ascending");
		
	}

	private void createcolumnSelectionEditor(Group columnSelection) {
		columnSelection.setLayout(new GridLayout(1,false));
		columnSelection.setText("Select columns to export, and specify order");
		ILabelProvider labelProvider = createLabelProvider();
		viewer = new TwoPaneListSelector<ColumnDefinition>(columnSelection, SWT.NONE, ColumnDefinition.class);
		List<ColumnDefinition> allDefs = theGenerator.getColumnDefinitions();
		viewer.setInput(allDefs,allDefs);
		viewer.setLabelProvider(labelProvider);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getSelectionChangedEvent().addListener(new UtilEventListener<Object>() {
			@Override
			public void eventHappened(Object msg) {
				updateOrderByComboContent();
				checkPageComplete();
			}
		});
	}

	private ILabelProvider createLabelProvider() {
		ILabelProvider labelProvider = new LabelProvider(){
			@Override
			public String getText(Object element) {
				if(element instanceof ColumnDefinition){
					return ((ColumnDefinition) element).getTitle();
				}
				return null;
			}
		};
		return labelProvider;
	}

	private void updateOrderByComboContent() {
		List<ColumnDefinition> newSel = getSelectedColumnDefinitions();
		ColumnDefinition currentOrderBy = getOrderByColumn();
		orderByCombo.setInput(newSel);
		if (currentOrderBy != null && newSel.contains(currentOrderBy)){
			orderByCombo.setSelection(new StructuredSelection(currentOrderBy));
		} else if (!newSel.isEmpty()){
			orderByCombo.setSelection(new StructuredSelection(newSel.get(0)));
		} else {
			orderByCombo.setSelection(StructuredSelection.EMPTY);
		}
	}
	
	private void createTargetFolderEditor(Composite targetFolder) {
		int columns = 3;
		
		targetFolder.setLayout(new GridLayout(columns,false));
		new Label(targetFolder,SWT.NONE).setText("Select target file :");
		targetFilePath = new Text(targetFolder,SWT.BORDER);
		final String defultPath = Preferences.getDefaultReportPath()+File.separator+theGenerator.getTargetSourceSet().id+".html";
		targetFilePath.setText(defultPath);
		targetFilePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetFilePath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPageComplete();
			}
		});
		Button browse = new Button(targetFolder, SWT.PUSH);
		browse.setText("Browse...");
		
		browse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = openDirectoryDialog(defultPath);
				if (path != null){
					targetFilePath.setText(path);
				}
				checkPageComplete();
			}
		});
		browse.setLayoutData(new GridData());
		
		generateStyle = new Button(targetFolder, SWT.CHECK);
		generateStyle.setText("Generate CSS");
		generateStyle.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,columns,1));
		generateStyle.setSelection(true);
		
		generateReviewStats = new Button(targetFolder, SWT.CHECK);
		generateReviewStats.setText("Generate review summary");
		generateReviewStats.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,columns,1));
		generateSonarStats = new Button(targetFolder, SWT.CHECK);
		generateSonarStats.setText("Generate SONAR metric summary");
		generateSonarStats.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,columns,1));
	}

	
	private String openDirectoryDialog(String defultPath){
		FileDialog fd = new FileDialog(getShell(),
				SWT.SAVE);
		fd.setText(TITLE);
		fd.setFileName(defultPath);
		return fd.open();
	}

	protected void checkPageComplete(){
		boolean complete = true;
		
		if (getOrderByColumn() == null){
			setErrorMessage("Ordering is not specified!");
			complete = false;
		}
		if (getSelectedColumnDefinitions().isEmpty()){
			setErrorMessage("At least one column must be selected!");
			complete = false;
		}
		if (getTargetFilePath() == null || getTargetFilePath().isEmpty()){
			setErrorMessage("Target file path is empty!");
			complete = false;
		}
		if (complete){
			setErrorMessage(null);
		}
		setPageComplete(complete);
	}
	
	/**
	 * Returns the {@link ColumnDefinition}s that the user has selected. The
	 * order of the elements must be considered by generator template.
	 * 
	 * @return
	 */
	public List<ColumnDefinition> getSelectedColumnDefinitions() {
		return viewer.getSelectedElements();
	}

	/**
	 * Returns the ordering direction : <code>true</code> for ascending,
	 * <code>false</code> for descending.
	 * 
	 * @return
	 */
	public boolean getOrderByDirection() {
		return orderDirection.getSelection();
	}

	/**
	 * See {@link ExportHtmlReportWizard#mustGenerateCss()}.
	 * 
	 * @return
	 */
	public boolean getGenerateCSS() {
		return generateStyle.getSelection();
	}

	/**
	 * See {@link ExportHtmlReportWizard#getTargetFile()}
	 * 
	 * @return
	 */
	public String getTargetFilePath() {
		return targetFilePath.getText();
	}
	/**
	 * The output must be ordered by this column.
	 * 
	 * @return
	 */
	public ColumnDefinition getOrderByColumn(){
		ISelection sel = orderByCombo.getSelection();
		if (sel instanceof StructuredSelection){
			return (ColumnDefinition) ((StructuredSelection) sel).getFirstElement();
		}
		return null;
	}
	
	/**
	 * See {@link ExportHtmlReportWizard#mustGenerateReviewStats()}.
	 * 
	 * @return
	 */
	public boolean mustGenerateReviewStats(){
		return generateReviewStats.getSelection();
	}
	/**
	 * See {@link ExportHtmlReportWizard#mustGenerateSonarStats()}.
	 * 
	 * @return
	 */
	public boolean mustGenerateSonarStats(){
		return generateSonarStats.getSelection();
	}
}
