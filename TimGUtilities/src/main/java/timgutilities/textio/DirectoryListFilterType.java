package timgutilities.textio;

public enum DirectoryListFilterType {
	DIRECTORY_ONLY(true, false), DIRECTORY_AND_FILE(true, true), FILE_ONLY(false, true);

	DirectoryListFilterType(boolean includeDirectories, boolean includeFiles) {
		this.includeDirectories = includeDirectories;
		this.includeFiles = includeFiles;
	}

	private final boolean includeDirectories;
	private final boolean includeFiles;

	public boolean isIncludeDirectories() {
		return includeDirectories;
	}

	public boolean isIncludeFiles() {
		return includeFiles;
	}
}
