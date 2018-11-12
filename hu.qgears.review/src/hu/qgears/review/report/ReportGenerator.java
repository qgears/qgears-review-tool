package hu.qgears.review.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.util.IPropertyGetter;
import hu.qgears.review.util.IndexByProperty;
import hu.qgears.sonar.client.model.SonarResource;
import hu.qgears.sonar.client.model.SonarResourceScope;

/**
 * Collects SONAR coverage statistics and review progress statistics for a
 * {@link ReviewSourceSet}. Creates {@link ReportEntry}s for each source file in
 * source set, and provides {@link ColumnDefinition}s for accessing data within
 * entries. This data can be used for generate HTML-based or other user readable
 * documentation.
 * 
 * @author agostoni
 * 
 */
public class ReportGenerator {

	private static final String IT_BRANCH_COVERAGE = "it_branch_coverage";
	private static final String IT_LINE_COVERAGE = "it_line_coverage";
	private static final String UNIT_TEST_BRANCH_COVERAGE = "branch_coverage";
	private static final String UNIT_TEST_LINE_COVERAGE = "line_coverage";
	private static final String UNIT_TEST_COVERAGE = "coverage";
	private static final String OVERALL_COVERAGE = "overall_coverage";
	private static final String OVERALL_LINE_COVERAGE = "overall_line_coverage";
	private static final String OVERALL_BRANCH_COVERAGE = "overall_branch_coverage";
	private static final String IT_COVERAGE = "it_coverage";
	private ReviewSourceSet targetSourceSet;
	private final ReviewModel modelRoot;
	/**
	 * Java FQ classname based index from {@link SonarResource}s queried from SONAR.
	 */
	private IndexByProperty<SonarResource> sonarResources;
	private ColumnDefinition orderBy;
	private boolean orderAscendant;
	private List<ColumnDefinition> columnDefinitions;

	public ReportGenerator(ReviewModel modelRoot, ReviewSourceSet targetSourceSet) {
		super();
		this.modelRoot = modelRoot;
		this.targetSourceSet = targetSourceSet;
		columnDefinitions = getDefaultColumnDefinitions();
	}
	
	public List<ColumnDefinition> getColumnDefinitions() {
		return columnDefinitions;
	}
	
