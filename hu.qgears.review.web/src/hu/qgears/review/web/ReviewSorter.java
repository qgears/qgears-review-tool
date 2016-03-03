package hu.qgears.review.web;

import hu.qgears.commons.MultiMap;
import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReviewSorter {
	public MultiMap<EReviewAnnotation, ReviewEntry> current=new MultiMapHashImpl<EReviewAnnotation, ReviewEntry>();
	public MultiMap<EReviewAnnotation, ReviewEntry> old=new MultiMapHashImpl<EReviewAnnotation, ReviewEntry>();
	public MultiMap<EReviewAnnotation, ReviewEntry> notInvalid=new MultiMapHashImpl<EReviewAnnotation, ReviewEntry>();
	public MultiMap<EReviewAnnotation, ReviewEntry> invalid=new MultiMapHashImpl<EReviewAnnotation, ReviewEntry>();
	public List<ReviewEntry> currentList=new ArrayList<ReviewEntry>();
	public List<ReviewEntry> oldList=new ArrayList<ReviewEntry>();
	public ReviewSource source;
	public ReviewSorter(ReviewSource rs, ReviewInstance instance)
	{
		this.source=rs;
		Collection<ReviewEntry> entries=instance.getModel().getReviewEntryByUrl().getMappedObjects(rs.getSourceUrl());
		for(ReviewEntry e:entries)
		{
			if(!instance.getModel().isInvalidated(e.getSha1Sum()))
			{
				if(e.matches(rs))
				{
					current.putSingle(e.getAnnotation(), e);
					currentList.add(e);
				}
				else
				{
					// Old matching review
					old.putSingle(e.getAnnotation(), e);
					oldList.add(e);
				}
				notInvalid.putSingle(e.getAnnotation(), e);
			}else
			{
				invalid.putSingle(e.getAnnotation(), e);
			}
		}
	}
	public static Map<String, ReviewSorter> parseAll(ReviewInstance instance, List<String> files)
	{
		Map<String, ReviewSorter> ret=new TreeMap<String, ReviewSorter>();
		for(String fileid: files)
		{
			ReviewSource rs=instance.getModel().getSource(fileid);
			ret.put(fileid, new ReviewSorter(rs, instance));
		}
		return ret;
	}
}
