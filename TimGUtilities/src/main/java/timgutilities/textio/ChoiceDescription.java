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
 * This class represents a single choice that is used for the choice based
 * methods.
 * 
 * There are up to 4 fields for the entry:
 * 
 * option is displayed n the list of choices - cannot be null
 * 
 * description is used to display any descriptive text - can be null
 * 
 * additional is further text the routines may sometimes display this
 * 
 * param is a typed object and all choice descriptions in the same choice
 * description data must be of the same type. for some selection methods the
 * param can be returned so you don't have to handle mapping the return in a
 * lookup or anything like that
 * 
 * notSelectable means that regardless of what the select methods do selected
 * will never be set to true, this is used in some situations where
 * 
 * selected is used when multiple options can be selected, for examples
 * selecting multiple files for a zip, or multiple elements in an enum. If it is
 * set then when getting the string data for display (the getData method) "(*)"
 * will be displayed after the name and before the description.
 * 
 * Additionally this can be a separator - in this case it's printed out by the
 * choice routines, but is not selectable.
 * 
 * @param <P> If using the ChoiceDescription to hold an object (for example when
 *            listing directory entries) then P represents the object type is is
 *            holding
 */
public class ChoiceDescription<P> implements Comparable<ChoiceDescription<?>> {

	/**
	 * Used as a default for the description and additional fields if they are not
	 * specified
	 */
	public final static String EMPTY_TEXT = "";
	/**
	 * Used by getText if the results should be separated
	 */
	public final static String FIELD_SEPARATOR = ", ";
	private final String option, description, additional;
	private final boolean separatorEntry;
	private final P param;
	private boolean selected = false;
	private boolean notSelectable = false;

	/**
	 * Create an instance only containing an option. The description, additional are
	 * set to EMPTY_STRING and param is set to null
	 * 
	 * @param option the text of the option
	 */
	public ChoiceDescription(String option) {
		this(option, null, null, false, null, false);
	}

	/**
	 * Create an instance only containing an option and description. The additional
	 * is set to EMPTY_STRING and param is set to null
	 * 
	 * @param option      the text of the option
	 * @param description the text of the description
	 */
	public ChoiceDescription(String option, String description) {
		this(option, description, null, false, null, false);
	}

	/**
	 * Create an instance only containing an option, description and additional. The
	 * param is set to null
	 * 
	 * @param option      the text of the option
	 * @param description the text of the description
	 * @param additional  the text of the additional field
	 */
	public ChoiceDescription(String option, String description, String additional) {
		this(option, description, additional, false, null, false);
	}

	/**
	 * Create an instance only containing an option. The description, additional are
	 * set to EMPTY_STRING and param is set to null
	 * 
	 * @param option         the text of the option
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 */
	public ChoiceDescription(String option, boolean separatorEntry) {
		this(option, null, null, separatorEntry, null, separatorEntry);
	}

	/**
	 * Create an instance only containing an option and description. The additional
	 * is set to EMPTY_STRING and param is set to null
	 * 
	 * @param option         the text of the option
	 * @param description    the text of the description
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 */
	public ChoiceDescription(String option, String description, boolean separatorEntry) {
		this(option, description, null, separatorEntry, null, separatorEntry);
	}

	/**
	 * Create an instance only containing an option, description and additional. The
	 * param is set to null and notSelectable is set to true
	 * 
	 * @param option         the text of the option
	 * @param description    the text of the description
	 * @param additional     the text of the additional field
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 */
	public ChoiceDescription(String option, String description, String additional, boolean separatorEntry) {
		this(option, description, additional, separatorEntry, null, separatorEntry);
	}

	/**
	 * Create an instance containing an option and associated parameter. The
	 * description, additional are set to EMPTY_STRING.
	 * 
	 * @param option the text of the option
	 * @param param  the param to associate with this option
	 */
	public ChoiceDescription(String option, P param) {
		this(option, null, null, false, param, false);
	}

