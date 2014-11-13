package hu.qgears.review.model;

/**
 * The valid annotation types, that can be assigned to {@link ReviewEntry}s. 
 * 
 * @author agostoni
 *
 */
public enum EReviewAnnotation {

	/**
	 * File reviewed, and everything seems to be OK.
	 */
	reviewOk {
		@Override
		public String getDescription() {
			return "Reviewed OK";
		}
	},
	/**
	 * File reviewed, but there is work to do. 
	 */
	reviewTodo {
		@Override
		public String getDescription() {
			return "Revieved with TODO";
		}
	},
	/**
	 * Marking file to exclude from review source set.
	 */
	reviewOff {
		@Override
		public String getDescription() {
			return "Reviewed with should not be part of class";
		}
	};
	
	/**
	 * A user readable representation of this enum value, that can be shown on
	 * UI.
	 * 
	 * @return
	 */
	public abstract String getDescription();
}
