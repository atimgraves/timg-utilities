/*
 * 
 * Copyright (c) 2026 Tim Graves.

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a set of choice inputs for the choice engine.
 * 
 * @param <P> If using the contained ChoiceDescription(s) to hold an object (for
 *            example when listing directory entries) then P represents the
 *            object type they are holding
 */
public class ChoiceDescriptionData<P> implements Cloneable {
	/**
	 * the deault text to be used for abandon options
	 */
	public final static String ABANDON_TEXT = "Abandon this operation";
	private static final int EXPECTED_MAX_CHOICES = 30; // this is just to minimize array resizing
	private List<ChoiceDescription<P>> choiceDescriptions = new ArrayList<>(ChoiceDescriptionData.EXPECTED_MAX_CHOICES);
	private boolean processed = false; // if true then the object has been "finalized" in terms of any sorting, adding
	// abandon options etc.
	private boolean doSort = false; // do the fields need sorting during the processing phase
	private boolean separateFields = false; // is the output to be split by "," or not
	private boolean abandonAdded = false; // do we need to add an "abandon" option
	private Integer defaultOptionInt = null;
	private ChoiceDescription<P> defaultOption = null; // if set then this will be used as the default choice in the
														// selection
	private ChoiceDescription<P> abandonChoice; // if we need to add an abandon option in processing this is it

	/**
	 * Built based on array input for all the descriptions, all arrays must be the
	 * same size
	 * 
	 * @param options      an array of option strings (will be presented as the
	 *                     option)
	 * @param descriptions an array of description string
	 * @param additionals  an array of additional info strings
	 */
	public ChoiceDescriptionData(String[] options, String[] descriptions, String[] additionals) {
		super();
		if (options.length != additionals.length) {
			throw new IllegalArgumentException("options and additionals arrays must be the same length");
		}
		if (options.length != descriptions.length) {
			throw new IllegalArgumentException("options and description arrays must be the same length");
		}

		for (int i = 0; i < options.length; i++) {
			addChoiceDescription(new ChoiceDescription<>(options[i], descriptions[i], additionals[i]));
		}
	}

	/**
	 * build based on array input for the choice text and description
	 * 
	 * @param options      an array of option strings (will be presented as the
	 *                     option)
	 * @param descriptions an array of description string
	 */
	public ChoiceDescriptionData(String[] options, String[] descriptions) {
		super();
		if (options.length != descriptions.length) {
			throw new IllegalArgumentException("options and description arrays must be the same length");
		}

		for (int i = 0; i < options.length; i++) {
			addChoiceDescription(new ChoiceDescription<>(options[i], descriptions[i]));
		}
	}

	/**
	 * build from an array with just options text
	 * 
	 * @param options an array of option strings (will be presented as the option)
	 */
	public ChoiceDescriptionData(String[] options) {
		for (int i = 0; i < options.length; i++) {
			addChoiceDescription(new ChoiceDescription<>(options[i]));
		}
	}

	/**
	 * build from a collection of jst the options text
	 * 
	 * @param options a collection of option strings (will be presented as the
	 *                option)
	 */
	public ChoiceDescriptionData(Collection<String> options) {
		this(options.toArray(new String[0]));
	}

	/**
	 * build from a list of choice description objects that already exist
	 * 
	 * @param cds ChoiceDescriptions to add
	 */
	public ChoiceDescriptionData(List<ChoiceDescription<P>> cds) {
		this.addChoiceDescription(cds);
	}

	/**
	 * Build an empty version, will need to be populated by adding options
	 * separately
	 */
	public ChoiceDescriptionData() {
	}

	/**
	 * Do a shallow clone, with the exception that the list of choice descriptions
	 * is a new list, but contains the original choice descriptions
	 */
	@Override
	public ChoiceDescriptionData<P> clone() {
		ChoiceDescriptionData<P> newCdd = new ChoiceDescriptionData<>();
		// want to be able to modify the resulting list
		newCdd.choiceDescriptions = new ArrayList<>(this.choiceDescriptions);
		newCdd.processed = this.processed;
		newCdd.doSort = this.doSort;
		newCdd.separateFields = this.separateFields;
		newCdd.abandonAdded = this.abandonAdded;
		newCdd.abandonChoice = this.abandonChoice;
		newCdd.defaultOption = this.defaultOption;
		newCdd.defaultOptionInt = this.defaultOptionInt;
		return newCdd;
	}

	/**
	 * If set to true then before the choices are presented the choices will be
	 * sorted by the option text
	 * 
	 * @param doSort the doSort to set
	 */
	public void setDoSort(boolean doSort) {
		this.doSort = doSort;
	}

	/**
	 * If true then when the text is displayed it will be splie between open,
	 * description and additional using "," If false they will just be concatenated
	 * 
	 * @param separateFields the separateFields to set
	 */
	public void setSeparateFields(boolean separateFields) {
		this.separateFields = separateFields;
	}

