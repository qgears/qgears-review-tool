package hu.qgears.review.model;

import hu.qgears.commons.MultiMap;
import hu.qgears.commons.MultiMapHashImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Computes the progress statistics of a reviewer over a specific
 * {@link ReviewSourceSet}.
 * 
 * @author agostoni
 * 
 */
public class ReviewProgress {

	private static Comparator<? super ReviewEntry> orderByDate = new Comparator<ReviewEntry>() {

		@Override
		public int compare(ReviewEntry o1, ReviewEntry o2) {
			long diff = o1.getDate() - o2.getDate();
			//descending order
			return diff > 0 ? -1 : (diff < 0 ? 1 :0 );
		}
	};
	
	private ReviewProgress(){
		//disable ctor, use factory method instead
	}
	private ReviewSourceSet targetSourceSet;
	private ReviewModel model;
	private String user;
	
	private MultiMap<EReviewAnnotation,ReviewEntry> reviewEntries = new MultiMapHashImpl<EReviewAnnotation, ReviewEntry>();
	
	public int getReviewEntryCount(EReviewAnnotation annotation, boolean old){
		Collection<ReviewEntry> res = reviewEntries.get(annotation);
		int count = 0;
		for (ReviewEntry r : res){
			ReviewSource src = model.getSource(r.getFullUrl());
			if (!old && r.matches(src)){
				count++;
			} else if (old &&  r.matchesPrevious(src)){
				count++;
			}
		}
		return count;
	}
	
	public int getOverallReviewEntryCount(boolean old){
		int sum = 0;
		for (EReviewAnnotation a: EReviewAnnotation.values()){
			sum += getReviewEntryCount(a, old);
		}
		return sum;
	}
	
	public int getMissingReviewEntryCount(boolean old){
		if (old){
			return 0;
		} else {
			return targetSourceSet.sourceFiles.size() - getOverallReviewEntryCount(true) - getOverallReviewEntryCount(false);
		}
	}
	
	public String getUser() {
		return user;
	}

	public static ReviewProgress create(ReviewModel model,ReviewSourceSet targetSourceSet, String user){
		ReviewProgress p = new ReviewProgress();
		p.targetSourceSet = targetSourceSet;
		p.user = user;
		p.model = model;
		for (String s : targetSourceSet.sourceFiles){
			ReviewSource src = model.getSource(s);
			List<ReviewEntry> entries = new ArrayList<ReviewEntry>();
			
			for (ReviewEntry re : src.getMatchingReviewEntries(model)){
				if (re.getUser().equals(user)){
					entries.add(re);
				}
			}
			if (entries.isEmpty()){
				for (ReviewEntry re : src.getMatchingReviewEntriesPreviousVersion(model)){
					if (re.getUser().equals(user)){
						entries.add(re);
					}
				}
			}
			if (entries.size() >0){
				Collections.sort(entries,orderByDate);
				p.reviewEntries.putSingle(entries.get(0).getAnnotation(), entries.get(0));
			}
		}
		return p;
	}
}
