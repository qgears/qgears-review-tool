package hu.qgears.review.tool;

import hu.qgears.review.model.ReviewInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of parsing a review configuration, with warnings and errors. The 
 * contents of this result is to be presented to the user, for example, by 
 * placing them into the 'Problems view'. Note instances of this class must not
 * be reused during configuration processings.
 * 
 * @author chreex
 */
public class ConfigParsingResult {
	/**
	 * Represents a problem that is detected during parsing the review 
	 * configuration. 
	 * @author chreex
	 */
	public static class Problem {
		/**
		 * Type of the problem. 
		 * 
		 * @author chreex
		 */
		public enum Type { 
			/**
			 * Problems are of this type, which result in enumerating fewer 
			 * source code files than expected. Such cases are:
			 * <ul>
			 * <li>empty file set</li>
			 * <li>TODO filter expression which </li>
			 * <li></li>
			 * <li></li>
			 * </ul>  
			 */
			WARNING,
			/**
			 * The following problems are of this type:
			 * <ul>
			 * <li>unresolvable files or directories</li>
			 * <li></li>
			 * <li></li>
			 * <li></li>
			 * </ul> 
			 */
			ERROR 
		};
		
		final Type type;
		final String message;
		final String details;
		final Exception exception;
		final long timestampMs = System.currentTimeMillis();
		
		public Problem(final Type type, final String message) {
			this.type = type;
			this.message = message;
			this.details = null;
			this.exception = null;
		}
		
		public Problem(final Type type, final String message,
				final String details) {
			this.type = type;
			this.message = message;
			this.details = details;
			this.exception = null;
		}
		
		public Problem(final Type type, final String message, 
				final String details, final Exception exception) {
			this.type = type;
			this.message = message;
			this.details = details;
			this.exception = exception;
		}

		public Type getType() {
			return type;
		}
		
		public String getMessage() {
			return message;
		}
		
		public String getDetails() {
			return details;
		}
		
		public Exception getException() {
			return exception;
		}
	}
	
	private final List<Problem> problems; 
	private final ReviewInstance reviewInstance;
	
	public ConfigParsingResult(final ReviewInstance reviewInstance,
			final List<Problem> problems) {
		this.reviewInstance = reviewInstance;
		this.problems = problems;
	}
	
	public ReviewInstance getReviewInstance() {
		return reviewInstance;
	}
	
	/**
	 * 
	 * @return a snapshot of the problems
	 */
	public List<Problem> getProblems() {
		return Collections.unmodifiableList(new ArrayList<Problem>(problems));
	}
	
	void addProblem(final Problem problem) {
		problems.add(problem);
	}
}
