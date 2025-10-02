/*Copyright (c) 2023 Tim Graves.

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
 * This class represents a single choice that is used for the choice based
 * methods.
 * 
 * There are up to 4 fields for the entry:
 * 
 * option is displayed n the list of choices - cannot be null
 * 
 * description is used to display any descriptive text - can be null
 * 
 * additional is further text the rotines may somtimes display this
 * 
 * param is a typed object and all choice descriptions int he same choice
 * description data must be of the same type. for some selecion methods the
 * param can be returned so you don;t have to handle mapping the return in a
 * lookup or anything like that
 * 
 * Additionally this can be a separator - in this case it's printed out by the
 * choice routines, but is not selectable.
 * 
 * @param <P>
 */
public class ChoiceDescription<P> implements Comparable<ChoiceDescription<?>> {

	public final static String EMPTY_TEXT = "";
	public final static String FIELD_SEPARATOR = ", ";
	private String option, description, additional;
	private boolean separatorEntry = false;
	private P param;

	public ChoiceDescription(String option) {
		this(option, null, null, false, null);
	}

	public ChoiceDescription(String option, String description) {
		this(option, description, null, false, null);
	}

	public ChoiceDescription(String option, String description, String additional) {
		this(option, description, additional, false, null);
	}

	public ChoiceDescription(String option, boolean separatorEntry) {
		this(option, null, null, separatorEntry, null);
	}

	public ChoiceDescription(String option, String description, boolean separatorEntry) {
		this(option, description, null, separatorEntry, null);
	}

	public ChoiceDescription(String option, String description, String additional, boolean separatorEntry) {
		this(option, description, additional, separatorEntry, null);
	}

	public ChoiceDescription(String option, P param) {
		this(option, null, null, false, param);
	}

	public ChoiceDescription(String option, String description, P param) {
		this(option, description, null, false, param);
	}

	public ChoiceDescription(String option, String description, String additional, P param) {
		this(option, description, additional, false, param);
	}

	public ChoiceDescription(String option, boolean separatorEntry, P param) {
		this(option, null, null, separatorEntry, param);
	}

	public ChoiceDescription(String option, String description, boolean separatorEntry, P param) {
		this(option, description, null, separatorEntry, param);
	}

	public ChoiceDescription(String option, String description, String additional, boolean separatorEntry, P param) {
		super();
		if (option == null) {
			throw new IllegalArgumentException("Option cannot be null");
		}
		this.option = option;
		this.description = description == null ? EMPTY_TEXT : description;
		this.additional = additional == null ? EMPTY_TEXT : additional;
		this.separatorEntry = separatorEntry;
		this.param = param;
	}

	/**
	 * @return the option
	 */
	public String getOption() {
		return option;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the additional
	 */
	public String getAdditional() {
		return additional;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChoiceDescription [option=" + option + ", description=" + description + ", additional=" + additional
				+ ", separatorEntry=" + separatorEntry + ", param=" + param + "]";
	}

	/**
	 * returns the "display" text, if separateFields it true then the output is
	 * split by the FIELD_SEPARATOR< if it's false then the option, description and
	 * additional are just concatenated together
	 * 
	 * @param separateFields
	 * @return
	 */
	public String getData(boolean separateFields) {
		if (separateFields) {
			return option + FIELD_SEPARATOR + description + FIELD_SEPARATOR + additional;
		} else {

			return option + description + additional;
		}
	}

	/**
	 * @return the param
	 */
	public P getParam() {
		return param;
	}

	@Override
	public int compareTo(ChoiceDescription<?> o) {
		return option.compareTo(o.option);
	}

	/**
	 * @return the separatorEntry
	 */
	public boolean isSeparatorEntry() {
		return separatorEntry;
	}

}