	/**
	 * Create an instance containing an option and associated parameter. The
	 * description, additional are set to EMPTY_STRING.
	 * 
	 * @param option        the text of the option
	 * @param param         the param to associate with this option
	 * @param notSelectable marks this as not being selectable in multi choice
	 *                      operations
	 */
	public ChoiceDescription(String option, P param, boolean notSelectable) {
		this(option, null, null, false, param, notSelectable);
	}

	/**
	 * Create an instance containing an option, description and associated
	 * parameter. The additional is set to EMPTY_STRING.
	 * 
	 * @param option      the text of the option
	 * @param description the text of the description
	 * @param param       the param to associate with this option
	 */
	public ChoiceDescription(String option, String description, P param) {
		this(option, description, null, false, param, false);
	}

	/**
	 * Create an instance containing an option, description and associated
	 * parameter. The additional is set to EMPTY_STRING.
	 * 
	 * @param option        the text of the option
	 * @param description   the text of the description
	 * @param param         the param to associate with this option
	 * @param notSelectable marks this as not being selectable in multi choice
	 *                      operations
	 */
	public ChoiceDescription(String option, String description, P param, boolean notSelectable) {
		this(option, description, null, false, param, notSelectable);
	}

	/**
	 * Create an instance containing an option, description, additional and
	 * associated parameter.
	 * 
	 * @param option      the text of the option
	 * @param description the text of the description
	 * @param additional  the text of the description
	 * @param param       the param to associate with this option
	 */
	public ChoiceDescription(String option, String description, String additional, P param) {
		this(option, description, additional, false, param, false);
	}

	/**
	 * Create an instance containing an option, description, additional and
	 * associated parameter.
	 * 
	 * @param option        the text of the option
	 * @param description   the text of the description
	 * @param additional    the text of the description
	 * @param param         the param to associate with this option
	 * @param notSelectable marks this as not being selectable in multi choice
	 *                      operations
	 */
	public ChoiceDescription(String option, String description, String additional, P param, boolean notSelectable) {
		this(option, description, additional, false, param, notSelectable);
	}

	/**
	 * Create an instance containing an option and param. The description,
	 * additional are set to EMPTY_STRING
	 * 
	 * @param option         the text of the option
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 * @param param          the param to associate with this option
	 */
	public ChoiceDescription(String option, boolean separatorEntry, P param) {
		this(option, null, null, separatorEntry, param, separatorEntry);
	}

	/**
	 * Create an instance containing an option, description and param. The
	 * additional is set to EMPTY_STRING
	 * 
	 * @param option         the text of the option
	 * @param description    the text of the description
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 * @param param          the param to associate with this option
	 */
	public ChoiceDescription(String option, String description, boolean separatorEntry, P param) {
		this(option, description, null, separatorEntry, param, separatorEntry);
	}

	/**
	 * Create an instance containing an option, description, additional and param.
	 * 
	 * @param option         the text of the option
	 * @param description    the text of the description
	 * @param additional     the text of the additional
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 * @param param          the param to associate with this option
	 */
	public ChoiceDescription(String option, String description, String additional, boolean separatorEntry, P param) {
		this(option, description, additional, separatorEntry, param, separatorEntry);
	}

	/**
	 * Create an instance containing an option, description, additional and param.
	 * 
	 * @param option         the text of the option
	 * @param description    the text of the description
	 * @param additional     the text of the additional
	 * @param separatorEntry if true this will be marked as a separator, and the
	 *                       chooser will display it but not let it be selected
	 * @param param          the param to associate with this option
	 * @param notSelectable  marks this as not being selectable in multi choice
	 *                       operations
	 */
	public ChoiceDescription(String option, String description, String additional, boolean separatorEntry, P param,
			boolean notSelectable) {
		super();
		if (option == null) {
			throw new IllegalArgumentException("Option cannot be null");
		}
		this.option = option;
		this.description = description == null ? EMPTY_TEXT : description;
		this.additional = additional == null ? EMPTY_TEXT : additional;
		this.separatorEntry = separatorEntry;
		this.param = param;
		this.notSelectable = notSelectable;
	}