	/**
	 * Add a choicedesription object, cannot be done if the choices data has already
	 * been processed
	 * 
	 * @param cd ChoiceDescription to add
	 */
	public void addChoiceDescription(ChoiceDescription<P> cd) {
		if (processed) {
			throw new IllegalStateException("Can't add choices once this has been locked");
		}
		choiceDescriptions.add(cd);
	}

	/**
	 * adds all of the choice description objects in the collection, can't be done
	 * once pre-selection processing has happened
	 * 
	 * @param cds ChoiceDescriptions to add
	 */
	public void addChoiceDescription(Collection<ChoiceDescription<P>> cds) {
		if (processed) {
			throw new IllegalStateException("Can't add choices once this has been locked");
		}
		choiceDescriptions.addAll(cds);
	}

	/**
	 * checks if the option at index i is the abandon option
	 * 
	 * @param i index of the ChoiceDescription to check
	 * @return true if it is, ralse if not
	 */
	public boolean isAbandoned(int i) {
		if (abandonAdded) {
			return choiceDescriptions.get(i) == abandonChoice;
		} else {
			return false;
		}
	}

	/**
	 * remove the choice at the specified index, can't be done once pre-selection
	 * processing has happened
	 * 
	 * @param i index of the ChoiceDescription to remove
	 * @return true if successful
	 */
	public boolean removeChoice(int i) {
		if (processed) {
			throw new IllegalStateException("Can't remove choices once this has been locked");
		}
		if (i < 0) {
			return false;
		}
		if (i >= choiceDescriptions.size()) {
			return false;
		}
		if (isAbandoned(i)) {
			return false;
		}
		choiceDescriptions.remove(i);
		return true;
	}

	/**
	 * Present the options and get the users input returning the option text for
	 * that choice
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @param i the option string of the option at that index
	 * @return the choice
	 */
	public String getChoice(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getOption();
	}

	/**
	 * Get the choice description for the specified index, if the index is null
	 * negative or more than the number of descriptions (this counts from zero) then
	 * null is returned.
	 * 
	 * @param index the index to use to get the choice
	 * @return the choice
	 */
	public ChoiceDescription<P> getChoiceDescription(Integer index) {
		if (index == null) {
			return null;
		}
		if ((index < 0) || (index >= choiceDescriptions.size())) {
			return null;
		}
		return choiceDescriptions.get(index);
	}

	/**
	 * Present the options and get the users input returning the associated
	 * description for that choice.
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * 
	 * @param i the index of the ChoiceDescription whose description to get
	 * @return gets the description string for the chosen option
	 */
	public String getDescription(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getDescription();
	}

	/**
	 * present the options and get the users choice returning the additional text
	 * for that choice.
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @param i number of option
	 * @return gets the additional string for the chosen option
	 */
	public String getAdditional(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getAdditional();
	}

	/**
	 * present the otpions and get the users choice returning the param object for
	 * that choice
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @param i the index of the ChoiceDescription whose param to get
	 * @return the returned param
	 */
	public P getParam(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getParam();
	}

	/**
	 * Triggers the process of handling sort, adding abandons etc, and makes the
	 * list read only. Only call when you're ready to use this.
	 */
	public void completeAndLock() {
		if (!processed) {
			process();
		}
	}

	private void process() {
		if (doSort) {
			Collections.sort(choiceDescriptions);
		}
		// if there is a choice description set it
		if (defaultOption == null) {
			defaultOptionInt = null;
		} else {
			defaultOptionInt = locateChoiceDescriptionIndexByChoiceDescription(defaultOption);
		}
		processed = true;
	}

	/**
	 * how many options are currently held, note that if the pre-selection
	 * processing has happened this may include an abandon option
	 * 
	 * @return the number of choices
	 */
	public int length() {
		return choiceDescriptions.size();
	}

	/**
	 * get the combined data (option, description and additional) for the option at
	 * that index, if it's the abandon option then only the text is returned.
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @param i the index of the ChoiceDescription whose data to get
	 * @return the text data form the choice
	 */
	public String getData(int i) {
		if (!processed) {
			process();
		}
		ChoiceDescription<P> cd = choiceDescriptions.get(i);
		if (abandonAdded) {
			if (cd == abandonChoice) {
				return cd.getOption();
			}
		}
		return cd.getData(separateFields);
	}

	/**
	 * generates the list of options as a string
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 */
	@Override
	public String toString() {
		if (!processed) {
			process();
		}
		return choiceDescriptions.stream().map((cd) -> {
			return cd.getData(separateFields) + (cd.equals(defaultOption) ? " (Default) " : "");
		}).collect(Collectors.joining("\n"));
	}

