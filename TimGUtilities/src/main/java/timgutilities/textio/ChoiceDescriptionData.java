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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a set of choice inputs for the choice engine.
 * 
 * @param <P>
 */
public class ChoiceDescriptionData<P> implements Cloneable {
	public final static String ABANDON_TEXT = "Abandon this operation";
	private static final int EXPECTED_MAX_CHOICES = 30; // this is just to minimize array resizing
	private List<ChoiceDescription<P>> choiceDescriptions = new ArrayList<>(ChoiceDescriptionData.EXPECTED_MAX_CHOICES);
	boolean processed = false; // if true then the object has been "finalized" in terms of any sorting, adding
								// abandon options etc.
	boolean doSort = false; // do the fields need sorting during the processing phase
	boolean separateFields = false; // is the output to be split by "," or not
	boolean abandonAdded = false; // do we need to add an "abandon" option
	private ChoiceDescription<P> abandonChoice; // if we need to ad an abandon option in processing this is it

	/**
	 * Built based on array input for all the descriptions
	 * 
	 * @param options
	 * @param descriptions
	 * @param additionals
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
	 * @param options
	 * @param descriptions
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
	 * @param options
	 */
	public ChoiceDescriptionData(String[] options) {
		for (int i = 0; i < options.length; i++) {
			addChoiceDescription(new ChoiceDescription<>(options[i]));
		}
	}

	/**
	 * build from a collection of jst the options text
	 * 
	 * @param options
	 */
	public ChoiceDescriptionData(Collection<String> options) {
		this(options.toArray(new String[0]));
	}

	/**
	 * build from a list of choce description objects that already exist
	 * 
	 * @param cds
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
	 * @param cd
	 */
	public void addChoiceDescription(ChoiceDescription<P> cd) {
		if (processed) {
			throw new IllegalStateException("Can't add choices once this has been used");
		}
		choiceDescriptions.add(cd);
	}

	/**
	 * adds all of the choice description objects in the list, can't be done once
	 * pre-selection processing has happened
	 * 
	 * @param cds
	 */
	public void addChoiceDescription(List<ChoiceDescription<P>> cds) {
		if (processed) {
			throw new IllegalStateException("Can't add choices once this has been processed");
		}
		choiceDescriptions.addAll(cds);
	}

	/**
	 * checks if the option ad index i is the abandon option
	 * 
	 * @param i
	 * @return
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
	 * @param i
	 * @return
	 */
	public boolean removeChoice(int i) {
		if (processed) {
			throw new IllegalStateException("Can't remove choices once this has been processed");
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
	 * @return the choice
	 */
	public String getChoice(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getOption();
	}

	/**
	 * Present the options and get the users input returning the associated
	 * description for that choice
	 * 
	 * @param i
	 * @return
	 */
	public String getDescription(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getDescription();
	}

	/**
	 * present the options and get the users choice returning the additional text
	 * for that choice
	 * 
	 * @param i
	 * @return
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
	 * @param i
	 * @return
	 */
	public P getParam(int i) {
		if (!processed) {
			process();
		}
		return choiceDescriptions.get(i).getParam();
	}

	private void process() {
		if (doSort) {
			Collections.sort(choiceDescriptions);
		}
		processed = true;
	}

	/**
	 * how many options are currently held, note that if the pre-selection
	 * processing has happened this may include an abandon option
	 * 
	 * @return
	 */
	public int length() {
		return choiceDescriptions.size();
	}

	/**
	 * get the combined data (option, description and additional) for the option at
	 * that index, if it's the abandon option then only the text is returned.
	 * 
	 * @param i
	 * @return
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

	@Override
	public String toString() {
		if (!processed) {
			process();
		}
		return choiceDescriptions.stream().map(cd -> cd.getData(separateFields)).collect(Collectors.joining("\n"));
	}

	/**
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * Default text will be added as the abandon option.
	 */
	public void addAbandonOption() {
		addAbandonOption(ABANDON_TEXT);
	}

	/**
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null.
	 * 
	 * @param abandonText
	 */
	public void addAbandonOption(String abandonText) {
		addAbandonOption(ABANDON_TEXT, false);
	}

	/**
	 * If an abandon choice has been added then you can use isAbandoned(choice
	 * number) to see if it was the abandoned that was selected. otherwise you
	 * really should be using the param as the thing to look for, as otherwise your
	 * code may be unhappy it will cause the getParam(length()) to return null
	 * 
	 * If addFirst is true then the abandon option will be at the start of the
	 * options, if not it will be later
	 * 
	 * Only one abandon options is allowed and doing so "freezes" the data
	 * structuire, so you can't add additional entries.
	 */
	public void addAbandonOption(String abandonText, boolean addFirst) {
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
	}
}