	/**
	 * Gets the value of the option for this ChoiceDescription (usually this is the
	 * string that will be displayed when using the chooser)
	 * 
	 * @return the option test, for display when choosing an option of potentially
	 *         to determine the option choice (e.g. if you're selecting an ENUM
	 *         based on the possible value names)
	 */
	public String getOption() {
		return option;
	}

	/**
	 * Gets the optional description field, for example you may want to provide a
	 * description of an option
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets any optional additional information you have chosen to provide
	 * 
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
		return "ChoiceDescription [option=" + option + ", selected=" + selected + ", description=" + description
				+ ", additional=" + additional + ", separatorEntry=" + separatorEntry + ", param=" + param + "]";
	}

	/**
	 * returns the "display" text, if separateFields it true then the output is
	 * split by the FIELD_SEPARATOR, if it's false then the option, description and
	 * additional are just concatenated together
	 * 
	 * @param separateFields if true the resulting string will have the
	 *                       FIELD_SEPARATOR inserted between them as they are
	 *                       concatenated, if false they will just be concatenated.
	 *                       This makes it easier to have option, description,
	 *                       additional if needed in the choice output
	 * @return text representing the option, description and additional data, if
	 *         selected is true add (*) after the option as an indicator
	 */
	public String getData(boolean separateFields) {
		String selectionStatus = "";
		if (notSelectable) {
			selectionStatus = "(Not selectable)";
		} else {
			selectionStatus = (selected ? "(*)" : "");
		}
		if (separateFields) {
			return option + selectionStatus + FIELD_SEPARATOR + description + FIELD_SEPARATOR + additional;
		} else {

			return option + selectionStatus + description + additional;
		}
	}

	/**
	 * Gets the optional param object, this allows you to easily associate an object
	 * with a choice, enabling the object to be easily determined based on a
	 * particular choice
	 * 
	 * @return the param you may have specified to be associated with this choice
	 */
	public P getParam() {
		return param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo()
	 */
	@Override
	public int compareTo(ChoiceDescription<?> o) {
		return option.compareTo(o.option);
	}

	/**
	 * If this choice is being provided to separate groups of options, then this can
	 * be used to determine that this is option cannot be selected
	 * 
	 * @return the separatorEntry
	 */
	public boolean isSeparatorEntry() {
		return separatorEntry;
	}

	/**
	 * sets the notSelectable flag and marks as not selected, after calling this
	 * then the selected flag can never be set.
	 * 
	 * @return this to enable chaining of this method
	 */
	public ChoiceDescription<P> setNotSelectable() {
		this.notSelectable = true;
		this.selected = false;
		return this;
	}

	/**
	 * is this a non selectable choice
	 * 
	 * @return true if it is
	 */
	public boolean isNotSelectable() {
		return this.notSelectable;
	}

	/**
	 * sets the selected flag, if it is then (*) will be included in the text string
	 * generated to describe this choice
	 * 
	 * @param selected - if this should be selected or not
	 */
	public void setSelected(boolean selected) {
		if (!notSelectable) {
			this.selected = selected;
		}
	}

	/**
	 * sets the selected flag to be true, (*) will be included in the text string
	 * generated to describe this choice
	 * 
	 */
	public void markSelected() {
		this.setSelected(true);
	}

	/**
	 * sets the selected flag to be false, no selected indicator will be included in
	 * the text string generated to describe this choice
	 * 
	 */
	public void markUnselected() {
		this.setSelected(false);
	}

	/**
	 * toggles the selected flag between true / false, if it is true then (*) will
	 * be included in the text string generated to describe this choice.
	 * 
	 * @return the new selected flag value
	 */
	public boolean toggleSelected() {
		this.setSelected(!this.isSelected());
		return isSelected();
	}

	/**
	 * gets the selected flag
	 * 
	 * @return the selected flag
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * gets the text to be used when listing the options, this may include if this
	 * is selected or not
	 * 
	 * @return the display text
	 */
	public String getDisplayText() {
		String selectionStatus = "";
		if (notSelectable) {
			selectionStatus = " (Not selectable)";
		} else {
			selectionStatus = (selected ? " (*)" : "");
		}
		return option + selectionStatus;
	}
}
