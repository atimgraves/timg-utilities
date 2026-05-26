/*Copyright (c) 2026 Tim Graves.

The Universal Permissive License (UPL), Version 1.0

Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both

(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),

without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.

This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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
	SELECT_FILES_ONLY_AS_LEAF("Please select the file to open", DirectoryListFilterType.FILE_ONLY, false, false, false,
			false),
	/**
	 * No navigation to other directories is allowed, both directories and files
	 * within the specified directory are listed for selection
	 */
	SELECT_DIRECTORY_OR_FILE_AS_LEAF("Please select the directory to use or file to open",
			DirectoryListFilterType.DIRECTORY_AND_FILE, true, false, false, true),
	/**
	 * Navigation to other directories is allowed, only directories within the
	 * active directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF("Please select the directory to navigate to or use",
			DirectoryListFilterType.DIRECTORY_ONLY, true, true, true, true),
	/**
	 * Navigation to other directories is allowed, only files within the active
	 * directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_FILE_AS_LEAF("Please select the directory to navigate to or the file to open",
			DirectoryListFilterType.DIRECTORY_AND_FILE, false, true, true, false),
	/**
	 * Navigation to other directories is allowed, both directories and files within
	 * the active directory are available for selection
	 */
	SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF_SELECT_FILE_AS_LEAF(
			"Please select the directory to navigate to / open or the file to open",
			DirectoryListFilterType.DIRECTORY_AND_FILE, true, true, true, true);

	DirectorySelectionMode(String defaultPrompt, DirectoryListFilterType directoryListFilterType,
			boolean currentDirectoryIncludedAsOption, boolean parentDirectoryIncludedAsOption, boolean navigableMode,
			boolean directoryAllowedAsLeaf) {
		this.defaultPrompt = defaultPrompt;
		this.directoryListFilterType = directoryListFilterType;
		this.currentDirectoryIncludedAsOption = currentDirectoryIncludedAsOption;
		this.parentDirectoryIncludedAsOption = parentDirectoryIncludedAsOption;
		this.navigableMode = navigableMode;
		this.directoryAllowedAsLeaf = directoryAllowedAsLeaf;
	}

	private final String defaultPrompt;
	private final DirectoryListFilterType directoryListFilterType;
	private final boolean currentDirectoryIncludedAsOption;
	private final boolean parentDirectoryIncludedAsOption;
	private final boolean navigableMode;
	private final boolean directoryAllowedAsLeaf;

	protected String getDefaultPrompt() {
		return defaultPrompt;
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
