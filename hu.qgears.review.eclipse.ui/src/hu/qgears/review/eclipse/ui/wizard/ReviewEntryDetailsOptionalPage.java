package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.eclipse.ui.preferences.Preferences;
import hu.qgears.review.model.ReviewEntry;

import java.util.Calendar;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page that contains optional (or computed) parameters of a
 * {@link ReviewEntry}. User may override this values if needed.
 * 
 * @author agostoni
 * 
 */
public class ReviewEntryDetailsOptionalPage extends WizardPage {

	private static final String PAGE_NAME = "Review entry details (optional settings)";
	private Composite container;
	private DateTime timeStampDate;
	private DateTime timestampTime;
	private Text userNameText;

	protected ReviewEntryDetailsOptionalPage() {
		super(PAGE_NAME);
		setTitle(PAGE_NAME);
		setDescription("Modify default parameters if necessary!");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		{
			final Label timeStampLabel = new Label(container, SWT.NONE );
			timeStampLabel.setText("Timestamp ");
			timeStampDate = new DateTime(container, SWT.DATE | SWT.DROP_DOWN);
			SelectionListener timeStampUpdater = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					timeStampLabel.setText("Timestamp ("+getTimeStamp()+") " );
				}
			};
			timeStampDate.addSelectionListener(timeStampUpdater);
			timestampTime = new DateTime(container, SWT.TIME );
			timestampTime.addSelectionListener(timeStampUpdater);
			timeStampUpdater.widgetSelected(null);
		}
		new Label(container, SWT.NONE).setText("User :");
		userNameText = new Text(container,SWT.BORDER);
		userNameText.setText(Preferences.getDefaultUserName());
		userNameText.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				checkPageComplete();
			}

		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		userNameText.setLayoutData(gd);
		// required to avoid an error in the system
		setControl(container);
		checkPageComplete();
	}
	
	protected void checkPageComplete() {
		boolean complete = true;
		if (getUser() == null || getUser().isEmpty()){
			complete = false;
			setErrorMessage("User name must not be empty!");
		}
		if (complete){
			setErrorMessage(null);
		}
		setPageComplete(complete);
	}
	
	public long getTimeStamp(){
		Calendar instance = Calendar.getInstance();
		instance.set(Calendar.DAY_OF_MONTH, timeStampDate.getDay());
		instance.set(Calendar.MONTH, timeStampDate.getMonth());
		instance.set(Calendar.YEAR, timeStampDate.getYear());
		instance.set(Calendar.HOUR_OF_DAY, timestampTime.getHours());
		instance.set(Calendar.MINUTE, timestampTime.getMinutes());
		instance.set(Calendar.SECOND, timestampTime.getSeconds());
		return instance.getTimeInMillis();
	}
	
	public String getUser(){
		return userNameText.getText();
	}
	
	protected Text getUserNameText() {
		return userNameText;
	}
	
	protected DateTime getTimestampTime() {
		return timestampTime;
	}
	
	protected DateTime getTimeStampDate() {
		return timeStampDate;
	}
}
