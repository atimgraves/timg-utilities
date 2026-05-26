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
package timgutilities.textio.test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import timgutilities.textio.DirectoryEntry;
import timgutilities.textio.DirectoryListFilterType;
import timgutilities.textio.DirectoryListOrderType;
import timgutilities.textio.DirectorySelectionMode;
import timgutilities.textio.TextIOUtils;

public class TestFileChoser {

	public static void main(String[] args) throws IOException {
		DirectoryEntry.setCaseInsensitiveSort(true);
		List<String> names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.DIRECTORIES_FIRST, false);
		String namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files with directories first and case insensitive sort including hidden files are:\n"
						+ namesString);

		names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.DIRECTORIES_FIRST);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files with directories first and case insensitive sort excluding hidden files are:\n"
						+ namesString);

		names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.FILES_FIRST, false);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files with directories last and case insensitive sort including hidden files are:\n"
						+ namesString);

		names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.FILES_FIRST);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files with directories last and case insensitive sort excluding hidden files are:\n"
						+ namesString);

		DirectoryEntry.setCaseInsensitiveSort(false);
		names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.JUST_BY_NAME, false);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files mixed in order with type and case sensitive sort including hidden files are:\n"
						+ namesString);

		DirectoryEntry.setCaseInsensitiveSort(false);
		names = TextIOUtils.listDirectoryNames(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.JUST_BY_NAME);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files mixed in order with type and case sensitive sort excluding hidden files are:\n"
						+ namesString);

		DirectoryEntry.setCaseInsensitiveSort(true);
		names = TextIOUtils.listDirectoryEntriesAsString(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.JUST_BY_NAME, false, "^.*\\.[Xx][Mm][Ll]$", true, true);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files mixed in order with type and case insensitive sort but only files ending in .xml (where xml can be in upper, ower or mixed case) including hidden files are:\n"
						+ namesString);

		DirectoryEntry.setCaseInsensitiveSort(true);
		names = TextIOUtils.listDirectoryEntriesAsString(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.JUST_BY_NAME, "^.*\\.[Xx][Mm][Ll]$", true, true);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files mixed in order with type and case insensitive sort but only files ending in .xml (where xml can be in upper, ower or mixed case) excluding hidden files are:\n"
						+ namesString);

		names = TextIOUtils.listDirectoryEntriesAsString(".", DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.DIRECTORIES_FIRST, true, ".*[Cc].*", false, true);
		namesString = names.stream().map(n -> n + "\n").collect(Collectors.joining("\n"));
		TextIOUtils.doOutput(
				"Directories and files with directories first in order with type and case insensitive sort but only files or directories containing c or C excluding hidden files are:\n"
						+ namesString);// now let's text the chosers

		// let's chose a file in the current directly, hidden files are allowed
		DirectoryEntry directoryEntry = TextIOUtils.choseFromDirectory(
				"Please chose a file from the list including hidden files but no directories", ".",
				DirectorySelectionMode.SELECT_FILES_ONLY_AS_LEAF, DirectoryListOrderType.FILES_FIRST, false, null,
				false, true, false);
		TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
				+ " it's path is " + directoryEntry.getPath() + " which in it's normalized absolute form is "
				+ directoryEntry.getPath().normalize().toAbsolutePath());

		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please chose an xml file or cancel from the list including hidden files but no directories", ".",
				DirectorySelectionMode.SELECT_FILES_ONLY_AS_LEAF, DirectoryListOrderType.FILES_FIRST, false,
				"^.*\\.[Xx][Mm][Ll]$", true, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		// let's chose a file in the current directly, hidden files are allowed
		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please chose a directory from the list including hidden directories but no files", ".",
				DirectorySelectionMode.SELECT_DIRECTORIES_ONLY_AS_LEAF, DirectoryListOrderType.FILES_FIRST, false, null,
				false, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		// let's chose a file in the current directly, hidden files are allowed
		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please chose a directory or file from the list excluding hidden entries", ".",
				DirectorySelectionMode.SELECT_DIRECTORY_OR_FILE_AS_LEAF, DirectoryListOrderType.DIRECTORIES_FIRST, true,
				null, false, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		// let's chose a file in the current directly, hidden files are allowed
		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please navigate the directories and chose a java file from the list including hidden entries", ".",
				DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_FILE_AS_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, false, "^.*\\.[Jj][Aa][Vv][Aa]$", true, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please navigate the directories and chose a directory including hidden entries", ".",
				DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF, DirectoryListOrderType.DIRECTORIES_FIRST,
				false, null, false, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		// let's chose a file in the current directly, hidden files are allowed
		directoryEntry = TextIOUtils.choseFromDirectory(
				"Please navigate the directories and chose a directory or file from the list including hidden entries",
				".", DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF_SELECT_FILE_AS_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, false, null, false, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}

		// let's chose a file in the current directly, hidden files are allowed
		directoryEntry = TextIOUtils.choseFromDirectory(null, ".",
				DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF_SELECT_FILE_AS_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, false, null, false, true, true);
		if (directoryEntry == null) {
			TextIOUtils.doOutput("You chose the cancel option");
		} else {
			TextIOUtils.doOutput("You chose " + directoryEntry.getName() + " which has type " + directoryEntry.getType()
					+ " it's path is " + directoryEntry.getPath() + " which in it's normalised absolute form is "
					+ directoryEntry.getPath().normalize().toAbsolutePath());
		}
	}
}
