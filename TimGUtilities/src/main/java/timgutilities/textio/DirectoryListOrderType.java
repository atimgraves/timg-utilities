package timgutilities.textio;

/**
 * enum to control the output order when listing directory entries.
 * 
 * Note that the entries will be sorted alphabetically within the groupings
 * below
 */
public enum DirectoryListOrderType {
	/**
	 * The result of a list directory operation will place files in the list before
	 * directories. Both the files and directory entries will be in alphabetical
	 * order within their grouping
	 */
	FILES_FIRST,
	/**
	 * The result of a list directory operation will place directories in the list
	 * before files. Both the files and directory entries will be in alphabetical
	 * order within their grouping
	 */
	DIRECTORIES_FIRST,
	/**
	 * The result of a list directory operation will combine files directories and
	 * they will be sorted based on alphabetical order regardless of type.
	 */
	JUST_BY_NAME
}
