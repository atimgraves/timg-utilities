package timgutilities.textio;

public enum DirectorySelectionMode {
	SELECT_DIRECTORIES_ONLY_AS_LEAF("Please select the directory to use", DirectoryListFilterType.DIRECTORY_ONLY, true,
			false, false, true),
	SELECT_FILES_ONLY_AS_LEAF("Please select the file to use", DirectoryListFilterType.FILE_ONLY, false, false, false,
			false),
	SELECT_DIRECTORY_OR_FILE_AS_LEAF("Please select the directory or file to use",
			DirectoryListFilterType.DIRECTORY_AND_FILE, true, false, false, true),
	SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF("Please select the directory to move into",
			DirectoryListFilterType.DIRECTORY_ONLY, true, true, true, true),
	SELECT_DIRECTORY_AS_NAVIGATION_OR_FILE_AS_LEAF("Please select the directory to move into or the file to use",
			DirectoryListFilterType.DIRECTORY_AND_FILE, false, true, true, false),
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

	public String getDescription() {
		return description;
	}

	public DirectoryListFilterType getDirectoryListFilterType() {
		return directoryListFilterType;
	}

	public boolean isCurrentDirectoryIncludedAsOption() {
		return currentDirectoryIncludedAsOption;
	}

	public boolean isParentDirectoryIncludedAsOption() {
		return parentDirectoryIncludedAsOption;
	}

	public boolean isNavigableMode() {
		return navigableMode;
	}

	public boolean isDirectoryAllowedAsLeaf() {
		return directoryAllowedAsLeaf;
	}
}