	/**
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * The abandon option is not set to the default
	 * 
	 * The abandon option will be added at the end of the list
	 * 
	 * Default text will be added as the abandon option.
	 */
	public void addAbandonOption() {
		addAbandonOption(ABANDON_TEXT, false, false);
	}

	/**
	 * Creates an abandon option, you can chose if it will be the default or not
	 * 
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * The abandon option will be added at the end of the list
	 * 
	 * Default text will be added as the abandon option.
	 * 
	 * @param abandonIsDefault if true the abandon option will be the default option
	 *                         (possibly overiding a previous one) if false the
	 *                         default won't be set
	 */
	public void addAbandonOption(boolean abandonIsDefault) {
		addAbandonOption(ABANDON_TEXT, false, abandonIsDefault);
	}

	/**
	 * Creates an abandon option, you can chose if it will be the default or not
	 * 
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * The abandon option will be added at the end of the list if addFirst is false
	 * or the beginning if it's true
	 * 
	 * Default text will be added as the abandon option.
	 * 
	 * @param addFirst         if true the abandon option will be the first entry in
	 *                         the choices, if false it will be added at the end
	 * @param abandonIsDefault if true the abandon option will be the default option
	 *                         (possibly overiding a previous one) if false the
	 *                         default won't be set
	 */
	public void addAbandonOption(boolean addFirst, boolean abandonIsDefault) {
		addAbandonOption(ABANDON_TEXT, addFirst, abandonIsDefault);
	}

	/**
	 * Creates an abandon option and you can chose if it will at the begining or end
	 * of the list
	 * 
	 * The text you provide will be used as the abandon text
	 * 
	 * @param abandonText the text to use for the abandon option
	 * @param addFirst    if true the abandon option will be the first entry in the
	 *                    choices, if false it will be added at the end
	 */
	public void addAbandonOption(String abandonText, boolean addFirst) {
		addAbandonOption(abandonText, addFirst, false);
	}

	/**
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * The text you provide will be used as the abandon text
	 * 
	 * The abandon option will be added at the end of the list
	 * 
	 * The abandon option is not set to the default
	 * 
	 * @param abandonText the text to use for the abandon option
	 */
	public void addAbandonOption(String abandonText) {
		addAbandonOption(abandonText, false, false);
	}

	/**
	 * Creates an abandon option and you can chose if it is to be used as the
	 * default
	 * 
	 * The text you provide will be used as the abandon text
	 * 
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null
	 * 
	 * If addFirst is true then the abandon option will be at the start of the
	 * options, if not it will be later
	 * 
	 * Only one abandon options is allowed and doing so "freezes" the data
	 * structure, so you can't add additional entries.
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @param abandonText      the text to use for the abandon option
	 * @param addFirst         where to add the abandon option
	 * @param abandonIsDefault if the abandon option is the default choice
	 * 
	 */
	public void addAbandonOption(String abandonText, boolean addFirst, boolean abandonIsDefault) {
		if (processed) {
			throw new IllegalStateException("Can't add choices once this has been locked");
		}
		// only allow one abandon option to be added
		if (abandonAdded) {
			return;
		}
		// force processing to happen as we're adding at the start or the end
		process();
		abandonAdded = true;
		abandonChoice = new ChoiceDescription<>(abandonText, null, null, null);
		if (addFirst) {
			choiceDescriptions.add(0, abandonChoice);
		} else {
			choiceDescriptions.add(abandonChoice);
		}
		if (abandonIsDefault) {
			defaultOption = abandonChoice;
			defaultOptionInt = locateChoiceDescriptionIndexByChoiceDescription(abandonChoice);
		}
	}

	/**
	 * gets the default option
	 * 
	 * @return the default option (if one has been set) null if not
	 */
	public ChoiceDescription<P> getDefaultOption() {
		return defaultOption;
	}

	/**
	 * gets the number of the default option
	 * 
	 * @return the index of the default option (is set) null if not
	 */
	public Integer getDefaultOptionNumber() {
		return defaultOptionInt;
	}

	/**
	 * sets the default choice using the provided choice description, and return the
	 * identified item. If no matching choice is located then return null to
	 * indicate this. If there was an existing choice then this will override this,
	 * and if there is and existing choice but no choice description matching the
	 * call argument is found the existing option will be set to null
	 * 
	 * @param cd the default option to set
	 * @return the default option
	 */
	public ChoiceDescription<P> setDefaultByChoiceDescription(ChoiceDescription<P> cd) {
		defaultOption = choiceDescriptionIsPresent(cd) ? cd : null;
		return defaultOption;
	}

	/**
	 * Sets the choice description at the specified index to be the default
	 * 
	 * @param index the index of the option to use as the default
	 * @return the option that was set
	 */
	public ChoiceDescription<P> setDefaultByIndex(Integer index) {
		defaultOption = index == null ? null : getChoiceDescription(index);
		return defaultOption;
	}

