package timgutilities.textio;

import java.nio.file.Path;

public class DirectoryEntry implements Comparable<DirectoryEntry> {
	// by default sort case insensitive, this may cause problems with files like
	// Tim.txt and tim.txt, but looking at most file choosers it's probably how
	// people think it should work
	private static boolean caseInsensitiveSort = true;
	private final String name;
	private String nameLowerCase;
	private final Type type;
	private final Path path;

	public DirectoryEntry(String name, Type type, Path path) throws NullPointerException {
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

	public static void setCaseInsensitiveSort(boolean caseInsensitiveSort) {
		DirectoryEntry.caseInsensitiveSort = caseInsensitiveSort;
	}

	public static boolean isCaseInsensitiveSort() {
		return DirectoryEntry.caseInsensitiveSort;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public String getNameType() {
		return name + "(" + getType().typeName + ")";
	}

	public Path getPath() {
		return path;
	}

	public enum Type {
		FILE("File"), DIRECTORY("Dir");

		private String typeName;

		Type(String typeName) {
			this.typeName = typeName;
		}

		public String getTypeName() {
			return typeName;
		}
	}

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
