package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.ReviewEntry;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.widgets.Composite;

/**
 * Shows the optional properties of an existing {@link ReviewEntry} in read only
 * mode.
 * 
 * @author agostoni
 * 
 */
public class ReviewEntryDetailsOptionalPageReadOnly extends ReviewEntryDetailsOptionalPage{

	
	private ReviewEntry reviewEntry;

	public ReviewEntryDetailsOptionalPageReadOnly(ReviewEntry reviewEntry) {
		super();
		this.reviewEntry = reviewEntry;
		setDescription(null);
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getUserNameText().setText(reviewEntry.getUser());
		long timeStamp = reviewEntry.getDate();
		Date dat = new Date(timeStamp);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dat);
		getTimeStampDate().setDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
		getTimestampTime().setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	
		getUserNameText().setEditable(false);
		getTimeStampDate().setEnabled(false);
		getTimestampTime().setEnabled(false);
		checkPageComplete();
	}
	
	
	@Override
	protected void checkPageComplete() {
		setPageComplete(true);
	}
}
