package hu.qgears.sonar.client.model;

/**
 * The possible scopes (or types) of SONAR resources. See comments on literals.
 * 
 * @author agostoni
 *
 */
public enum SonarResourceScope {
	/**
	 * Project scope (for instance an eclipse project)
	 */
	TRK
	{
		@Override
		public String scopeName(SonarAPI api) {
			switch (api) {
			case PRE_4_3:
				return "PRJ";
			default:
				return scopeName(api);
			}
		}
	},
	/**
	 * Directory (package) scope (for instance a JAva package).
	 */
	DIR,
	/**
	 * File (java) scope (For instance a Java class).
	 */
	FIL,
	/**
	 * SubProject
	 */
	BRC {
		@Override
		public String scopeName(SonarAPI api) {
			switch (api) {
			case PRE_4_3:
				throw new UnsupportedOperationException("Not available in SONAR server 4.3");
			default:
				return scopeName(api);
			}
		}
	},
	/**
	 * Unit test
	 */
	UTS {
		@Override
		public String scopeName(SonarAPI api) {
			switch (api) {
			case PRE_4_3:
				throw new UnsupportedOperationException("Not available in SONAR server 4.3");
			default:
				return scopeName(api);
			}
		}
	}
;
	public String scopeName(SonarAPI api) {
		return toString();
	}

	public static SonarResourceScope scope(String textContent, SonarAPI api) {
		switch (api) {
		case POST_6_7:
				return SonarResourceScope.valueOf(textContent);
		case PRE_4_3:
		default:
			if ("PRJ".equals(textContent)){
				return TRK;
			}
			return SonarResourceScope.valueOf(textContent);
		}
	}
}
