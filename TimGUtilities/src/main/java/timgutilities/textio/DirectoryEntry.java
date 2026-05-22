package timgutilities.textio;

import java.nio.file.Path;

/**
 * Returned by many of the TextIOUtils list directory entries and directory
 * entry chooser methods
 * 
 * @see TextIOUtils
 */
public class DirectoryEntry implements Comparable<DirectoryEntry> {
	// by default sort case insensitive, this may cause problems with files like
	// Tim.txt and tim.txt, but looking at most file choosers it's probably how
	// people think it should work
	private static boolean caseInsensitiveSort = true;
	private final String name;
	private String nameLowerCase;
	private final Type type;
	private final Path path;

	/**
	 * Build the entry
	 * 
	 * @param name name of the entry, this is in it's containing directory to
	 *             Path.getFilename()
	 * @param type type of the entry
	 * @param path Java PATH to the entry
	 */
	public DirectoryEntry(String name, Type type, Path path) {
		if (name == null) {
			throw new NullPointerException("DirectoryEntry name cannot be null");
		}
		if (type == null) {
			throw new NullPointerException("DirectoryEntry type cannot be null");
		}
		this.name = name;
		// make this lower case to "match" the expected output
		this.nameLowerCase = name.toLowerCase();
		this.type = type;
		this.path = path;
	}

	/**
	 * when comparing to another DirectoryEntry instance is the comparisson case
	 * sensitive or not. Setting this will apply to ALL comparissons
	 * 
	 * @param caseInsensitiveSort true to make sorts case insensitice, false to make
	 *                            it case sensitive.
	 */
	public static void setCaseInsensitiveSort(boolean caseInsensitiveSort) {
		DirectoryEntry.caseInsensitiveSort = caseInsensitiveSort;
	}

	/**
	 * Compare to another instance of DirectoryEntry
	 * 
	 * @return if comparisons are case insensitive
	 */
	public static boolean isCaseInsensitiveSort() {
		return DirectoryEntry.caseInsensitiveSort;
	}

	/**
	 * get the name of the entry (relative to it's parent only)
	 * 
	 * @return the entry name
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the type of the entry, used to help know what the path points to
	 * 
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * get the type and name as a string, e.g. readme.txt(FILE) or ..(DIR) used to
	 * make it clear in a chooser what the entry is
	 * 
	 * @return the string containing the name and it's type
	 */
	public String getNameType() {
		return name + "(" + getType().typeName + ")";
	}

	/**
	 * get the path object representing this entry
	 * 
	 * @return the path object
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * 
	 * represents the type of DirectoryEntry, used for names and so on and to make
	 * working things out easier
	 */
	public enum Type {
		/**
		 * this DirectoryEntry relates to a file Files.isFile(Path) returns true)
		 */
		FILE("File"),

		/**
		 * this DirectoryEntry relates to a directory Files.isDirectory(Path) returns
		 * true)
		 */
		DIRECTORY("Dir");

		private String typeName;

		Type(String typeName) {
			this.typeName = typeName;
		}

		/**
		 * get the type as a nice name
		 * 
		 * @return the types name
		 */
		public String getTypeName() {
			return typeName;
		}
	}

	/**
	 * compares to another Directory entry by comparing the names
	 */
	@Override
	public int compareTo(DirectoryEntry other) {
		if (other == null) {
			return 0;
		} else {
			if (caseInsensitiveSort) {
				return this.nameLowerCase.compareTo(other.nameLowerCase);
			} else {
				return this.name.compareTo(other.name);
			}
		}
	}
}
