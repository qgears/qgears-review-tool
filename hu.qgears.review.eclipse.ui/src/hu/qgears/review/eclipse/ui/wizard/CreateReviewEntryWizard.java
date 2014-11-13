package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.UtilSha1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.wizard.Wizard;

/**
 * {@link Wizard} for creating a new {@link ReviewEntry}.
 * 
 * @author agostoni
 * @see ReviewEntryDetailsPage
 * @see ReviewEntryDetailsOptionalPage
 */
public class CreateReviewEntryWizard extends Wizard {

	private ReviewEntryDetailsPage redp;
	private ReviewSource targetSource;
	
	private ReviewEntry newReviewEntry;
	private ReviewEntryDetailsOptionalPage redop;
	private final ReviewModel reviewModel;
	
	public CreateReviewEntryWizard(ReviewSource targetSource, ReviewModel reviewModel) {
		super();
		this.targetSource = targetSource;
		this.reviewModel = reviewModel;
		setWindowTitle("New review for "+targetSource.getFullyQualifiedJavaName());
	}

	@Override
	public boolean performFinish() {
		File f=targetSource.getFileInWorkingCopy();
		byte[] content;
		try {
			content = UtilFile.loadFile(f);
			String sha1=UtilSha1.getSHA1(content);
			newReviewEntry=new ReviewEntry(
					targetSource.getSourceFolderId(),
					targetSource.getSourceFolderUrl(),
					targetSource.getFolderVersion(),
					sha1,
					targetSource.getFileVersion(), 
					targetSource.getSourceUrl(), 
					redp.getComment(),
					redp.getAnnotationType(), redop.getUser(), "all", redop.getTimeStamp(),
					redp.getInvalidates());
		} catch (IOException e) {
			UtilLog.showErrorDialog("Cannot load source file from workspace "+f.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	@Override
	public void addPages() {
		Collection<ReviewEntry> existingEntries = reviewModel.getReviewEntryByUrl().getMappedObjects(targetSource.getSourceUrl());
		Collection<ReviewEntry> nonInvalidatedEntries = new ArrayList<ReviewEntry>();
		for (ReviewEntry e : existingEntries){
			if (!reviewModel.isInvalidated(e.getSha1Sum())){
				nonInvalidatedEntries.add(e);
			}
		}
		addPage(redp = new ReviewEntryDetailsPage(nonInvalidatedEntries));
		addPage(redop = new ReviewEntryDetailsOptionalPage());
	}
	
	public ReviewEntry getNewReviewEntry() {
		return newReviewEntry;
	}
}
