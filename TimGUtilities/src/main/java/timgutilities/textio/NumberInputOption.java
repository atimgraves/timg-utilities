package timgutilities.textio;

/**
 * defined the various options that apply when entering numbers with upper /
 * lowers.
 */
public enum NumberInputOption {
	/**
	 * ignores the lower / upper limits
	 */
	ANY_NUM,
	/**
	 * input must be &gt;= lower limit, upper limit is ignored
	 */
	AT_OR_ABOVE,
	/**
	 * input must be &gt; lower limit, upper limit is ignored
	 */
	ABOVE,
	/**
	 * input must be &lt; = lower limit, upper limit is ignored
	 */
	AT_OR_BELOW,
	/**
	 * input must be &lt; lower limit, upper limit is ignored
	 */
	BELOW,

	/**
	 * input must be &gt;= lower limit and &lt;= upper limit
	 */
	RANGE,
	/**
	 * the same a RANGE, but no restriction text is displayed
	 */
	SELECTION;

}