	public static List<ColumnDefinition> getDefaultColumnDefinitions() {
		List<ColumnDefinition> definitions = new ArrayList<ColumnDefinition>();
		definitions.add(new ClassNameColumnDefinition());
		definitions.add(new ReviewStatusColumnDefinition());
		definitions.add(new SonarMetricsColumnDefinition("Unit test coverage",UNIT_TEST_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("Unit test line coverage",UNIT_TEST_LINE_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("Unit test branch coverage",UNIT_TEST_BRANCH_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("IT covarage",IT_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("IT line coverage",IT_LINE_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("IT branch coverage",IT_BRANCH_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("Overall covarage",OVERALL_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("Overall line coverage",OVERALL_LINE_COVERAGE));
		definitions.add(new SonarMetricsColumnDefinition("Overall branch covarage",OVERALL_BRANCH_COVERAGE));
		definitions.add(new FileSizeColumnDefinition());
		//return ",,";
		return definitions;
	}

	public final List<ReportEntry> collectReportEntries(){
		List<ReportEntry> ret = generateReportEntries();
		if (orderBy != null){
			Collections.sort(ret,orderBy.getComparator());
			if (!orderAscendant){
				Collections.reverse(ret);
			}
		}
		return ret;
	}

	protected List<ReportEntry> generateReportEntries() {
		List<ReportEntry> ret = new ArrayList<ReportEntry>();
		collectSonarStats();
		for (String s : targetSourceSet.sourceFiles){
			ReviewSource sourceFile = modelRoot.getSource(s);
			SonarResource resource = getSonarResource(sourceFile);
			ReportEntry report = new ReportEntry(sourceFile,resource,getReviewStatus(sourceFile,modelRoot));
			if (resource != null){
				report.setSonarLink(calculateSonarResourceURL(resource));
			}
			report.setSourceFileLink("/source/"+sourceFile.modelUrl());
			ret.add(report);
		}
		return ret;
	}
	
	
	private String calculateSonarResourceURL(SonarResource resource) {
		String sonarAPI = modelRoot.getSonarBaseURL();
		return sonarAPI + "/../resource/index/"+resource.getResourceId();
	}

	/**
	 * Assigns a {@link ReviewStatus} for specified source file. The status is
	 * calculated based on {@link ReviewEntry}s, that are connected to source.
	 * 
	 * @param sourceFile
	 * @return
	 */
	public static ReviewStatus getReviewStatus(ReviewSource sourceFile,ReviewModel modelRoot) {
		List<ReviewEntry> reviews = sourceFile.getMatchingReviewEntries(modelRoot);
		Set<String> reviewOkUsers = new HashSet<String>();
		for (ReviewEntry re : reviews){
			if (!modelRoot.isInvalidated(re.getSha1Sum())){
				switch (re.getAnnotation()){
				case reviewOff:
					return ReviewStatus.OFF;
				case reviewWontReview:
					return ReviewStatus.WONT_REVIEW;
				case reviewOk:
					reviewOkUsers.add(re.getUser());
					break;
				case reviewTodo:
					return ReviewStatus.TODO;
				default:
					throw new RuntimeException("Not implemented annotation case "+re.getAnnotation());
				}
			}
		}
		List<ReviewEntry> oldReviews = sourceFile.getMatchingReviewEntriesPreviousVersion(modelRoot);
		for (ReviewEntry re : oldReviews){
			if (!modelRoot.isInvalidated(re.getSha1Sum())){
				if (re.getAnnotation() == EReviewAnnotation.reviewTodo){
					//old, but invalidated review todo found!
					return ReviewStatus.TODO;
				}
			}
		}
		if (reviewOkUsers.size() > 1){
			return ReviewStatus.OK_MORE_REVIEWERS;
		} else if (reviewOkUsers.size() == 1){
			return ReviewStatus.OK_ONE_REVIEWER;
		} else {
			if (oldReviews.isEmpty()){
				return ReviewStatus.MISSING;
			} else {
				return ReviewStatus.OLD;
			}
		}
	}
	
	private void collectSonarStats() {
		if (modelRoot.getSonarBaseURL() != null && modelRoot.getSonarProjectId() != null){
			SonarCodeCoverageQuery q = SonarCodeCoverageQuery.getInstance(
					modelRoot.getSonarBaseURL(), 
					modelRoot.getSonarProjectId(), 
					Arrays.asList(OVERALL_COVERAGE,OVERALL_BRANCH_COVERAGE, OVERALL_LINE_COVERAGE,
							IT_BRANCH_COVERAGE,IT_COVERAGE, IT_LINE_COVERAGE,
							UNIT_TEST_BRANCH_COVERAGE, UNIT_TEST_COVERAGE,UNIT_TEST_LINE_COVERAGE
							),modelRoot.getSonarAPIVersion());
			
			//building index
			sonarResources = new IndexByProperty<SonarResource>(new IPropertyGetter<SonarResource>() {
				@Override
				public String getPropertyValue(SonarResource obj) {
					return obj.getFullyQualifiedJavaName();
				}
			});
			List<SonarResource> loadResorucesFromSonar = q.loadResorucesFromSonar();
			if (loadResorucesFromSonar != null){
				for (SonarResource sr : loadResorucesFromSonar){
					collectResourcesRecursive(sr);
				}
			}
		}
	}
	
	/**
	 * Traverses given {@link SonarResource} recursively, and loads all
	 * {@link SonarResourceScope#FIL file scoped} resource into
	 * {@link #sonarResources index}.
	 * 
	 * @param sr
	 */
	private void collectResourcesRecursive(SonarResource sr){
		if (sr.getScope() == SonarResourceScope.FIL){
			sonarResources.addObject(sr);
		}
		for (SonarResource src : sr.getContainedResources()){
			collectResourcesRecursive(src);
		}
	}

	/**
	 * Get the {@link SonarResource} from index, that belongs to specified
	 * {@link ReviewSource}. They are matched by full qualified Java class name.
	 * 
	 * @param s
	 *            The {@link ReviewSource} whose pair is must be found
	 * @return The corresponding {@link SonarResource} or <code>null</code>
	 */
	private SonarResource getSonarResource(ReviewSource s) {
		if (sonarResources != null){
			Collection<SonarResource> targets = sonarResources.getMappedObjects(s.getFullyQualifiedJavaName());
			return targets.size() == 0 ? null : targets.iterator().next();
		}
		return null;
	}

	/**
	 * Returns the title of the generated report.
	 * 
	 * @return
	 */
	public String getTitle() {
		return "Statitics for review source set '"+targetSourceSet+"'";
	}
	
	public void setOrderBy(ColumnDefinition orderBy, boolean asc) {
		this.orderBy = orderBy;
		this.orderAscendant = asc;
	}
	
	public ColumnDefinition getOrderBy() {
		return orderBy;
	}
	
	public boolean isOrderAscendant() {
		return orderAscendant;
	}
	
	/**
	 * The input review source set associated with this generator.
	 * 
	 * @return
	 */
	public ReviewSourceSet getTargetSourceSet() {
		return targetSourceSet;
	}
	
	/**
	 * The target review model root associated with this generator.
	 * 
	 * @return
	 */
	public ReviewModel getModelRoot() {
		return modelRoot;
	}

	/**
	 * Set the {@link ColumnDefinition}s that must be included in report.
	 * 
	 * @param def
	 */
	public void setColumnDefinitions(List<ColumnDefinition> def) {
		this.columnDefinitions = def;
	}
}
