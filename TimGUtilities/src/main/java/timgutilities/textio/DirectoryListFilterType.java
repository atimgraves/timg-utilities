package timgutilities.textio;

/**
 * The various mode supported for filtering DirectoryEntries when listing them.
 * 
 * @see DirectoryEntry
 */
public enum DirectoryListFilterType {
	/**
	 * Only returns DirectoryEntry instances for sub directories when listing a
	 * directory.
	 * 
	 * There are various protected properties to reduce enum constant specific code
	 * in the listing routines.
	 */
	DIRECTORY_ONLY(true, false),
	/**
	 * Returns DirectoryEntry instances for both files and sub directories when
	 * listing a directory
	 */
	DIRECTORY_AND_FILE(true, true),
	/**
	 * Only returns DirectoryEntry instances for files when listing a directory
	 */
	FILE_ONLY(false, true);

	DirectoryListFilterType(boolean includeDirectories, boolean includeFiles) {
		this.includeDirectories = includeDirectories;
		this.includeFiles = includeFiles;
	}

	private final boolean includeDirectories;
	private final boolean includeFiles;

	protected boolean isIncludeDirectories() {
		return includeDirectories;
	}

	protected boolean isIncludeFiles() {
		return includeFiles;
	}
}