	/**
	 * sets the default choice using the provided option using the string equals,
	 * and return the identified item. If no matching choice is located then return
	 * null to indicate this. If there was an existing choice then this will
	 * override this, and if there is and existing choice but no choice description
	 * matching the call argument is found the existing option will be set to null
	 * 
	 * @param option the name of the option to set as the default
	 * @return the option that was set as the default
	 */
	public ChoiceDescription<P> setDefaultByOption(String option) {
		defaultOption = option == null ? null : locateChoiceDescriptionByOptionString(option);
		return defaultOption;
	}

	/**
	 * sets the default choice using the provided param using it's equals method,
	 * and return the identified item. If no matching choice is located then return
	 * null to indicate this. If there was an existing choice then this will
	 * override this, and if there is and existing choice but no choice description
	 * matching the call argument is found the existing option will be set to null
	 * 
	 * @param param the param to use to locate the option to make the default
	 * @return the option that was set as the default
	 */
	public ChoiceDescription<P> setDefaultByParam(P param) {
		defaultOption = param == null ? null : locateChoiceDescriptionByParam(param);
		return defaultOption;
	}

	/**
	 * check if an option is in the list
	 * 
	 * @param cd the option to check
	 * @return true if it is present, false if not
	 */
	public boolean choiceDescriptionIsPresent(ChoiceDescription<P> cd) {
		return locateChoiceDescriptionIndexByChoiceDescription(cd) != null;
	}

	/**
	 * Locate the number of the choice description (if any) in the list, this used
	 * the equals method, so in principle if there are multiple descriptions that
	 * match only the first is returned.
	 * 
	 * @param cd the option to look for
	 * @return the index of the option (or null if not present)
	 */
	public Integer locateChoiceDescriptionIndexByChoiceDescription(ChoiceDescription<P> cd) {
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			if (cd.equals(choiceDescriptions.get(i))) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Locate the number of the choice description (if any) in the list buy
	 * comparing the supplied string to the option name specified when creating the
	 * choice descriptions. This used the String equals method. If there are
	 * multiple matches with the same option this will ONLY find the first match.
	 * 
	 * @param option the option string to check for
	 * 
	 * @return the index of the option or null if not present
	 */
	public Integer locateChoiceDescriptionIndexByOptionString(String option) {
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			if (option.equals(choiceDescriptions.get(i).getOption())) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Locate the choice description (if any) in the list buy comparing the supplied
	 * string to the option name specified when creating the choice descriptions.
	 * This used the String equals method. If there are multiple matches with the
	 * same option this will ONLY find the first match.
	 * 
	 * @param option the option string of the option to check for
	 * @return the option if present or null if not
	 */
	public ChoiceDescription<P> locateChoiceDescriptionByOptionString(String option) {
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			if (option.equals(choiceDescriptions.get(i).getOption())) {
				return choiceDescriptions.get(i);
			}
		}
		return null;
	}

	/**
	 * Locate the number of the choice description (if any) in the list buy
	 * comparing the supplied param to the param specified when creating the choice
	 * descriptions. This used the param's equals method. If there are multiple
	 * matches with the same option this will ONLY find the first match.
	 * 
	 * @param param the param to be used to check for the option *
	 * @return the index of the option if present or null if not
	 */
	public Integer locateChoiceDescriptionIndexByParam(P param) {
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			if (param.equals(choiceDescriptions.get(i).getParam())) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Locate the choice description (if any) in the list buy comparing the supplied
	 * param to the param specified when creating the choice descriptions. This used
	 * the param's equals method. If there are multiple matches with the same option
	 * this will ONLY find the first match.
	 * 
	 * @param param the param to be used to locate the option
	 * @return the chosen option if present or null if not
	 */
	public ChoiceDescription<P> locateChoiceDescriptionByParam(P param) {
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			if (param.equals(choiceDescriptions.get(i).getParam())) {
				return choiceDescriptions.get(i);
			}
		}
		return null;
	}

	/**
	 * scan all of the options and get their choice strings, combine them and return
	 * 
	 * If not already done triggers the process of handling sort, adding abandons
	 * etc, and makes the list read only. Only call when you're ready to use this.
	 * 
	 * @return a string representing the choices for display
	 */
	public String getChoicesString() {
		process();
		String processedPrompt = "";
		for (int i = 0; i < choiceDescriptions.size(); i++) {
			ChoiceDescription<P> cd = choiceDescriptions.get(i);
			processedPrompt = processedPrompt + "\n[" + i + "] = " + cd.getOption();
			// if this is the default item
			if ((defaultOptionInt != null) && (defaultOptionInt == i)) {
				processedPrompt += " (Default)";
			}
		}
		return processedPrompt;
	}
}
