package timgutilities.textio;

/**
 * When chosing a file how should the chooser code behave, for example is
 * navigation across directories allowed, or can only files be selected (useful
 * for opening a file - opening a directory is invalid) or perhaps only
 * directories can be selected (usefull for locating a directory so save a
 * file).
 * 
 * There are various protected properties to reduce enum constant specific code
 * in the listing routines.
 */
public enum DirectorySelectionMode {
	/**
	 * No navigation to other directories is allowed, only directories within the
	 * specified directory are listed for selection
	 */
	SELECT_DIRECTORIES_ONLY_AS_LEAF("Please select the directory to use", DirectoryListFilterType.DIRECTORY_ONLY, true,
			false, false, true),
	/**
	 * No navigation to other directories is allowed, only files within the
	 * specified directory are listed for selection
	 */
	SELECT_FILES_ONLY_AS_LEAF("Please select the file to use", DirectoryListFilterType.FILE_ONLY, false, false, false,
			false),
	/**
	 * No navigation to other directories is allowed, both directories and files
	 * within the specified directory are listed for selection
	 */
	SELECT_DIRECTORY_OR_FILE_AS_LEAF("Please select the directory or file to use",
			DirectoryListFilterType.DIRECTORY_AND_FILE, true, false, false, true),
	/**
	 * Navigation to other directories is allowed, only directories within the
	 * active directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF("Please select the directory to move into",
			DirectoryListFilterType.DIRECTORY_ONLY, true, true, true, true),
	/**
	 * Navigation to other directories is allowed, only files within the active
	 * directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_FILE_AS_LEAF("Please select the directory to move into or the file to use",
			DirectoryListFilterType.DIRECTORY_AND_FILE, false, true, true, false),
	/**
	 * Navigation to other directories is allowed, both directories and files within
	 * the active directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF_SELECT_FILE_AS_LEAF(
			"Please select the directory to move into or use or the file to use",
			DirectoryListFilterType.DIRECTORY_AND_FILE, true, true, true, true);

	DirectorySelectionMode(String description, DirectoryListFilterType directoryListFilterType,
			boolean currentDirectoryIncludedAsOption, boolean parentDirectoryIncludedAsOption, boolean navigableMode,
			boolean directoryAllowedAsLeaf) {
		this.description = description;
		this.directoryListFilterType = directoryListFilterType;
		this.currentDirectoryIncludedAsOption = currentDirectoryIncludedAsOption;
		this.parentDirectoryIncludedAsOption = parentDirectoryIncludedAsOption;
		this.navigableMode = navigableMode;
		this.directoryAllowedAsLeaf = directoryAllowedAsLeaf;
	}

	private final String description;
	private final DirectoryListFilterType directoryListFilterType;
	private final boolean currentDirectoryIncludedAsOption;
	private final boolean parentDirectoryIncludedAsOption;
	private final boolean navigableMode;
	private final boolean directoryAllowedAsLeaf;

	protected String getDescription() {
		return description;
	}

	protected DirectoryListFilterType getDirectoryListFilterType() {
		return directoryListFilterType;
	}

	protected boolean isCurrentDirectoryIncludedAsOption() {
		return currentDirectoryIncludedAsOption;
	}

	protected boolean isParentDirectoryIncludedAsOption() {
		return parentDirectoryIncludedAsOption;
	}

	protected boolean isNavigableMode() {
		return navigableMode;
	}

	protected boolean isDirectoryAllowedAsLeaf() {
		return directoryAllowedAsLeaf;
	}
}
