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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timgutilities.textio.DirectoryEntry.Type;

/**
 * This class represents a bunch of utility functions that over the years I have
 * found myself having to repeatedly write, so I basically took my usual code
 * and put it here.
 * 
 * Most of the code here is around the reading and validation of input data.
 * However there is also a mechanism to use that code to then run a command
 * structure allowing you to navigate between different levels of a command tree
 * and on selection of a command to execute Java code (I recommend calling
 * lambda) to do the actual processing. I've mostly used this for putting
 * together simple test code before it's then converted into a proper client.
 */
public class TextIOUtils {

	private TextIOUtils() {
		throw new IllegalCallerException(
				"TextIOUtils is a utility class and shoudl not be constructed, only used statically");
	}

	private static String defaltPauseString = "Please press enter or return to continue";
	private static BufferedReader br;

	/**
	 * Lets you set the default string to be displayed when pausing
	 * 
	 * @param newDefaultPauseString the new string
	 */
	public static void setDefaultPauseString(String newDefaultPauseString) {
		defaltPauseString = newDefaultPauseString;
	}

	/**
	 * gets the default pause string
	 * 
	 * @return the current default pause string
	 */
	public static String getDefaultPauseString() {
		return defaltPauseString;
	}

	/**
	 * If needed setup the appropriate buffered reader on Standard-in
	 */
	private static void setupInput() {
		if (br == null) {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
	}

	/**
	 * Output text in a manner that is compatible with prompts from the input
	 * routines
	 * 
	 * @param output the text to output
	 */
	public static void doOutput(String output) {
		System.out.println(output);
	}

	/**
	 * Waits for the user to press return - useful if you need to allow for
	 * something in a separate environment to complete (e.g. the provisioning of a
	 * service in a cloud) Displays the defaultPauseString to let the user know they
	 * need to do something
	 * 
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static void pauseBeforeProgressing() throws IOException {
		pauseBeforeProgressing(defaltPauseString);
	}

	/**
	 * Displays the prompt and then waits for the user tp press return - useful if
	 * you need to allow for something in a separate environment to complete (e.g.
	 * the provisioning of a service in a cloud)
	 * 
	 * @param prompt the prompt to use when asking for input
	 * 
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static void pauseBeforeProgressing(String prompt) throws IOException {
		getString(prompt, "", true);
	}

	/**
	 * Outputs the prompt and then waits for the user to enter zero or more
	 * characters which are then returned.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the text that was input
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getString(String prompt) throws IOException {
		return getString(prompt, (String) null, false);
	}

	/**
	 * Displays the provided prompt and asks for input. If the defaultValue is non
	 * null that is diplayed and entering nothing (e.g. just pressing rreturn) will
	 * return the default value, if there is not default value provided pressing
	 * return results in a notice that input is required
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue a default value to return if the user just presses return
	 * @return the entered text or the defaultValue if they just pressed return
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getString(String prompt, String defaultValue) throws IOException {
		return getString(prompt, defaultValue, false);
	}

	/**
	 * Displays the provided prompt and asks for input. If the defaultValue is non
	 * null that is displayed and entering nothing (e.g. just pressing return) will
	 * return the default value given, if there is not default value and
	 * allowEmptyInput is false then pressing return results in a notice that input
	 * is required, if allowEmptyInput is true and the user just presses return the
	 * the empty string is returned
	 * 
	 * @param prompt          the prompt to use when asking for input
	 * @param defaultValue    a default value to return if the user just presses
	 *                        return
	 * @param allowEmptyInput if true and there is no default if the user presses
	 *                        return the method will return an empty string, if
	 *                        false and there is no default the users is prompted to
	 *                        enter input
	 * @return the entered text or the defaultValue / empty string if they just
	 *         pressed return
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getString(String prompt, String defaultValue, boolean allowEmptyInput) throws IOException {

		setupInput();
		String result;
		while (true) {
			System.out.println(
					prompt + (defaultValue != null ? " (Press return for default of " + defaultValue + ")" : ""));
			result = br.readLine();
			if (result == null) {
				throw new IOException("End of data, can't proceed");
			}
			if (result.length() == 0) {
				if (defaultValue != null) {
					return defaultValue;
				}
				if (allowEmptyInput) {
					return "";
				} else {
					System.out.println("You must enter a response");
					continue;
				}
			}
			return result;
		}
	}

	/**
	 * Displays the prompt and asks the user to enter y or n (case insensitive) if y
	 * is entered then true is returned, if n then false, if the input is neither y
	 * or n then the user is re-asked for input. If the input is empty then the
	 * defaultValue is returned
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue a default value to return if the user just presses return
	 * @return the boolean value of true if they entered y/Y and false if they
	 *         entered n/N or the default if they just pressed return
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static boolean getYN(String prompt, boolean defaultValue) throws IOException {
		String realPrompt = prompt + "(y/n)";
		while (true) {
			String res = getString(realPrompt, defaultValue ? "y" : "n");
			if (res.equalsIgnoreCase("Y")) {
				return true;
			}
			if (res.equalsIgnoreCase("N")) {
				return false;
			}
			System.out.println("Please enter y or n");
		}
	}

	/**
	 * Displays the prompt and asks the user to enter y or n (case insensitive) if y
	 * is entered then true is returned, if n then false, if the input is neither y
	 * or n then the user is re-asked for input.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the boolean value of true if they entered y/Y and false if they
	 *         entered n/N
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static boolean getYN(String prompt) throws IOException {
		String realPrompt = prompt + "(y/n)";
		while (true) {
			String res = getString(realPrompt);
			if (res.equalsIgnoreCase("Y")) {
				return true;
			}
			if (res.equalsIgnoreCase("N")) {
				return false;
			}
			System.out.println("Please enter y or n");
		}
	}

	/**
	 * display the prompt, then a list of command names strings from the entries in
	 * the RunnableCommands, if insertQuit is present then a quit option is added as
	 * the first option.
	 * 
	 * The user is prompted to enter a number representing the command they want to
	 * run, if the quit option is chosen (see insertQuit above) then the user is
	 * asked to confirm they want to quit the loop and the method will return. The
	 * RunnableCommands runnable is called. If the runnable throws an exception it's
	 * trapped and displayed as output.
	 * 
	 * Once the runnable command has been completed the text output it returns is
	 * displayed if it is non null. if the runnable returns null then nothing is
	 * output
	 * 
	 * Once the output (if any) is presented then provided the chosen option was not
	 * the quit option (if that option is displayed - see insertQuit above) the
	 * sequence returns to the start and the options are re-output and the loop
	 * continues.
	 * 
	 * If the user chooses the quit option they will be asked to confirm before this
	 * method exits the loop and returns
	 * 
	 * @param prompt          the prompt to use when asking for input
	 * @param runableCommands the list of commands to offer
	 * @param insertQuit      if true a quit option will be added
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static void selectAndRunLoop(String prompt, RunnableCommand[] runableCommands, boolean insertQuit)
			throws IOException {
		selectAndRunLoop(prompt, runableCommands, insertQuit, true);
	}

	/**
	 * display the prompt, then a list of command names strings from the entries in
	 * the RunnableCommands, if insertQuit is present then a quit option is added as
	 * the first option.
	 * 
	 * The user is prompted to enter a number representing the command they want to
	 * run, if the quit option is chosen (see insertQuit above) then if confirmQuit
	 * is true the user is asked to confirm they want to quit the loop and the
	 * method will return, if confirmQuit is false the loop will exit withuth asking
	 * for confirmation. The RunnableCommands runnable is called. If the runnable
	 * throws an exception it's trapped and displayed as output.
	 * 
	 * Once the runnable command has been completed the text output it returns is
	 * displayed if it is non null. if the runnable returns null then nothing is
	 * output
	 * 
	 * Once the output (if any) is presented then provided the chosen option was not
	 * the quit option (if that option is displayed - see insertQuit above) the
	 * sequence returns to the start and the options are re-output and the loop
	 * continues.
	 * 
	 * If the user chooses the quit option and confirmQuit is true they will be
	 * asked to confirm before this method exits the loop and returns
	 * 
	 * @param prompt          the prompt to use when asking for input
	 * @param runableCommands the list of commands to offer
	 * @param insertQuit      if true a quit option will be added
	 * @param confirmQuit     if true then the user has to confirm the quit
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static void selectAndRunLoop(String prompt, RunnableCommand[] runableCommands, boolean insertQuit,
			boolean confirmQuit) throws IOException {
		selectAndRunLoop(prompt, runableCommands, insertQuit, confirmQuit, false);
	}

	/**
	 * display the prompt, then a list of command names strings from the entries in
	 * the RunnableCommands, if insertQuit is present then if quitFirst is present a
	 * quit option is added as the first option, if quitFirst is false then the quit
	 * option will be the last option.
	 * 
	 * The user is prompted to enter a number representing the command they want to
	 * run, if the quit option is chosen (see insertQuit above) then if confirmQuit
	 * is true the user is asked to confirm they want to quit the loop and the
	 * method will return, if confirmQuit is false the loop will exit withuth asking
	 * for confirmation. The RunnableCommands runnable is called. If the runnable
	 * throws an exception it's trapped and displayed as output.
	 * 
	 * Once the runnable command has been completed the text output it returns is
	 * displayed if it is non null. if the runnable returns null then nothing is
	 * output
	 * 
	 * Once the output (if any) is presented then provided the chosen option was not
	 * the quit option (if that option is displayed - see insertQuit above) the
	 * sequence returns to the start and the options are re-output and the loop
	 * continues.
	 * 
	 * 
	 * If the user chooses the quit option and confirmQuit is true they will be
	 * asked to confirm before this method exits the loop and returns
	 * 
	 * @param prompt          the prompt to use when asking for input
	 * @param runableCommands the list of commands to offer
	 * @param insertQuit      if true a quit option will be added
	 * @param confirmQuit     if true then the user has to confirm the quit
	 * @param quitFirst       if true then the quit option will be inserted at the
	 *                        start of the commands list (option 0) if false it will
	 *                        be at the end.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static void selectAndRunLoop(String prompt, RunnableCommand[] runableCommands, boolean insertQuit,
			boolean confirmQuit, boolean quitFirst) throws IOException {
		ChoiceDescriptionData<RunnableCommand> cdd = new ChoiceDescriptionData<>();
		Arrays.stream(runableCommands).forEach(cmd -> {
			ChoiceDescription<RunnableCommand> cd;
			cd = new ChoiceDescription<>(cmd.getCommandName(), cmd);
			cdd.addChoiceDescription(cd);
		});
		if (insertQuit) {
			cdd.addAbandonOption("Quit", quitFirst);
		}
		while (true) {
			int cmdSelected = TextIOUtils.getIntChoice("Please chose", cdd);
			// was it quit ?
			if (cdd.isAbandoned(cmdSelected)) {
				if (confirmQuit) {
					// quit was chosen, make sure
					if (getYN("You've chosen to quit, are you sure ?")) {
						return;
					} else {
						continue;
					}
				} else {
					// chose quit and no confirm
					return;
				}
			}
			// quit isn't an option or wasn't chosen
			try {
				String response = cdd.getParam(cmdSelected).runIt();
				if (response != null) {
					doOutput("Response is " + response);
				}
			} catch (Exception e) {
				System.err.println("Exception processing command " + cdd.getParam(cmdSelected).getCommandName());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Displays the prompt, then the strings in the choice description data entries.
	 * The user is prompted to enter an integer representing the choice which it
	 * then returned to the caller if it represents one of the choices.
	 * 
	 * @param prompt                the prompt to use when asking for input
	 * @param choiceDescriptionData the options to be presented, if there is only
	 *                              one item in the choiceDescriptionData then it
	 *                              will be automatically selected
	 * @return the number of the chosen option, note that it's up to the caller to
	 *         detect if this is a quit or separator
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */

	public static int getIntChoice(String prompt, ChoiceDescriptionData<?> choiceDescriptionData) throws IOException {
		if (choiceDescriptionData == null) {
			throw new IllegalArgumentException("ChoiceDescriptionData cannot be null");
		}
		int choicesCount = choiceDescriptionData.length();
		if (choicesCount == 0) {
			throw new IOException("Must provide at least once choice option");
		}
		// if there is only one option then return it automatically
		if (choicesCount == 1) {
			doOutput("Only option " + choiceDescriptionData.getChoice(0) + " is available, selecting it for you");
			return 0;
		}
		String processedPrompt = prompt;
		if (processedPrompt == null) {
			processedPrompt = "Please chose from";
		}
		processedPrompt = processedPrompt + "\nOptions are ";
		processedPrompt += choiceDescriptionData.getChoicesString();

		Integer defaultIndex = choiceDescriptionData.getDefaultOptionNumber();
		if (defaultIndex != null) {
			processedPrompt += "\n";
			return getInt(processedPrompt, NumberInputOption.SELECTION, 0, choicesCount - 1, defaultIndex);
		} else {
			return getInt(processedPrompt, NumberInputOption.SELECTION, 0, choicesCount - 1);
		}
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param options the options to be presented, if there is only one item in the
	 *                options then it will be automatically selected
	 * @return the chosen String
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, String options[]) throws IOException {
		return getStringChoice(prompt, options, -1);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * if defaultValue is non null AND is one of the options then the user can just
	 * press return instead of entering a number to select it
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the options to be presented, if there is only one item in
	 *                     the options then it will be automatically selected
	 * @param defaultValue the default value (which must be one of the options) if
	 *                     the user just presses return
	 * @return the chosen String
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, String options[], String defaultValue) throws IOException {
		ChoiceDescriptionData<?> cdd = new ChoiceDescriptionData<>(options);
		cdd.setDefaultByOption(defaultValue);
		return options[getIntChoice(prompt, cdd)];
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * if defaultIndex is between 0 and the mad options size then the user can just
	 * press return instead of entering a number to select it
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the options to be presented, if there is only one item in
	 *                     the options then it will be automatically selected
	 * @param defaultIndex the index of the default option if the user just presses
	 *                     return
	 * @return the chosen String
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, String options[], Integer defaultIndex) throws IOException {
		ChoiceDescriptionData<?> cdd = new ChoiceDescriptionData<>(options);
		if (defaultIndex != null) {
			if ((defaultIndex >= 0) && (defaultIndex < cdd.length())) {
				cdd.setDefaultByIndex(defaultIndex);
			}
		}
		return options[getIntChoice(prompt, cdd)];
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param options the options to be presented, if there is only one item in the
	 *                options then it will be automatically selected
	 * @return the index of the chosen option
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntChoice(String prompt, String options[]) throws IOException {
		return getIntChoice(prompt, options, null);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param options the options to be presented, if there is only one item in the
	 *                options then it will be automatically selected
	 * @return the index of the chosen option
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntChoice(String prompt, Collection<String> options) throws IOException {
		return getIntChoice(prompt, options, null);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the options to be presented, if there is only one item in
	 *                     the options then it will be automatically selected
	 * @param defaultValue the default value (which must be one of the options) if
	 *                     the user just presses return
	 * @return the index of the chosen option
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntChoice(String prompt, Collection<String> options, String defaultValue) throws IOException {
		ChoiceDescriptionData<?> cdd = new ChoiceDescriptionData<>(options);
		cdd.setDefaultByOption(defaultValue);
		return getIntChoice(prompt, cdd);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the options to be presented, if there is only one item in
	 *                     the options then it will be automatically selected
	 * @param defaultValue the default value (which must be one of the options) if
	 *                     the user just presses return
	 * @return the index of the chosen option
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntChoice(String prompt, String options[], String defaultValue) throws IOException {
		ChoiceDescriptionData<?> cdd = new ChoiceDescriptionData<>(options);
		cdd.setDefaultByOption(defaultValue);
		return getIntChoice(prompt, cdd);
	}

	/**
	 * Displays the prompt, then the strings in the choice description data entries.
	 * The user is prompted to enter an integer representing the choice, if it
	 * represents one of the choices then the string for that choice is returned.
	 * 
	 * @param prompt                the prompt to use when asking for input
	 * @param choiceDescriptionData the set of choices to present, if there is only
	 *                              one option it will be automatically selected
	 * @return the String provided as the option value for the choice.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, ChoiceDescriptionData<?> choiceDescriptionData)
			throws IOException {
		return choiceDescriptionData.getChoice(getIntChoice(prompt, choiceDescriptionData));
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice, if it represents one of
	 * the choices then the string for that choice is returned.
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param options the set of choices to present, if there is only one option it
	 *                will be automatically selected
	 * @return the String provided as the option value for the choice.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, Collection<String> options) throws IOException {
		return getStringChoice(prompt, options, (String) null);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice, if it represents one of
	 * the choices then the string for that choice is returned.
	 * 
	 * If the defaultValue is not null and is one of the options then it will be
	 * marked as the default and the user can just press return to select it
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the set of choices to present, if there is only one
	 *                     option it will be automatically selected
	 * @param defaultValue the default value (which must be one of the options) if
	 *                     the user just presses return
	 * @return the String provided as the option value for the choice.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, Collection<String> options, String defaultValue)
			throws IOException {
		ChoiceDescriptionData<String> cdd = new ChoiceDescriptionData<>(options);
		cdd.setDefaultByOption(defaultValue);
		return getStringChoice(prompt, cdd);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice, if it represents one of
	 * the choices then the string for that choice is returned.
	 * 
	 * If the defaultValue is not null and is one of the options then it will be
	 * marked as the default and the user can just press return to select it
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param options      the set of choices to present, if there is only one
	 *                     option it will be automatically selected
	 * @param defaultValue the index of the default option if the user just presses
	 *                     return
	 * @return the String provided as the option value for the choice.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getStringChoice(String prompt, Collection<String> options, Integer defaultValue)
			throws IOException {
		ChoiceDescriptionData<String> cdd = new ChoiceDescriptionData<>(options);
		cdd.setDefaultByIndex(defaultValue);
		return getStringChoice(prompt, cdd);
	}

	/**
	 * Displays the choices in the choice description data, asks the user to enter a
	 * number representing the choice. If the choice is the "abandon" option (See
	 * ChoiceDescriptionData.addAbandion methods) then null is returned, otherwise
	 * returns the object in the parameter for the individual choice descriptions
	 * 
	 * @param <T>                   the type of the param object in the choices
	 * @param prompt                the prompt to use when asking for input
	 * @param choiceDescriptionData the set of choices to present, if there is only
	 *                              one option it will be automatically selected
	 * @return the object provided as the param value for the choice.
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static <T> T getParamChoice(String prompt, ChoiceDescriptionData<T> choiceDescriptionData)
			throws IOException {
		if (choiceDescriptionData.length() == 0) {
			return null;
		}
		int choiceNo = getIntChoice(prompt, choiceDescriptionData);
		if (choiceDescriptionData.isAbandoned(choiceNo)) {
			return null;
		}
		return choiceDescriptionData.getParam(choiceNo);
	}

	/**
	 * List the options and ask the user to enter a number representing the option
	 * they want, the string selected is returned
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param options the choices
	 * @return the chosen option
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getString(String prompt, String options[]) throws IOException {
		return options[getIntChoice(prompt, options)];
	}

	/**
	 * defined the various options that apply when entering numbers with upper /
	 * lowers.
	 * 
	 * This enum is deprecated as it has been moved out into a separate enum class
	 * file NumberInputOption. For backwards compatibility methods with the old
	 * signatures still exist which do the conversion and call the real methods with
	 * the proper enums
	 * 
	 * This enum will be maintained until at least version 2 of this package
	 */
	@Deprecated(forRemoval = true, since = "1.0.16")
	public enum NUM_TYPE {
		/**
		 * ignores the lower / upper limits
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		ANY_NUM(NumberInputOption.ANY_NUM),
		/**
		 * input must be &gt;= lower limit, upper limit is ignored
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		AT_OR_ABOVE(NumberInputOption.AT_OR_ABOVE),
		/**
		 * input must be &gt; lower limit, upper limit is ignored
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		ABOVE(NumberInputOption.ABOVE),
		/**
		 * input must be &lt; = lower limit, upper limit is ignored
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		AT_OR_BELOW(NumberInputOption.AT_OR_BELOW),
		/**
		 * input must be &lt; lower limit, upper limit is ignored
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		BELOW(NumberInputOption.BELOW),

		/**
		 * input must be &gt;= lower limit and &lt;= upper limit
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		RANGE(NumberInputOption.RANGE),
		/**
		 * the same a RANGE, but no restriction text is displayed
		 */
		@Deprecated(forRemoval = true, since = "1.0.16")
		SELECTION(NumberInputOption.SELECTION);

		private NumberInputOption numberInputOption;

		@Deprecated(forRemoval = true, since = "1.0.16")
		NUM_TYPE(NumberInputOption numberInputOption) {
			this.numberInputOption = numberInputOption;
		}

		/**
		 * get the NumberInputOption that has replaced this internal enum constant
		 * 
		 * @return the numberInputOption
		 */

		@Deprecated(forRemoval = true, since = "1.0.16")
		public NumberInputOption getNumberInputOption() {
			return numberInputOption;
		}

	}

	/**
	 * Asks the user to enter an integer in base 10, this is parsed and if that
	 * fails then the user is re-prompted, if the parsing succeeds then the entered
	 * number is returned to the caller
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getInt(String prompt) throws IOException {
		return getInt(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Asks the user to enter an integer in base 16 (hex), this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntHex(String prompt) throws IOException {
		return getIntBase(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, 16);
	}

	/**
	 * Asks the user to enter an integer in an arbitrary base, this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntBase(String prompt, int base) throws IOException {
		return getIntBase(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, base);
	}

	/**
	 * Asks the user to enter an integer in base 10, this is parsed and if that
	 * fails then the user is re-prompted, if the parsing succeeds then the entered
	 * number is returned to the caller.
	 * 
	 * If the user just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getInt(String prompt, int defaultValue) throws IOException {
		return getInt(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, 10);
	}

	/**
	 * Asks the user to enter an integer in base 16 (hex), this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller.
	 * 
	 * If the suer just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntHex(String prompt, int defaultValue) throws IOException {
		return getInt(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, 16);
	}

	/**
	 * Asks the user to enter an integer in an arbitrary base, this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller.
	 * 
	 * If the user just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntBase(String prompt, int defaultValue, int base) throws IOException {
		return getInt(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue,
				base);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the user is prompted to have another go
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getInt(String , NumberInputOption , int , int ) throws
	 * IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper) throws IOException {
		return getInt(prompt, type.getNumberInputOption(), lower, upper);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the user is prompted to have another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getInt(String prompt, NumberInputOption type, int lower, int upper) throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, 10);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getInt(String , NumberInputOption , int , int, int)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue) throws IOException {
		return getInt(prompt, type.getNumberInputOption(), lower, upper, defaultValue);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getInt(String prompt, NumberInputOption type, int lower, int upper, int defaultValue)
			throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, 10);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the user is prompted to have another go
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getIntHex(String , NumberInputOption , int , int, int)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getIntHex(String prompt, NUM_TYPE type, int lower, int upper) throws IOException {
		return getIntHex(prompt, type.getNumberInputOption(), lower, upper);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the user is prompted to have another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static int getIntHex(String prompt, NumberInputOption type, int lower, int upper) throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, 16);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the default value is used
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getIntHex(String , NumberInputOption , int , int, int)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getIntHex(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue)
			throws IOException {
		return getIntHex(prompt, type.getNumberInputOption(), lower, upper, defaultValue);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the default value is used
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see NumberInputOption
	 */
	public static int getIntHex(String prompt, NumberInputOption type, int lower, int upper, int defaultValue)
			throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, 16);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the user is prompted to have
	 * another go
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getIntBase(String , NumberInputOption , int , int, int)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getIntBase(String prompt, NUM_TYPE type, int lower, int upper, int base) throws IOException {
		return getIntBase(prompt, type.getNumberInputOption(), lower, upper, base);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the user is prompted to have
	 * another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getIntBase(String prompt, NumberInputOption type, int lower, int upper, int base)
			throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, base);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getIntBase(String , NumberInputOption , int , int, int,
	 * int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getIntBase(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue, int base)
			throws IOException {
		return getIntBase(prompt, type.getNumberInputOption(), lower, upper, defaultValue, base);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getIntBase(String prompt, NumberInputOption type, int lower, int upper, int defaultValue,
			int base) throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, base);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used. If useDefault
	 * is true then the value of the default is displayed after the text describing
	 * the restriction, if false it is not
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getInt(String , NumberInputOption , int , int, boolean,
	 * int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the defaultValue
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, boolean useDefault, int defaultValue)
			throws IOException {
		return getInt(prompt, type.getNumberInputOption(), lower, upper, useDefault, defaultValue);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used. If useDefault
	 * is true then the value of the default is displayed after the text describing
	 * the restriction, if false it is not
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the defaultValue
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getInt(String prompt, NumberInputOption type, int lower, int upper, boolean useDefault,
			int defaultValue) throws IOException {
		return getInt(prompt, type, lower, upper, useDefault, defaultValue, 10);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used. If
	 * useDefault is true then the value of the default is displayed after the text
	 * describing the restriction, if false it is not
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getInt(String , NumberInputOption , int , int, boolean,
	 * int, int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the defaultValue
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, boolean useDefault, int defaultValue,
			int base) throws IOException {
		return getInt(prompt, type.getNumberInputOption(), lower, upper, useDefault, defaultValue, base);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used. If
	 * useDefault is true then the value of the default is displayed after the text
	 * describing the restriction, if false it is not
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the defaultValue
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static int getInt(String prompt, NumberInputOption type, int lower, int upper, boolean useDefault,
			int defaultValue, int base) throws IOException {
		String restriction = "";
		int result;
		switch (type) {
		case ANY_NUM:
			break;
		case ABOVE:
			restriction = " (Must be > " + Integer.toString(lower, base) + ")";
			break;
		case AT_OR_ABOVE:
			restriction = " (Must be >= " + Integer.toString(lower, base) + ")";
			break;
		case RANGE:
			restriction = " (Must be >= " + Integer.toString(lower, base) + " and <= " + Integer.toString(upper, base)
					+ ")";
			break;
		case SELECTION:
			break;
		case AT_OR_BELOW:
			restriction = " (Must be <= " + Integer.toString(lower, base) + ")";
			break;
		case BELOW:
			restriction = " (Must be < " + Integer.toString(lower, base) + ")";
			break;
		default:
			throw new IllegalArgumentException("Unknown input restrictions " + type + ", this is a programming error");
		}
		while (true) {
			String resp = useDefault ? getString(prompt + restriction, "" + defaultValue)
					: getString(prompt + restriction);
			// does it parse as an Int ?
			try {
				result = Integer.parseInt(resp, base);
			} catch (NumberFormatException nfe) {
				System.out.println("That was not an integer number, please try again");
				continue;
			}
			// sanity check if needed
			if (type == NumberInputOption.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NumberInputOption.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NumberInputOption.RANGE) || (type == NumberInputOption.SELECTION)) {
				if (result < lower || result > upper) {
					System.out.println("Invalid input, must be >= " + lower + " and <= " + upper);
					continue;
				}
			}
			break;
		}
		return result;
	}

	/**
	 * Asks the user to enter a long value and returns a Long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLong(String prompt) throws IOException {
		return getLong(prompt, NumberInputOption.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, 10);
	}

	/**
	 * Asks the user to enter a hexadecimal value and returns it as a long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLongHex(String prompt) throws IOException {
		return getLongBase(prompt, NumberInputOption.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, 16);
	}

	/**
	 * Asks the user to enter a number in an arbitrary base (specified by the base
	 * param) and returns it as a long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLongBase(String prompt, int base) throws IOException {
		return getLongBase(prompt, NumberInputOption.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, base);
	}

	/**
	 * Asks the user to enter a long value, but if they press return with no input
	 * returns the default
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLong(String prompt, long defaultValue) throws IOException {
		return getLong(prompt, NumberInputOption.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, true, defaultValue, 10);
	}

	/**
	 * Asks the user to enter a hex value and returns as a long, but if they just
	 * press return returns the default
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLongHex(String prompt, long defaultValue) throws IOException {
		return getLong(prompt, NumberInputOption.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, true, defaultValue, 16);
	}

	/**
	 * 
	 * Asks the user to enter a number in an arbitrary base (specified by the base
	 * param) and returns it as a long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param base         the base to be used when parsing the input, e.g. 2 for
	 *                     binary, 9 for octal, 10 for decimal, 16 for hex etc.
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static long getLongBase(String prompt, long defaultValue, int base) throws IOException {
		return getLong(prompt, NumberInputOption.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue,
				base);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLong(String , NumberInputOption , long , long) throws
	 * IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper) throws IOException {
		return getLong(prompt, type.getNumberInputOption(), lower, upper);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a Long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLong(String prompt, NumberInputOption type, long lower, long upper) throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, 10);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLong(String , NumberInputOption , long , long, long)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLong(prompt, type.getNumberInputOption(), lower, upper, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLong(String prompt, NumberInputOption type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, 10);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper) but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLongHex(String , NumberInputOption , long , long)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLongHex(String prompt, NUM_TYPE type, long lower, long upper) throws IOException {
		return getLongHex(prompt, type.getNumberInputOption(), lower, upper);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper) but returns a Long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLongHex(String prompt, NumberInputOption type, long lower, long upper) throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, 16);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLongHex(String , NumberInputOption , long , long,
	 * long) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLongHex(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLongHex(prompt, type.getNumberInputOption(), lower, upper, defaultValue);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLongHex(String prompt, NumberInputOption type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, 16);
	}

	/**
	 * see getInt(prompt, type, lower, upper, base) but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLongBase(String , NumberInputOption , long , long,
	 * int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLongBase(String prompt, NUM_TYPE type, long lower, long upper, int base) throws IOException {
		return getLongBase(prompt, type.getNumberInputOption(), lower, upper, base);
	}

	/**
	 * see getInt(prompt, type, lower, upper, base) but returns a Long
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @param base   the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLongBase(String prompt, NumberInputOption type, long lower, long upper, int base)
			throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, base);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, defaultValue, base) but returns a
	 * Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLongBase(String , NumberInputOption , long , long,
	 * long, int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLongBase(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue, int base)
			throws IOException {
		return getLongBase(prompt, type.getNumberInputOption(), lower, upper, defaultValue, base);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, defaultValue, base) but returns a
	 * Long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLongBase(String prompt, NumberInputOption type, long lower, long upper, long defaultValue,
			int base) throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, base);
	}

	/**
	 * see getInt(prompt, type, lower, upper, useDefault, useDefault, defaultValue)
	 * but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLong(String , NumberInputOption , long , long,
	 * boolean, long) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, boolean useDefault,
			long defaultValue) throws IOException {
		return getLong(prompt, type.getNumberInputOption(), lower, upper, useDefault, defaultValue);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, useDefault, useDefault,
	 * defaultValue) but returns a Long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLong(String prompt, NumberInputOption type, long lower, long upper, boolean useDefault,
			long defaultValue) throws IOException {
		return getLong(prompt, type, lower, upper, useDefault, defaultValue, 10);
	}

	/**
	 * see getLongBase(prompt, type, lower, upper, useDefault, defaultValue, base)
	 * but returns a Long
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getLong(String , NumberInputOption , long , long,
	 * boolean, long, int) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, boolean useDefault,
			long defaultValue, int base) throws IOException {
		return getLong(prompt, type.getNumberInputOption(), lower, upper, useDefault, defaultValue, base);
	}

	/**
	 * see getLongBase(prompt, type, lower, upper, useDefault, defaultValue, base)
	 * but returns a Long
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @param base         the number base to use (e.g. 2 for binary, 16 for hex)
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static long getLong(String prompt, NumberInputOption type, long lower, long upper, boolean useDefault,
			long defaultValue, int base) throws IOException {
		String restriction = "";
		long result;
		switch (type) {
		case ANY_NUM:
			break;
		case ABOVE:
			restriction = " (Must be > " + Long.toString(lower, base) + ")";
			break;
		case AT_OR_ABOVE:
			restriction = " (Must be >= " + Long.toString(lower, base) + ")";
			break;
		case RANGE:
			restriction = " (Must be >= " + Long.toString(lower, base) + " and <= " + Long.toString(upper, base) + ")";
			break;
		case SELECTION:
			break;
		case AT_OR_BELOW:
			restriction = " (Must be <= " + Long.toString(lower, base) + ")";
			break;
		case BELOW:
			restriction = " (Must be < " + Long.toString(lower, base) + ")";
			break;
		default:
			throw new IllegalArgumentException("Unknown input restrictions " + type + ", this is a programming error");
		}
		while (true) {
			String resp = useDefault ? getString(prompt + restriction, "" + defaultValue)
					: getString(prompt + restriction);
			// does it parse as an Int ?
			try {
				result = Long.parseLong(resp, base);
			} catch (NumberFormatException nfe) {
				System.out.println("That was not an long number, please try again");
				continue;
			}
			// sanity check if needed
			if (type == NumberInputOption.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NumberInputOption.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NumberInputOption.RANGE) || (type == NumberInputOption.SELECTION)) {
				if (result < lower || result > upper) {
					System.out.println("Invalid input, must be >= " + lower + " and <= " + upper);
					continue;
				}
			}
			break;
		}
		return result;
	}

	/**
	 * see getInt(prompt) but returns a double
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static double getDouble(String prompt) throws IOException {
		return getDouble(prompt, NumberInputOption.ANY_NUM, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	/**
	 * See getInt(prompt, defaultValue) but returns a double
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static double getDouble(String prompt, double defaultValue) throws IOException {
		return getDouble(prompt, NumberInputOption.ANY_NUM, Double.MIN_VALUE, Double.MAX_VALUE, true, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a double
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getDouble(String , NumberInputOption , double , double)
	 * throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper) throws IOException {
		return getDouble(prompt, type.getNumberInputOption(), lower, upper);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a double
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param type   the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower  if relevant for the type the lower limit for checking
	 * @param upper  if relevant for the type the upper limit for checking
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static double getDouble(String prompt, NumberInputOption type, double lower, double upper)
			throws IOException {
		return getDouble(prompt, type, lower, upper, false, 0.0);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a double
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getDouble(String , NumberInputOption , double , double,
	 * double) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper, double defaultValue)
			throws IOException {
		return getDouble(prompt, type.getNumberInputOption(), lower, upper, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a double
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static double getDouble(String prompt, NumberInputOption type, double lower, double upper,
			double defaultValue) throws IOException {
		return getDouble(prompt, type, lower, upper, true, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper, useDefault, useDefault, defaultValue)
	 * but returns a double
	 * 
	 * This method is deprecated and is only provided for backwards compatibility it
	 * is a wrapper around getDouble(String , NumberInputOption , double , double,
	 * boolean, double) throws IOException
	 * 
	 * This method will be maintained until at least version 2 of this package
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NUM_TYPE
	 */

	@Deprecated(forRemoval = true, since = "1.0.16")
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper, boolean useDefault,
			double defaultValue) throws IOException {
		return getDouble(prompt, type.getNumberInputOption(), lower, upper, useDefault, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper, useDefault, useDefault, defaultValue)
	 * but returns a double
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param type         the type of checking to do see TextIOUtils.NUM_TYPE
	 * @param lower        if relevant for the type the lower limit for checking
	 * @param upper        if relevant for the type the upper limit for checking
	 * @param useDefault   if true offer the default value
	 * @param defaultValue the default value to use if the user just presses return
	 * @return the entered number
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * @see NumberInputOption
	 */
	public static double getDouble(String prompt, NumberInputOption type, double lower, double upper,
			boolean useDefault, double defaultValue) throws IOException {
		String restriction = "";
		double result;
		switch (type) {
		case ABOVE:
			restriction = " (Must be > " + lower + ")";
			break;
		case ANY_NUM:
			break;
		case AT_OR_ABOVE:
			restriction = " (Must be >= " + lower + ")";
			break;
		case AT_OR_BELOW:
			restriction = " (Must be <= " + lower + ")";
			break;
		case BELOW:
			restriction = " (Must be < " + lower + ")";
			break;
		case RANGE:
			restriction = " (Must be >= " + lower + " and <= " + upper + ")";
			break;
		case SELECTION:
			break;
		default:
			throw new IllegalArgumentException("Unknown input restrictions " + type + ", this is a programming error");
		}
		while (true) {
			String resp = useDefault ? getString(prompt + restriction, "" + defaultValue)
					: getString(prompt + restriction);
			// does it parse as an double ?
			try {
				result = Double.parseDouble(resp);
			} catch (NumberFormatException nfe) {
				System.out.println("That was not a double number, please try again");
				continue;
			}
			// sanity check if needed
			if (type == NumberInputOption.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NumberInputOption.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NumberInputOption.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NumberInputOption.RANGE) || (type == NumberInputOption.SELECTION)) {
				if (result < lower || result > upper) {
					System.out.println("Invalid input, must be >= " + lower + " and <= " + upper);
					continue;
				}
			}
			break;
		}
		return result;
	}

	/**
	 * Asks the user to enter a string representing a file name and path which can
	 * be relative or absolute, ensures that the entered string represents an
	 * existing file (not a directory) if it's not a file then requests another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFile(String prompt) throws IOException {
		return getFile(prompt, null, null);
	}

	/**
	 * Asks the user to enter a string representing a file name and path which can
	 * be relative or absolute, ensures that the entered string represents an
	 * existing file (not a directory) if it's not a file then requests another go,
	 * if the user enters and empty string then the defaultValue is used
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default name to use if the user just presses return
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFile(String prompt, String defaultValue) throws IOException {
		while (true) {
			String name = getString(prompt, defaultValue);
			File file = new File(name);
			if (file.isFile()) {
				return name;
			}
		}
	}

	/**
	 * Asks the user to enter a string representing a file name and path which must
	 * be relative to the starting location, ensures that the entered string
	 * represents an existing file (not a directory) if it's not a file then
	 * requests another go
	 * 
	 * @param prompt        the prompt to use when asking for input
	 * @param startLocation the directory to prepend to the entered name
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFileUnder(String prompt, String startLocation) throws IOException {
		return getFileUnder(prompt, startLocation, null);
	}

	/**
	 * Asks the user to enter a string representing a file name and path which must
	 * be relative to the starting location, ensures that the entered string
	 * represents an existing file (not a directory) if it's not a file then
	 * requests another go, if the user enters and empty string then the
	 * defaultValue is used
	 * 
	 * @param prompt        the prompt to use when asking for input
	 * @param startLocation the directory to prepend to the entered name
	 * @param defaultValue  the default file name if the user just presses return
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFileUnder(String prompt, String startLocation, String defaultValue) throws IOException {
		while (true) {
			String name = getString(prompt, defaultValue);
			File file = new File(startLocation + File.separator + name);
			if (file.isFile()) {
				return name;
			}
		}
	}

	/**
	 * Asks the user to enter a string representing a file name and path which can
	 * be absolute ot relative to the starting location where the filename element
	 * (last part of the path) must match the regular expression, ensures that the
	 * entered string represents an existing file (not a directory) if it's not a
	 * file then requests another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param regexp a regular expression that the entered filename must match, if
	 *               it doesn't then the users is asked to re-enter the name. If
	 *               null any entered name will match
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFileMatching(String prompt, String regexp) throws IOException {
		return getFile(prompt, regexp, null);
	}

	/**
	 * Asks the user to enter a string representing a file name and path which can
	 * be absolute or relative to the starting location where the filename element
	 * (last part of the path) must match the regular expression, ensures that the
	 * entered string represents an existing file (not a directory) if it's not a
	 * file then requests another go, if the user enters and empty string then the
	 * defaultValue is used.
	 * 
	 * Note that if regexp is non null it is only applied to the filename component,
	 * not the rest of the path entered
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param regexp       if non null then the filename must match this
	 * @param defaultValue the default name if the user just presses return
	 * @return the entered string this will represent a file
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getFileMatching(String prompt, String regexp, String defaultValue) throws IOException {
		return getFile(prompt, regexp, defaultValue);
	}

	private static String getFile(String prompt, String regexp, String defaultValue) throws IOException {
		Pattern p = null;
		if (regexp != null) {
			p = Pattern.compile(regexp);
		}
		while (true) {
			String name = getString(prompt, defaultValue);
			File file = new File(name);
			// check it's a file
			if (!file.isFile()) {
				doOutput("File name " + name + " is not a file");
				continue;
			}
			// check if it matched the regexp if provided
			if (p != null) {
				String fileName = file.getName();
				Matcher matcher = p.matcher(fileName);
				if (!matcher.find()) {
					doOutput("File name part of " + name + " (" + fileName + ") does not match pattern " + regexp);
					continue;
				}
			}
			return name;
		}
	}

	/**
	 * Asks the user to enter a string representing a directory which can be
	 * relative or absolute, ensures that the entered string represents an existing
	 * directory (not a file) and if it's not then requests another go
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered string this will represent a directory
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getDirectory(String prompt) throws IOException {
		return getDirectory(prompt, null);
	}

	/**
	 * Asks the user to enter a string representing a directory which can be
	 * relative or absolute, ensures that the entered string represents an existing
	 * directory (not a file) and if it's not then requests another go, if the user
	 * enters an empty string then the defaultValue is used
	 * 
	 * @param prompt       the prompt to use when asking for input
	 * @param defaultValue the default to use if the user just presses return
	 * @return the entered string this will represent a directory
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getDirectory(String prompt, String defaultValue) throws IOException {
		while (true) {
			String name = getString(prompt, defaultValue);
			File file = new File(name);
			if (!file.isDirectory()) {
				continue;
			}
			return name;
		}
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first. Entries determined as hidden by the OS (e.g. starting with
	 * . in Unix / Linux / MacOS, hidden flag for windows) are removed from the
	 * result
	 * 
	 * @param dir the directory to list
	 * @return returns the list of names
	 */
	public static List<String> listDirectoryNames(String dir) {
		return listDirectoryNames(dir, true);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first
	 * 
	 * @param dir                the directory to list
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return returns the list of names
	 */
	public static List<String> listDirectoryNames(String dir, boolean excludeHiddenFiles) {
		return listDirectoryNames(Path.of(dir), excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first. Entries determined as hidden by the OS (e.g. starting with
	 * . in Unix / Linux / MacOS, hidden flag for windows) are removed from the
	 * result
	 * 
	 * @param dir the directory to list
	 * @return returns the list of names
	 */
	public static List<String> listDirectoryNames(Path dir) {
		return listDirectoryNames(dir, true);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first
	 * 
	 * @param dir                the directory to list
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return returns the list of names
	 */
	public static List<String> listDirectoryNames(Path dir, boolean excludeHiddenFiles) {
		return listDirectoryNames(dir, DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.DIRECTORIES_FIRST, excludeHiddenFiles);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @return the names of the type specified by the filter only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListFilterType listFilterType) {
		return listDirectoryNames(dir, listFilterType, true);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names of the type specified by the filter only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListFilterType listFilterType,
			boolean excludeHiddenFiles) {
		return listDirectoryNames(Path.of(dir), listFilterType, excludeHiddenFiles);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @return the names of the type specified by the filter only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListFilterType listFilterType) {
		return listDirectoryNames(dir, listFilterType, true);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListFilterType listFilterType,
			boolean excludeHiddenFiles) {
		return listDirectoryNames(dir, listFilterType, DirectoryListOrderType.DIRECTORIES_FIRST, excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir           the directory to list
	 * @param listOrderType how the resulting entries should be ordered
	 * @return the names only
	 * 
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListOrderType listOrderType) {
		return listDirectoryNames(dir, listOrderType, true);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping
	 * 
	 * @param dir                the directory to list
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListOrderType listOrderType,
			boolean excludeHiddenFiles) {
		return listDirectoryNames(Path.of(dir), listOrderType, excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir           the directory to list
	 * @param listOrderType how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListOrderType listOrderType) {
		return listDirectoryNames(dir, listOrderType, true);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping
	 * 
	 * @param dir                the directory to list
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListOrderType listOrderType,
			boolean excludeHiddenFiles) {
		return listDirectoryNames(dir, DirectoryListFilterType.DIRECTORY_AND_FILE, listOrderType, excludeHiddenFiles);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed). Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @param listOrderType  how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType) {
		return listDirectoryNames(dir, listFilterType, listOrderType, true);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed)
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles) {
		return listDirectoryNames(Path.of(dir), listFilterType, listOrderType, excludeHiddenFiles);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed). Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @param listOrderType  how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType) {
		return listDirectoryNames(dir, listFilterType, listOrderType, true);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed)
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNames(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles) {
		return listDirectoryEntriesAsString(dir, listFilterType, listOrderType, excludeHiddenFiles, null, true, false);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first. Entries determined as hidden by the OS (e.g. starting with
	 * . in Unix / Linux / MacOS, hidden flag for windows) are removed from the
	 * result
	 * 
	 * @param dir the directory to list
	 * @return list of name/type strings
	 */
	public static List<String> listDirectoryNameTypes(String dir) {
		return listDirectoryNameTypes(dir);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first.
	 * 
	 * @param dir                the directory to list
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return list of name/type strings
	 */
	public static List<String> listDirectoryNameTypes(String dir, boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(Path.of(dir), excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first. Entries determined as hidden by the OS (e.g. starting with
	 * . in Unix / Linux / MacOS, hidden flag for windows) are removed from the
	 * result
	 * 
	 * @param dir the directory to list
	 * @return list of name/type strings
	 */
	public static List<String> listDirectoryNameTypes(Path dir) {
		return listDirectoryNameTypes(dir, true);
	}

	/**
	 * Locates all entries in the specified directory strict alpha order with
	 * directories first.
	 * 
	 * @param dir                the directory to list
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return list of name/type strings
	 */
	public static List<String> listDirectoryNameTypes(Path dir, boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(dir, DirectoryListFilterType.DIRECTORY_AND_FILE,
				DirectoryListOrderType.DIRECTORIES_FIRST, excludeHiddenFiles);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @return the names only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListFilterType listFilterType) {
		return listDirectoryNameTypes(dir, listFilterType, true);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output.
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListFilterType listFilterType,
			boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(Path.of(dir), listFilterType, excludeHiddenFiles);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @return the names only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListFilterType listFilterType) {
		return listDirectoryNameTypes(dir, listFilterType, true);
	}

	/**
	 * Locates specified (dir or file or both) entries in the specified directory
	 * strict alpha order with directories first if they are in the output.
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListFilterType listFilterType,
			boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(dir, listFilterType, DirectoryListOrderType.DIRECTORIES_FIRST,
				excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir           the directory to list
	 * @param listOrderType how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListOrderType listOrderType) {
		return listDirectoryNameTypes(dir, listOrderType, true);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping.
	 * 
	 * @param dir                the directory to list
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListOrderType listOrderType,
			boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(Path.of(dir), listOrderType, excludeHiddenFiles);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping. Entries
	 * determined as hidden by the OS (e.g. starting with . in Unix / Linux / MacOS,
	 * hidden flag for windows) are removed from the result
	 * 
	 * @param dir           the directory to list
	 * @param listOrderType how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListOrderType listOrderType) {
		return listDirectoryNameTypes(dir, listOrderType, true);
	}

	/**
	 * Locates all entries in the specified directory in output in either dir first,
	 * file first or mixed, returned in strict alpha order by grouping.
	 * 
	 * @param dir                the directory to list
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListOrderType listOrderType,
			boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(dir, DirectoryListFilterType.DIRECTORY_AND_FILE, listOrderType,
				excludeHiddenFiles);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed). Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @param listOrderType  how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType) {
		return listDirectoryNameTypes(dir, listFilterType, listOrderType, true);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed).
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles) {
		return listDirectoryNameTypes(Path.of(dir), listFilterType, listOrderType, excludeHiddenFiles);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed). Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir            the directory to list
	 * @param listFilterType what entries to return (sub dirs, files or both)
	 * @param listOrderType  how the resulting entries should be ordered
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType) {
		return listDirectoryEntriesAsString(dir, listFilterType, listOrderType, true, null, true, true);
	}

	/**
	 * Locates the specified entries (dir, file, both) in the specified directory,
	 * returning in strict alpha order with the specified grouping (dir / file first
	 * or mixed).
	 * 
	 * @param dir                the directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @return the names only
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryNameTypes(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles) {
		return listDirectoryEntriesAsString(dir, listFilterType, listOrderType, excludeHiddenFiles, null, true, true);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls. Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir             directory to list
	 * @param listFilterType  what entries to return (sub dirs, files or both)
	 * @param listOrderType   how the resulting entries should be ordered
	 * @param regexp          if provided is used to only select matching entries,
	 *                        for example files ending in .java
	 * @param regexpFilesOnly does the regexp apply to both directories and files or
	 *                        just files
	 * @param nameAndType     if true then the name and type will be in the result
	 *                        (e.g. tim.txt (File)), if fals only the name (e.g.
	 *                        tim.txt)
	 * @return the names only or names + type (see nameAndType flag)
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryEntriesAsString(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, String regexp, boolean regexpFilesOnly, boolean nameAndType) {
		return listDirectoryEntriesAsString(Path.of(dir), listFilterType, listOrderType, true, regexp, regexpFilesOnly,
				nameAndType);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls. Entries determined as hidden by the OS (e.g. starting with . in
	 * Unix / Linux / MacOS, hidden flag for windows) are removed from the result
	 * 
	 * @param dir             directory to list
	 * @param listFilterType  what entries to return (sub dirs, files or both)
	 * @param listOrderType   how the resulting entries should be ordered
	 * @param regexp          if provided is used to only select matching entries,
	 *                        for example files ending in .java
	 * @param regexpFilesOnly does the regexp apply to both directories and files or
	 *                        just files
	 * @param nameAndType     if true then the name and type will be in the result
	 *                        (e.g. tim.txt (File)), if fals only the name (e.g.
	 *                        tim.txt)
	 * @return the names only or names + type (see nameAndType flag)
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryEntriesAsString(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, String regexp, boolean regexpFilesOnly, boolean nameAndType) {
		return listDirectoryEntriesAsString(dir, listFilterType, listOrderType, true, regexp, regexpFilesOnly,
				nameAndType);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls.
	 * 
	 * @param dir                directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @param regexp             if provided is used to only select matching
	 *                           entries, for example files ending in .java
	 * @param regexpFilesOnly    does the regexp apply to both directories and files
	 *                           or just files
	 * @param nameAndType        if true then the name and type will be in the
	 *                           result (e.g. tim.txt (File)), if false only the
	 *                           name (e.g. tim.txt)
	 * @return the names only or names + type (see nameAndType flag)
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryEntriesAsString(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles, String regexp, boolean regexpFilesOnly,
			boolean nameAndType) {

		return listDirectoryEntriesAsString(Path.of(dir), listFilterType, listOrderType, excludeHiddenFiles, regexp,
				regexpFilesOnly, nameAndType);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls.
	 * 
	 * @param dir                directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @param regexp             if provided is used to only select matching
	 *                           entries, for example files ending in .java
	 * @param regexpFilesOnly    does the regexp apply to both directories and files
	 *                           or just files
	 * @param nameAndType        if true then the name and type will be in the
	 *                           result (e.g. tim.txt (File)), if fals only the name
	 *                           (e.g. tim.txt)
	 * @return the names only or names + type (see nameAndType flag)
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<String> listDirectoryEntriesAsString(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles, String regexp, boolean regexpFilesOnly,
			boolean nameAndType) {
		List<DirectoryEntry> entries = listDirectoryEntries(dir, listFilterType, listOrderType, excludeHiddenFiles,
				regexp, regexpFilesOnly);
		return entries.stream().map(entry -> {
			if (nameAndType) {
				return entry.getNameType();
			} else {
				return entry.getName();
			}
		}).toList();
	}

	/**
	 * From the given directory list the directory contents as directory entries
	 * subject to the controls. Entries determined as hidden by the OS (e.g.
	 * starting with . in Unix / Linux / MacOS, hidden flag for windows) are removed
	 * from the result
	 * 
	 * @param dir             directory to list
	 * @param listFilterType  what entries to return (sub dirs, files or both)
	 * @param listOrderType   how the resulting entries should be ordered
	 * @param regexp          if provided is used to only select matching entries,
	 *                        for example files ending in .java
	 * @param regexpFilesOnly does the regexp apply to both directories and files or
	 *                        just files
	 * @return list of matching entries ordered as requested
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<DirectoryEntry> listDirectoryEntries(String dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, String regexp, boolean regexpFilesOnly) {
		return listDirectoryEntries(Path.of(dir), listFilterType, listOrderType, regexp, regexpFilesOnly);
	}

	/**
	 * From the given directory list the directory contents as directory entries
	 * subject to the controls. Entries determined as hidden by the OS (e.g.
	 * starting with . in Unix / Linux / MacOS, hidden flag for windows) are removed
	 * from the result
	 * 
	 * @param dir             directory to list
	 * @param listFilterType  what entries to return (sub dirs, files or both)
	 * @param listOrderType   how the resulting entries should be ordered
	 * @param regexp          if provided is used to only select matching entries,
	 *                        for example files ending in .java
	 * @param regexpFilesOnly does the regexp apply to both directories and files or
	 *                        just files
	 * @return list of matching entries ordered as requested
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<DirectoryEntry> listDirectoryEntries(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, String regexp, boolean regexpFilesOnly) {
		return listDirectoryEntries(dir, listFilterType, listOrderType, true, regexp, regexpFilesOnly);
	}

	/**
	 * From the given directory list the directory contents as directory entries
	 * subject to the controls.
	 * 
	 * @param dir                directory to list
	 * @param listFilterType     what entries to return (sub dirs, files or both)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list conventions) are not included in the list
	 * @param regexp             if provided is used to only select matching
	 *                           entries, for example files ending in .java
	 * @param regexpFilesOnly    does the regexp apply to both directories and files
	 *                           or just files
	 * @return list of matching entries ordered as requested
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static List<DirectoryEntry> listDirectoryEntries(Path dir, DirectoryListFilterType listFilterType,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles, String regexp, boolean regexpFilesOnly) {
		SortedSet<DirectoryEntry> fileEntries = new TreeSet<>();
		SortedSet<DirectoryEntry> directoryEntries = new TreeSet<>();
		Pattern regexpPattern = regexp != null ? Pattern.compile(regexp) : null;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for (Path p : stream) {
				String name = p.getFileName().toString();
				if (excludeHiddenFiles && Files.isHidden(p)) {
					// we're excluding them and they are hidden, so just go round again
					continue;
				}
				// is is a directory ?
				if (Files.isDirectory(p)) {
					if (listFilterType.isIncludeDirectories()) {
						boolean addit = false;
						// are we looking for a regexp and if so are we checking directory entries with
						// it ?
						if ((regexpPattern != null) && !regexpFilesOnly) {
							if (regexpPattern.matcher(name).find()) {
								addit = true;
							}
						} else {
							// no regexp or we're not checking directoryies, either way just add it
							addit = true;
						}
						if (addit) {
							directoryEntries.add(new DirectoryEntry(name, DirectoryEntry.Type.DIRECTORY, p));
						}
					}
					continue;
				}
				if (Files.isRegularFile(p)) {
					if (listFilterType.isIncludeFiles()) {
						boolean addit = false;
						// are we looking for a regexp ?
						if (regexpPattern != null) {
							if (regexpPattern.matcher(name).find()) {
								addit = true;
							}
						} else {
							// no regexp just add it
							addit = true;
						}
						if (addit) {
							fileEntries.add(new DirectoryEntry(name, DirectoryEntry.Type.FILE, p));
						}
					}
					continue;
				}
				// not a directory or file, no logic to handle it needed
			}
		} catch (IOException | DirectoryIteratorException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can only be thrown by newDirectoryStream.
			System.err.println(x);
		}
		// now work out how to combine the results, if one type was excluded it won't
		// have been added to it's set, so this is an ordering process, not a which set
		// to
		// include process
		List<DirectoryEntry> finalList = new LinkedList<>();
		switch (listOrderType) {
		case DIRECTORIES_FIRST:
			finalList.addAll(directoryEntries);
			finalList.addAll(fileEntries);
			break;
		case FILES_FIRST:
			finalList.addAll(fileEntries);
			finalList.addAll(directoryEntries);
			break;
		case JUST_BY_NAME:
		default:
			// if there is an additional enum problem just do both
			finalList.addAll(fileEntries);
			finalList.addAll(directoryEntries);
			Collections.sort(finalList);
			break;
		}
		return finalList;
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls
	 * 
	 * @param dir                 directory to list
	 * @param listFilterType      what entries to return (sub dirs, files or both)
	 * @param listOrderType       how the resulting entries should be ordered
	 * @param excludeHiddenFiles  if true then entries with as determined as hidden
	 *                            by the OS (e.g. starting with . in Unix / Linux /
	 *                            MacOS, hidden flag for windows) are removed from
	 *                            the list
	 * @param regexp              if provided is used to only select matching
	 *                            entries, for example files ending in .java
	 * @param regexpFilesOnly     does the regexp apply to both directories and
	 *                            files or just files
	 * @param nameAndType         if true then the name and type will be in the
	 *                            result (e.g. tim.txt (File)), if false only the
	 *                            name (e.g. tim.txt)
	 * @param addAbandon          If true then the abandon option will be added at
	 *                            the end of the list and made the default
	 * @param addCurrentDirectory If true then an entry will be made for the current
	 *                            directory (".")
	 * @param addParentDirectory  If true then an entry will be made for the parent
	 *                            directory ("..")
	 * @return the ChoiceDescriptionData object which is ready to be used.
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static ChoiceDescriptionData<DirectoryEntry> buildChoiceDescriptionDataFromDirectory(String dir,
			DirectoryListFilterType listFilterType, DirectoryListOrderType listOrderType, boolean excludeHiddenFiles,
			String regexp, boolean regexpFilesOnly, boolean nameAndType, boolean addAbandon,
			boolean addCurrentDirectory, boolean addParentDirectory) {
		return buildChoiceDescriptionDataFromDirectory(Path.of(dir), listFilterType, listOrderType, excludeHiddenFiles,
				regexp, regexpFilesOnly, nameAndType, addAbandon, addCurrentDirectory, addParentDirectory);
	}

	/**
	 * String to be used as the name for the entry for the current directory
	 */
	public static String currentDirectoryName = ".";

	/**
	 * getter for currentDirectoryName
	 * 
	 * @return the value of currentDirectoryName
	 */
	public String getCurrentDirectoryName() {
		return currentDirectoryName;
	}

	/**
	 * setter for currentDirectoryName
	 * 
	 * Be very careful using this to ensure you don;t use a valid entry name
	 * 
	 * @param newCurrentDirectoryName the new value for cparentDirectoryName
	 */
	public void setCurrentDirectoryName(String newCurrentDirectoryName) {
		currentDirectoryName = newCurrentDirectoryName;
	}

	/**
	 * String to be used as the name for the entry for the current directories
	 * parent
	 */
	public static String parentDirectoryName = "..";

	/**
	 * getter for parentDirectoryName
	 * 
	 * @return the value of parentDirectoryName
	 */
	public String getParentDirectoryName() {
		return parentDirectoryName;
	}

	/**
	 * setter for parentDirectoryName
	 * 
	 * Be very careful using this to ensure you dont use a valid entry name
	 * 
	 * @param newParentDirectoryName the new value for parentDirectoryName
	 */
	public void setParentDirectoryName(String newParentDirectoryName) {
		parentDirectoryName = newParentDirectoryName;
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls
	 * 
	 * @param dir                 directory to list
	 * @param listFilterType      what entries to return (sub dirs, files or both)
	 * @param listOrderType       how the resulting entries should be ordered
	 * @param excludeHiddenFiles  if true then entries with as determined as hidden
	 *                            by the OS (e.g. starting with . in Unix / Linux /
	 *                            MacOS, hidden flag for windows) are removed from
	 *                            the list
	 * @param regexp              if provided is used to only select matching
	 *                            entries, for example files ending in .java
	 * @param regexpFilesOnly     does the regexp apply to both directories and
	 *                            files or just files
	 * @param nameAndType         if true then the name and type will be in the
	 *                            result (e.g. tim.txt (File)), if false only the
	 *                            name (e.g. tim.txt)
	 * @param addAbandon          If true then the abandon option will be added at
	 *                            the end of the list and made the default
	 * @param addCurrentDirectory If true then an entry will be made for the current
	 *                            directory (".") at the top of the list
	 * @param addParentDirectory  If true then an entry will be made for the parent
	 *                            directory ("..") at the top of the list
	 * @return the ChoiceDescriptionData object which is ready to be used.
	 * @see DirectoryListFilterType
	 * @see DirectoryListOrderType
	 */
	public static ChoiceDescriptionData<DirectoryEntry> buildChoiceDescriptionDataFromDirectory(Path dir,
			DirectoryListFilterType listFilterType, DirectoryListOrderType listOrderType, boolean excludeHiddenFiles,
			String regexp, boolean regexpFilesOnly, boolean nameAndType, boolean addAbandon,
			boolean addCurrentDirectory, boolean addParentDirectory) {
		// make sure we have a directory to scan
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException(
					"Provided dir param " + dir.toString() + " is not a directory, cannot scan it");
		}
		List<DirectoryEntry> entries = listDirectoryEntries(dir, listFilterType, listOrderType, excludeHiddenFiles,
				regexp, regexpFilesOnly);
		// if we are adding the parent directory insert it before the current so if both
		// are selected the order will be current, parent, entries
		if (addParentDirectory) {
			// for just "." this will return null, for ./src it will return "."
			// it is important not to normalise this as otherwise .src will be normalized to
			// serc and getParent will at that point fail
			Path parentDir = dir.getParent();
			if (parentDir != null) {
				entries.add(0, new DirectoryEntry(parentDirectoryName, Type.DIRECTORY, parentDir));
			}
		}
		if (addCurrentDirectory) {
			entries.add(0, new DirectoryEntry(currentDirectoryName, Type.DIRECTORY, dir));
		}

		List<ChoiceDescription<DirectoryEntry>> entriesChoices = entries.stream()
				.map(entry -> new ChoiceDescription<DirectoryEntry>(nameAndType ? entry.getNameType() : entry.getName(),
						entry))
				.toList();
		// set it up
		ChoiceDescriptionData<DirectoryEntry> cdd = new ChoiceDescriptionData<>(entriesChoices);
		if (addAbandon) {
			// this will also force it to complete itself, co no need to call that
			// separately
			cdd.addAbandonOption("Cancel", false, true);
		} else {
			// complete it and lock it down
			cdd.completeAndLock();
		}
		return cdd;
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing file to
	 * open, they can navigate around the directories, but cannot chose a directory
	 * itself. Hidden files / directories are not listed
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseFileToOpen(String prompt, String dir) throws IOException {
		return choseFileToOpen(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing file to
	 * open, they can navigate around the directories, but cannot chose a directory
	 * itself. Hidden files / directories are not listed
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseFileToOpen(String prompt, Path dir) throws IOException {
		return choseFileToOpen(prompt, dir, null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing file to
	 * open, they can navigate around the directories, but cannot chose a directory
	 * itself.
	 * 
	 * Filter files only based on the regexp, for example "^.*\\.[Xx][Mm][Ll]$" will
	 * only display files ending in .xml (in any case combination). Hidden files /
	 * directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only files matching the regexp will be displayed,
	 *               directories are not filtered
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseFileToOpen(String prompt, String dir, String regexp) throws IOException {
		return choseFileToOpen(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing file to
	 * open, they can navigate around the directories, but cannot chose a directory
	 * itself.
	 * 
	 * Filter files only based on the regexp, for example "^.*\\.[Xx][Mm][Ll]$" will
	 * only display files ending in .xml (in any case combination). Hidden files /
	 * directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only files matching the regexp will be displayed,
	 *               directories are not filtered
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseFileToOpen(String prompt, Path dir, String regexp) throws IOException {
		return choseFromDirectory(prompt, dir, DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_FILE_AS_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, true, regexp, true, true, true);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file into.
	 * 
	 * Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectory(String prompt, String dir) throws IOException {
		return choseDirectory(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file into.
	 * 
	 * Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectory(String prompt, Path dir) throws IOException {
		return choseDirectory(prompt, dir, null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file into.
	 * 
	 * Filter directories based on the regexp. Hidden files / directories are not
	 * listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only directories matching the regexp will be
	 *               displayed
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectory(String prompt, String dir, String regexp) throws IOException {
		return choseDirectory(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file.
	 * 
	 * Filter directories based on the regexp. Hidden files / directories are not
	 * listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only directories matching the regexp will be
	 *               displayed
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectory(String prompt, Path dir, String regexp) throws IOException {
		return choseFromDirectory(prompt, dir, DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, true, regexp, false, true, true);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file, or an
	 * existing file to overwrite.
	 * 
	 * Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectoryOrFile(String prompt, String dir) throws IOException {
		return choseDirectoryOrFile(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file, or an
	 * existing file to overwrite.
	 * 
	 * Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectoryOrFile(String prompt, Path dir) throws IOException {
		return choseDirectoryOrFile(prompt, dir, null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file, or an
	 * existing file to overwrite.
	 * 
	 * Filter files based on the regexp, directories are not filtered. For example
	 * "^.*\\.[Xx][Mm][Ll]$" will only display files ending in .xml (in any case
	 * combination). Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only files matching the regexp will be displayed
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectoryOrFile(String prompt, String dir, String regexp) throws IOException {
		return choseDirectoryOrFile(prompt, Path.of(dir), null);
	}

	/**
	 * Simple version of the choseFromDirectory methods.
	 * 
	 * Starting from the given directory asks the user to select an existing
	 * directory to open, they can navigate around the directories, files are not
	 * shown. One use case would be to select a directory to write a new file, or an
	 * existing file to overwrite.
	 * 
	 * Filter files based on the regexp, directories are not filtered. For example
	 * "^.*\\.[Xx][Mm][Ll]$" will only display files ending in .xml (in any case
	 * combination). Hidden files / directories are not listed.
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param dir    directory to start in
	 * @param regexp if not null only files matching the regexp will be displayed
	 * @return the selected file, OR, null if the cancel option was chosen
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 */
	public static DirectoryEntry choseDirectoryOrFile(String prompt, Path dir, String regexp) throws IOException {
		return choseFromDirectory(prompt, dir,
				DirectorySelectionMode.SELECT_DIRECTORY_AS_NAVIGATION_OR_LEAF_SELECT_FILE_AS_LEAF,
				DirectoryListOrderType.DIRECTORIES_FIRST, true, regexp, true, true, true);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls
	 * 
	 * Entries shown will depend on the selectionMode, so if the selection mode
	 * limits choices to directories only directories will be shown, but a mode that
	 * allows only files to be chosen will display the directories for navigation
	 * purposes, but they cannot be chosen.
	 * 
	 * Note that for the selection Modes that support navigation it is not possible
	 * to navigate above the initially provided start directory, though navigation
	 * back up to it is allowed.
	 * 
	 * @param prompt             the prompt to use when asking for input
	 * @param dir                directory to list
	 * @param selectionMode      what choices to allow, this will influence what
	 *                           content is chosen and thus indirectly may override
	 *                           sort order for some options (e.g. if only files can
	 *                           be selected then no subdirectories will be shown)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @param regexp             if provided is used to only select matching
	 *                           entries, for example files ending in .java
	 * @param regexpFilesOnly    does the regexp apply to both directories and files
	 *                           or just files
	 * @param nameAndType        if true then the name and type will be in the
	 *                           result (e.g. tim.txt (File)), if false only the
	 *                           name (e.g. tim.txt)
	 * @param addAbandon         If true then the abandon option will be added at
	 *                           the end of the list and made the default
	 * @return the DirectoryEntry selected or null if the abandon choice was taken
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 * @see DirectoryEntry
	 * @see DirectorySelectionMode
	 * @see DirectoryListOrderType
	 */
	public static DirectoryEntry choseFromDirectory(String prompt, String dir, DirectorySelectionMode selectionMode,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles, String regexp, boolean regexpFilesOnly,
			boolean nameAndType, boolean addAbandon) throws IOException {
		return choseFromDirectory(prompt, Path.of(dir), selectionMode, listOrderType, excludeHiddenFiles, regexp,
				regexpFilesOnly, nameAndType, addAbandon);
	}

	/**
	 * From the given directory list the directory entries as strings subject to the
	 * controls.
	 * 
	 * Entries shown will depend on the selectionMode, so if the selection mode
	 * limits choices to directories only directories will be shown, but a mode that
	 * allows only files to be chosen will display the directories for navigation
	 * purposes, but they cannot be chosen.
	 * 
	 * Note that for the selection Modes that support navigation it is not possible
	 * to navigate above the initially provided start directory, though navigation
	 * back up to it is allowed.
	 * 
	 * 
	 * @param prompt             the prompt to use when asking for input, if null or
	 *                           zero length then the selectionMode prompt is used.
	 * @param dir                directory to list
	 * @param selectionMode      what choices to allow, this will influence what
	 *                           content is chosen and thus indirectly may override
	 *                           sort order for some options (e.g. if only files can
	 *                           be selected then no subdirectories will be shown)
	 * @param listOrderType      how the resulting entries should be ordered
	 * @param excludeHiddenFiles if true then entries with as determined as hidden
	 *                           by the OS (e.g. starting with . in Unix / Linux /
	 *                           MacOS, hidden flag for windows) are removed from
	 *                           the list
	 * @param regexp             if provided is used to only select matching
	 *                           entries, for example files ending in .java
	 * @param regexpFilesOnly    does the regexp apply to both directories and files
	 *                           or just files
	 * @param nameAndType        if true then the name and type will be in the
	 *                           result (e.g. tim.txt (File)), if false only the
	 *                           name (e.g. tim.txt)
	 * @param addAbandon         If true then the abandon option will be added at
	 *                           the end of the list and made the default
	 * @return the DirectoryEntry selected or null if the abandon choice was taken
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input.
	 * 
	 * @see DirectoryEntry
	 * @see DirectorySelectionMode
	 * @see DirectoryListOrderType
	 */
	public static DirectoryEntry choseFromDirectory(String prompt, Path dir, DirectorySelectionMode selectionMode,
			DirectoryListOrderType listOrderType, boolean excludeHiddenFiles, String regexp, boolean regexpFilesOnly,
			boolean nameAndType, boolean addAbandon) throws IOException {
		// if what we are given is a file, then build a entry for that and return it
		// this would seem to be the correct behavior, and will also help if doing
		// recursive processing
		if (Files.isRegularFile(dir)) {
			return new DirectoryEntry(dir.getFileName().toString(), DirectoryEntry.Type.FILE, dir);
		}
		ChoiceDescriptionData<DirectoryEntry> cdd = buildChoiceDescriptionDataFromDirectory(dir,
				selectionMode.getDirectoryListFilterType(), listOrderType, excludeHiddenFiles, regexp, regexpFilesOnly,
				nameAndType, addAbandon, selectionMode.isCurrentDirectoryIncludedAsOption(),
				selectionMode.isParentDirectoryIncludedAsOption());
		DirectoryEntry directoryEntry = getParamChoice(
				(((prompt == null || (prompt.length() == 0))) ? selectionMode.getDefaultPrompt() : prompt) + " ("
						+ dir.toString() + ")",
				cdd);

		if (directoryEntry == null) {
			// they chose the abandon, pass that back
			return null;
		}
		// if we are limiting to only the current directory then return it, as the build
		// choice description will have only built entries for the allowed types we
		// don't need to figure out if we're returning a directory when they have chosen
		// a file or anything like that
		if (!selectionMode.isNavigableMode()) {
			return directoryEntry;
		}
		// if what has been chosen is a directory AND it's the current directory AND we
		// are allowing directories to be selected as a leaf return it
		if ((directoryEntry.getType() == Type.DIRECTORY) && (directoryEntry.getName().equals(currentDirectoryName))
				&& selectionMode.isDirectoryAllowedAsLeaf()) {
			return directoryEntry;
		}
		// they are allowed to navigate to a sub directory. recurse, if it's a file it
		// will just be returned, if it's a directory it will either be opened or
		// selected depending on the selection mode (see the handling of that earlier in
		// this code)
		return choseFromDirectory(prompt, directoryEntry.getPath(), selectionMode, listOrderType, excludeHiddenFiles,
				regexp, regexpFilesOnly, nameAndType, addAbandon);
	}

	/**
	 * ask for the date / time with a timezone requested by name and offering a
	 * default of the current TZ
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTimeTimeZone(String prompt) throws IOException {
		return getISODateTimeTimeZone(prompt, true, true, true);
	}

	/**
	 * Asks the user to enter a data and time (but no time zone) returns a string in
	 * the format YYYY-MM-DDTHH:mm Attempts to ensure that the entered data is valid
	 * (e.g. that the day is only allowed values for the month, so 1-30 for april,
	 * 1-31 for March and either 1-28 or 1-29 for Feb. Only works on the Gregorian
	 * calendar
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return a string representing the entered date and time
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTime(String prompt) throws IOException {
		return getISODateTime(prompt, false);
	}

	/**
	 * Asks the user to enter a data and time (but no time zone) returns a string in
	 * the format YYYY-MM-DDTHH:mm
	 * 
	 * if askForTimezone is true then prompts for the timezone to use e.g.
	 * Europe/London and offers a default of the current TZ
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * @param prompt         the prompt to use when asking for input
	 * @param askForTimezone if true the user will be asked to select a timezone
	 * @return a string representing the entered data time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTime(String prompt, boolean askForTimezone) throws IOException {
		return getISODateTimeTimeZone(prompt, askForTimezone, true, true);
	}

	/**
	 * Asks the user to enter a data and time (but no time zone) returns a string in
	 * the format YYYY-MM-DDTHH:mm
	 * 
	 * if askForTimezone is true then prompts for the timezone to use e.g.
	 * Europe/London and offers a default of the current TZ
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return The entered data time, note this is local so there is no timezone
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static LocalDateTime getISOLocalDateTime(String prompt) throws IOException {
		return getISOZonedDateTime(prompt, false, false, false, LocalDateTime.MIN, LocalDateTime.MAX).toLocalDateTime();
	}

	/**
	 * Asks the user to enter a data and time (in whatever TZ) and returns a string
	 * in the format YYYY-MM-DDTHH:mm:SS
	 * 
	 * If you are using a timezone then the time entered should be in the local time
	 * for that timezone, not that for some name based timezones e.g. Europe/Paris
	 * then this also allows for summer time / winter time adjustments (not all TZ
	 * support daylight savings, for example the ones starting with GMT)
	 * 
	 * So for example entering 2023-01-01 12:00:00 with timezone Europe/London
	 * returns an ISO formatted string with no offset, however 2023-06-01 12:00:00
	 * with a TRZ fo Europe/London would return a formatted string with an offset of
	 * +01:00 as that time is a summer time and so one hour ahead
	 * 
	 * if askForTimezone is true then prompt for the timezone
	 * 
	 * if askForTimezoneName is true prompts for a timezone by name, e.g.
	 * Europe/London
	 * 
	 * if askForTimezoneName is false then ask an offset from GMT in terms of hours
	 * an mins, the resulting string will be YYYY-MM-DDTHH:mmZ01:00 (in the case of
	 * british summer time which is 1 hours ahead of GMT, or YYYY-MM-DDTHH:mmZ04:30)
	 * for India which is 4 1/2 hours ahead.
	 * 
	 * if defaulToCurrentTimezone is true then will prompt for the timezone based on
	 * the current timezone name / offset
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offser in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt                   the prompt to use when asking for input
	 * @param askForTimezone           if true the user will ba asked for a timezone
	 * @param askForTimezoneName       if asking for timezone they will be asked by
	 *                                 name, if false by offset
	 * @param defaultToCurrentTimezone if trie the timezone will default to the
	 *                                 current if they user just presses return
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTimeTimeZone(String prompt, boolean askForTimezone, boolean askForTimezoneName,
			boolean defaultToCurrentTimezone) throws IOException {
		return getISODateTimeTimeZone(prompt, askForTimezone, askForTimezoneName, defaultToCurrentTimezone,
				LocalDateTime.MIN, LocalDateTime.MAX);
	}

	/**
	 * Asks the user to enter a data and time (in whatever TZ) and returns a string
	 * in the format YYYY-MM-DDTHH:mm:SS
	 * 
	 * If you are using a timezone then the time entered should be in the local time
	 * for that timezone, not that for some name based timezones e.g. Europe/Paris
	 * then this also allows for summer time / winter time adjustments (not all TZ
	 * support daylight savings, for example the ones starting with GMT)
	 * 
	 * So for example entering 2023-01-01 12:00:00 with timezone Europe/London
	 * returns an ISO formatted string with no offset, however 2023-06-01 12:00:00
	 * with a TRZ fo Europe/London would return a formatted string with an offset of
	 * +01:00 as that time is a summer time and so one hour ahead
	 * 
	 * if askForTimezone is true then prompt for the timezone
	 * 
	 * if askForTimezoneName is true prompts for a timezone by name, e.g.
	 * Europe/London
	 * 
	 * if askForTimezoneName is false then ask an offset from GMT in terms of hours
	 * an mins, the resulting string will be YYYY-MM-DDTHH:mmZ01:00 (in the case of
	 * british summer time which is 1 hours ahead of GMT, or YYYY-MM-DDTHH:mmZ04:30)
	 * for India which is 4 1/2 hours ahead.
	 * 
	 * if defaulToCurrentTimezone is true then will prompt for the timezone based on
	 * the current timezone name / offset
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offser in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt                   the prompt to use when asking for input
	 * @param askForTimezone           if true the user will ba asked for a timezone
	 * @param askForTimezoneName       if asking for timezone they will be asked by
	 *                                 name, if false by offset
	 * @param defaultToCurrentTimezone if trie the timezone will default to the
	 *                                 current if they user just presses return
	 * @param mindtg                   the entered info must be &gt;= this
	 * @param maxdtg                   the entered info must be &lt;= this
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTimeTimeZone(String prompt, boolean askForTimezone, boolean askForTimezoneName,
			boolean defaultToCurrentTimezone, LocalDateTime mindtg, LocalDateTime maxdtg) throws IOException {
		return getISOZonedDateTime(prompt, askForTimezone, askForTimezoneName, defaultToCurrentTimezone, mindtg, maxdtg)
				.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	/**
	 * Asks the user to enter a data and time (in the local or whatever TZ they
	 * enter) in the format YYYY-MM-DDTHH:mm:SS
	 * 
	 * If you are asking for a timezone then the time entered should be in the local
	 * time for that timezone, not that for some name based timezones e.g.
	 * Europe/Paris then this also allows for summer time / winter time adjustments
	 * (not all TZ support daylight savings, for example the ones starting with GMT)
	 * 
	 * So for example entering 2023-01-01 12:00:00 with timezone Europe/London
	 * returns an ISO formatted string with no offset, however 2023-06-01 12:00:00
	 * with a TRZ fo Europe/London would return a formatted string with an offset of
	 * +01:00 as that time is a summer time and so one hour ahead
	 * 
	 * if askForTimezone is true then prompt for the timezone
	 * 
	 * if askForTimezone is false then the current (i.e. system local) TZ is used.
	 * 
	 * if askForTimezoneName is true prompts for a timezone by name, e.g.
	 * Europe/London
	 * 
	 * if askForTimezoneName is false then ask an offset from GMT in terms of hours
	 * an mins, the resulting string will be YYYY-MM-DDTHH:mmZ01:00 (in the case of
	 * british summer time which is 1 hours ahead of GMT, or YYYY-MM-DDTHH:mmZ04:30)
	 * for India which is 4 1/2 hours ahead.
	 * 
	 * if defaulToCurrentTimezone is true then will prompt for the timezone based on
	 * the current timezone name / offset
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offser in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt                   the prompt to use when asking for input
	 * @param askForTimezone           if true the user will ba asked for a timezone
	 * @param askForTimezoneName       if asking for timezone they will be asked by
	 *                                 name, if false by offset
	 * @param defaultToCurrentTimezone if true the timezone will default to the
	 *                                 current if they user just presses return
	 * @param mindtg                   the entered info must be &gt;= this
	 * @param maxdtg                   the entered info must be &lt;= this
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static ZonedDateTime getISOZonedDateTime(String prompt, boolean askForTimezone, boolean askForTimezoneName,
			boolean defaultToCurrentTimezone, LocalDateTime mindtg, LocalDateTime maxdtg) throws IOException {
		if (mindtg.isAfter(maxdtg)) {
			throw new IOException("Zoned DTG mindtg " + mindtg.format(DateTimeFormatter.ISO_DATE_TIME)
					+ " cannot be before maxdtg " + maxdtg.format(DateTimeFormatter.ISO_DATE_TIME));
		}
		LocalDate date = getISOLocalDate(prompt, mindtg.toLocalDate(), maxdtg.toLocalDate());
		// if the date chosen was the min date, then need to limit the time to the min
		// dtg element, otherwise it can be midnight for the min
		LocalTime minTime = mindtg.toLocalDate().isEqual(date) ? mindtg.toLocalTime() : LocalTime.MIN;
		// likewise if the data chosen was the max date then limit toe max time to
		// whaetever is in it
		LocalTime maxTime = maxdtg.toLocalDate().isEqual(date) ? maxdtg.toLocalTime() : LocalTime.MAX;
		LocalTime time = getISOLocalTime(prompt, minTime, maxTime);
		LocalDateTime dtg = LocalDateTime.of(date, time);
		if (askForTimezone) {
			if (askForTimezoneName) {
				ZoneId zoneId = getTimeZoneByName(prompt, defaultToCurrentTimezone);
				ZonedDateTime zdt = ZonedDateTime.of(dtg, zoneId);
				return zdt;
			} else {
				ZoneOffset tz = getISOOffsetTimeZoneOffset(prompt, defaultToCurrentTimezone);
				ZonedDateTime zdt = ZonedDateTime.of(dtg, tz.normalized());
				return zdt;
			}
		}
		return ZonedDateTime.of(dtg, ZoneId.systemDefault());
	}

	/**
	 * Asks the user to enter a full date, time and in the specified time zone
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param zoneId the timezone to use
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTimeTimeZone(String prompt, ZoneId zoneId) throws IOException {
		return getISODateTimeTimeZone(prompt, zoneId, LocalDateTime.MIN, LocalDateTime.MAX);
	}

	/**
	 * Asks the user to enter the data and time in the specified timezone where the
	 * entered data must be &gt;= mindtg and &lt;= maxdtg
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param zoneId the timezone to use
	 * @param mindtg the entered info must be &gt;= this
	 * @param maxdtg the entered info must be &lt;= this
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODateTimeTimeZone(String prompt, ZoneId zoneId, LocalDateTime mindtg,
			LocalDateTime maxdtg) throws IOException {
		return getISOZonedDateTimeTimeZone(prompt, zoneId, mindtg, maxdtg).format(DateTimeFormatter.ISO_DATE_TIME);
	}

	/**
	 * Asks the user to enter the data and time in the specified timezone where the
	 * entered data must be &gt;= mindtg and &lt;= maxdtg
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @param zoneId the timezone to use
	 * @param mindtg the entered info must be &gt;= this
	 * @param maxdtg the entered info must be &lt;= this
	 * @return a string representing the entered date time and tz
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static ZonedDateTime getISOZonedDateTimeTimeZone(String prompt, ZoneId zoneId, LocalDateTime mindtg,
			LocalDateTime maxdtg) throws IOException {
		String zoneInfo = " (in timezone " + zoneId.getId() + ")";
		LocalDate mindate = mindtg.toLocalDate();
		LocalDate maxdate = maxdtg.toLocalDate();
		LocalDate date = getISOLocalDate(prompt + zoneInfo, mindate, maxdate);
		// if the date is the first date then the min time is that within the day, if
		// however the selected date is before the minimum date then the minimum date
		// time is the begining of the day
		LocalTime minTime = mindate.isEqual(date) ? mindtg.toLocalTime() : LocalTime.MIN;
		// if the date is the last date then the max time is that within the day, if
		// however the selected date is before the maximum date then the maximum date
		// time is the end of the day
		LocalTime maxTime = maxdate.isEqual(date) ? maxdtg.toLocalTime() : LocalTime.MAX;
		LocalTime time = getISOLocalTime(prompt + zoneInfo, minTime, maxTime);
		LocalDateTime ldt = LocalDateTime.of(date, time);
		ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
		return zdt;
	}

	/**
	 * prompts for an offset from GMT in terms of hours an mins, the resulting
	 * string will be 01:00 (in the case of british summer time which is 1 hours
	 * ahead of GMT, or 04:30) for India which is 4 1/2 hours ahead.
	 * 
	 * The UI will use gmt as the default values
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered offset (default to the current offset if the user just
	 *         presses return)
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISOTimeZoneOffset(String prompt) throws IOException {
		return getISOTimeZoneOffset(prompt, true);
	}

	/**
	 * prompts for an offset from GMT in terms of hours an mins, the resulting
	 * string will be 01:00 (in the case of british summer time which is 1 hours
	 * ahead of GMT, or 04:30) for India which is 4 1/2 hours ahead.
	 * 
	 * The UI will use gmt as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offset in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt                the prompt to use when asking for input
	 * @param defaulToCurrentOffset if true default to the current offset if the
	 *                              user just presses return
	 * @return the entered offset
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISOTimeZoneOffset(String prompt, boolean defaulToCurrentOffset) throws IOException {
		return getISOOffsetTimeZoneOffset(prompt, defaulToCurrentOffset).getId();
	}

	/**
	 * prompts for an offset from GMT in terms of hours an mins, the resulting
	 * string will be 01:00 (in the case of british summer time which is 1 hours
	 * ahead of GMT, or 04:30) for India which is 4 1/2 hours ahead.
	 * 
	 * The UI will use gmt as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offset in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt                the prompt to use when asking for input
	 * @param defaulToCurrentOffset if true default to the current offset if the
	 *                              user just presses return
	 * @return the entered offset
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static ZoneOffset getISOOffsetTimeZoneOffset(String prompt, boolean defaulToCurrentOffset)
			throws IOException {
		int offset = defaulToCurrentOffset ? ZonedDateTime.now().getOffset().getTotalSeconds() : 0;
		int hoursOffsetDefault = offset / 3600;
		int minsOffsetDefault = (offset % 3600) / 60;
		int hoursOffset = getInt("Please enter time zone offset hours from GMT for " + prompt, NumberInputOption.RANGE,
				-12, 12, hoursOffsetDefault);
		int minsOffset;
		if (hoursOffset < 0) {
			minsOffset = getInt("Please enter time zone offset mins from GMT for " + prompt, NumberInputOption.RANGE,
					-59, 0, minsOffsetDefault);
		} else {
			minsOffset = getInt("Please enter time zone offset mins from GMT for " + prompt, NumberInputOption.RANGE, 0,
					59, minsOffsetDefault);
		}
		return ZoneOffset.ofHoursMinutes(hoursOffset, minsOffset);
	}

	/**
	 * Asks the user to enter the timezone based on it's name e.g. Europe/London
	 * 
	 * The selector presents a list of "top level" timezones and timezone groupings
	 * first e.g. America, Europe, GMT, ACT etc.
	 * 
	 * If the selection is a timezone rather than a region of timezones then it is
	 * returned
	 * 
	 * If the selection is a regio nof timezones then a list of regions and
	 * timezones within that region is presented and the loop continues until a
	 * timezone is actually selected.
	 * 
	 * The following only applies if defaultToCurrentTimezone is true :
	 * 
	 * Before selection starts the local timezone region will be presented as the
	 * default option, if the users selects the default by pressing enter it will be
	 * returned
	 * 
	 * If the user enters a "full" TZ e..g. America/North_Dakota/Beulah it will be
	 * checked to see if it's a valid TZ and if so returned
	 * 
	 * If the user enteres a partial TZ ahat is valied e.g. America or
	 * America/North_Dakota then selection will progress from that point through the
	 * TZ tree.
	 * 
	 * @param prompt                the prompt to use when asking for input
	 * @param defaulToCurrentOffset if true default to the current offset if the
	 *                              user just presses return
	 * @return the entered offset
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	private static Map<String, Set<String>> tzRegions;
	private static Set<String> tzTopLevel;
	private final static String TZ_SEPARATOR = "/";

	/**
	 * 
	 * Gets the name of a timezone
	 * 
	 * @param prompt                  the prompt to use when asking for input
	 * @param defaulToCurrentTimeZone if true will offer the current tx as a default
	 *                                if the user presses return
	 * @return a string of the selected tx
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getTimeZoneNameByName(String prompt, boolean defaulToCurrentTimeZone) throws IOException {
		return getTimeZoneByName(prompt, defaulToCurrentTimeZone).toString();
	}

	/**
	 * Asks the user to select a timezone by name, if they press return and
	 * defaultToCurrentTimeZOne is true then the current TZ is used
	 * 
	 * @param prompt                  the prompt to use when asking for input
	 * @param defaulToCurrentTimeZone if true default to the current timezone if the
	 *                                user just presses return
	 * @return the entered offset
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static ZoneId getTimeZoneByName(String prompt, boolean defaulToCurrentTimeZone) throws IOException {
		setupTZDataStructures();
		var currentOptionsSet = tzTopLevel;
		String currentTZLevel = "";
		int levelCounter = 0;
		String currentTZ = TimeZone.getDefault().getID();
		if (defaulToCurrentTimeZone) {
			// ask to use the TZ with the provided as the default
			String chosenTZ = TextIOUtils.getString(
					"Please enter a timezone, press enter to use the default or enter something that is nto a current timezone (e.g. n) to entert the time zone selector",
					currentTZ);
			boolean knownTZName = tzRegions.containsKey(chosenTZ);
			var possibleChildren = tzRegions.get(chosenTZ);
			if (knownTZName && (possibleChildren == null)) {
				// we know of this zone and it is a leaf so has no children
				return ZoneId.of(chosenTZ);
			} else if (knownTZName && (possibleChildren != null)) {
				// we know of this zone but it is not a leaf so has children, set things up to
				// start selecting from there
				currentTZLevel = chosenTZ;
				currentOptionsSet = possibleChildren;
				levelCounter = chosenTZ.split(TZ_SEPARATOR).length;
				TextIOUtils.doOutput(chosenTZ + " is a region, starting selection from there");
			} else {
				TextIOUtils.doOutput("Sorry, but " + chosenTZ + " is not known, starting selection");
			}
		}
		while (currentOptionsSet != null) {
			var choiceDescriptions = new LinkedList<ChoiceDescription<String>>();
			for (String option : currentOptionsSet) {
				var optionWithPath = levelCounter == 0 ? option : currentTZLevel + TZ_SEPARATOR + option;
				var children = tzRegions.get(optionWithPath);
				int childCount = children == null ? 0 : children.size();
				var desc = childCount == 0 ? " TimeZone" : " Region with " + childCount + " entries";
				choiceDescriptions.add(new ChoiceDescription<>(option, desc));
			}
			var cdd = new ChoiceDescriptionData<>(choiceDescriptions);
			var choice = TextIOUtils.getStringChoice(prompt, cdd);
			// String choice = cdd.getChoice(chosenOption);
			// from the current currentOptionsSet find all of the children
			// try to move down a level
			currentTZLevel = levelCounter == 0 ? choice : currentTZLevel + TZ_SEPARATOR + choice;
			// "move" down a level
			currentOptionsSet = tzRegions.get(currentTZLevel);
			levelCounter++;
		}
		return ZoneId.of(currentTZLevel);
	}

	/**
	 * if we need the TZ stuff build it, but on demand
	 */
	private static void setupTZDataStructures() {
		// if needed do lazy initialization of the TZ structures
		if (tzRegions == null) {
			tzRegions = new HashMap<>();
			tzTopLevel = new TreeSet<>();
			var ids = ZoneId.getAvailableZoneIds();
			for (var tz : ids) {
				var tzPathComponents = tz.split(TZ_SEPARATOR);
				tzTopLevel.add(tzPathComponents[0]);
				var tzPath = "";
				for (int i = 0; i < tzPathComponents.length; i++) {
					// build the path string up
					tzPath = i == 0 ? tzPathComponents[i] : tzPath + TZ_SEPARATOR + tzPathComponents[i];
					// if we've reached the end of the path then add it to the map with a null value
					if (i == tzPathComponents.length - 1) {
						tzRegions.put(tzPath, null);
					} else {
						// there is at least one more sub element (path or actuall TZ) beneath this one
						// if there's not a set of suib elements there already for this path build and
						// add one
						var tzChildren = tzRegions.get(tzPath);
						if (tzChildren == null) {
							tzChildren = new TreeSet<>();
							tzRegions.put(tzPath, tzChildren);
						}
						tzChildren.add(tzPathComponents[i + 1]);
					}
				}
			}
		}
	}

	/**
	 * Prompts the user to enter a time in 24 hours format, the current local time
	 * is used as the default
	 * 
	 * The return string is of the format HH:MM:SS
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entred time
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISOTime(String prompt) throws IOException {
		return getISOTime(prompt, LocalTime.MIN, LocalTime.MAX);
	}

	/**
	 * Prompts the user to enter a time in 24 hours format, the current local time
	 * is used as the default
	 * 
	 * The return string is of the format HH:MM:SS
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param mintime the entered time must be &gt;= this
	 * @param maxtime the entered time must be &lt;= this
	 * @return the entered time
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISOTime(String prompt, LocalTime mintime, LocalTime maxtime) throws IOException {
		return getISOLocalTime(prompt, mintime, maxtime).format(DateTimeFormatter.ISO_LOCAL_TIME);
	}

	/**
	 * Prompts the user to enter a time in 24 hours format, the current local time
	 * is used as the default.
	 * 
	 * The return string is of the format HH:MM:SS
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered time
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static LocalTime getISOLocalTime(String prompt) throws IOException {
		return getISOLocalTime(prompt, LocalTime.MIN, LocalTime.MAX);
	}

	/**
	 * Prompts the user to enter a time in 24 hours format, the current local time
	 * is used as the default the time must be within the mintime and max time
	 * 
	 * The return string is of the format HH:MM:SS
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered time
	 * @param mintime the entered time must be &gt;= this
	 * @param maxtime the entered time must be &lt;= this
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 * 
	 */
	public static LocalTime getISOLocalTime(String prompt, LocalTime mintime, LocalTime maxtime) throws IOException {
		if (mintime.isAfter(maxtime)) {
			throw new IOException("Mintime " + mintime.format(DateTimeFormatter.ISO_TIME) + " can't be after maxtime "
					+ maxtime.format(DateTimeFormatter.ISO_TIME));
		}
		int minhour = mintime.getHour();
		int maxhour = maxtime.getHour();
		int defaulthour = LocalTime.now().getHour();
		if (defaulthour < minhour) {
			defaulthour = minhour;
		}
		if (defaulthour > maxhour) {
			defaulthour = maxhour;
		}
		int hour = getInt("Please enter hours for " + prompt, NumberInputOption.RANGE, minhour, maxhour, defaulthour);

		int minmin = mintime.getHour() == hour ? mintime.getMinute() : 0;
		int maxmin = maxtime.getHour() == hour ? maxtime.getMinute() : 59;
		int defaultmin = LocalTime.now().getMinute();
		if (defaultmin < minmin) {
			defaultmin = minmin;
		}
		if (defaultmin > maxmin) {
			defaultmin = maxmin;
		}
		int mins = getInt("Please enter mins for " + prompt, NumberInputOption.RANGE, minmin, maxmin, defaultmin);

		int minsec = ((mintime.getHour() == hour) && (mintime.getMinute() == mins)) ? mintime.getSecond() : 0;
		int maxsec = ((maxtime.getHour() == hour) && (maxtime.getMinute() == mins)) ? maxtime.getSecond() : 59;
		int defaultsec = LocalTime.now().getSecond();
		if (defaultsec < minsec) {
			defaultsec = minsec;
		}
		if (defaultsec > maxsec) {
			defaultsec = maxsec;
		}
		int secs = getInt("Please enter secs for " + prompt, NumberInputOption.RANGE, minsec, maxsec, defaultsec);
		return LocalTime.of(hour, mins, secs);
	}

	/**
	 * Prompts the user to enter a date with any allowed date, the current local
	 * date is used as the default.
	 * 
	 * The day of the month will be restricted to the actual number of days in that
	 * month in that year (so for Feb 28 normally, but 28 in leap years
	 * 
	 * The return string is of the format YYYY-MM-DD
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered date
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODate(String prompt) throws IOException {
		return getISODate(prompt, LocalDate.MIN, LocalDate.MAX);
	}

	/**
	 * Prompts the user to enter a date, which the current local date is used as the
	 * default.
	 * 
	 * The day of the month will be restricted to the actual number of days in that
	 * month in that year (so for Seb 28 normally, but 28 in leap years
	 * 
	 * The return string is of the format YYYY-MM-DD
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param mindate the entered date must be &gt;= this
	 * @param maxdate the entered date must be &lt;= this
	 * @return the entered date
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static String getISODate(String prompt, LocalDate mindate, LocalDate maxdate) throws IOException {
		return getISOLocalDate(prompt, mindate, maxdate).format(DateTimeFormatter.ISO_DATE);
	}

	/**
	 * Prompts the user to enter a date , which the current local date is used as
	 * the default.
	 * 
	 * The day of the month will be restricted to the actual number of days in that
	 * month in that year (so for Seb 28 normally, but 28 in leap years
	 * 
	 * The return string is of the format YYYY-MM-DD
	 * 
	 * @param prompt the prompt to use when asking for input
	 * @return the entered date
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static LocalDate getISOLocalDate(String prompt) throws IOException {
		return getISOLocalDate(prompt, LocalDate.MIN, LocalDate.MAX);
	}

	/**
	 * Prompts the user to enter a date , which the current local date is used as
	 * the default.
	 * 
	 * The day of the month will be restricted to the actual number of days in that
	 * month in that year (so for Seb 28 normally, but 28 in leap years
	 * 
	 * The return string is of the format YYYY-MM-DD
	 * 
	 * @param prompt  the prompt to use when asking for input
	 * @param mindate the entered date must be &gt;= this
	 * @param maxdate the entered date must be &lt;= this
	 * @return the entered date
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static LocalDate getISOLocalDate(String prompt, LocalDate mindate, LocalDate maxdate) throws IOException {
		if (maxdate.isBefore(mindate)) {
			throw new IOException("Provided maximum date " + maxdate.format(DateTimeFormatter.ISO_DATE)
					+ " is before the minimum date " + mindate.format(DateTimeFormatter.ISO_DATE));
		}
		LocalDate ld = LocalDate.now();
		if (ld.isBefore(mindate)) {
			ld = mindate;
		}
		if (ld.isAfter(maxdate)) {
			ld = maxdate;
		}
		int year = getInt("Please enter year for " + prompt, NumberInputOption.RANGE, mindate.getYear(),
				maxdate.getYear(), ld.getYear());
		// given the year what are the options for months - it might be we have a
		// restricted number if the year is at the begining or end of the range
		int minmonth = mindate.getYear() == year ? mindate.getMonthValue() : 1;
		int maxmonth = maxdate.getYear() == year ? maxdate.getMonthValue() : 12;
		int defaultmonth = ld.getMonthValue();
		if (defaultmonth < minmonth) {
			defaultmonth = minmonth;
		}
		if (defaultmonth > maxmonth) {
			defaultmonth = maxmonth;
		}
		int month = getInt("Please enter month in " + year + " for " + prompt, NumberInputOption.RANGE, minmonth,
				maxmonth, defaultmonth);

		// work out the day of the month, whihs can vary betwene 28 & 31 depending on
		// the month (and possibly leap year) and also
		// need to be bounded by the min / max numbers
		LocalDate selectedMonthFirstDay = ld.withDayOfMonth(1).withYear(year).withMonth(month);
		int maxDayOfSelectedMonth = selectedMonthFirstDay.lengthOfMonth();
		int minday = ((mindate.getYear() == year) && (mindate.getMonthValue() == month)) ? mindate.getDayOfMonth() : 1;
		int maxday = ((maxdate.getYear() == year) && (maxdate.getMonthValue() == month)) ? maxdate.getDayOfMonth()
				: maxDayOfSelectedMonth;
		int currentDayOfMonth = ld.getDayOfMonth();
		if (currentDayOfMonth < minday) {
			currentDayOfMonth = minday;
		}
		if (currentDayOfMonth > maxday) {
			currentDayOfMonth = maxday;
		}
		int day = getInt("Please enter day within " + year + " and month " + month + " for " + prompt,
				NumberInputOption.RANGE, minday, maxday, currentDayOfMonth);
		return LocalDate.of(year, month, day);
	}

	/**
	 * Converts a number to a minimum of two digits in the resulting string
	 * 
	 * @param number the number to convert
	 * @return the String representation at a minimum of 2 characters (i.e with
	 *         leading zero if required)
	 */
	public static String toTwoDigit(int number) {
		if (number <= -10) {
			return "" + number;
		} else if (number < 0) {
			return "-0" + (number * -1);
		} else if (number < 10) {
			return "0" + number;
		} else {
			return "" + number;
		}
	}

	/**
	 * Converts a number to a minimum of four digits in the resulting string
	 * 
	 * @param number the number to convert
	 * @return the String representation at a minimum of 4 characters (i.e with
	 *         leading zeros if required)
	 */
	public static String toFourDigit(int number) {
		if (number <= -1000) {
			return "" + number;
		} else if (number <= -100) {
			return "-0" + (number * -1);
		} else if (number <= -10) {
			return "-00" + (number * -1);
		} else if (number < 0) {
			return "-000" + (number * -1);
		} else if (number == 0) {
			return "0000";
		} else if (number < 10) {
			return "000" + number;
		} else if (number < 100) {
			return "00" + number;
		} else if (number < 1000) {
			return "0" + number;
		} else {
			return "" + number;
		}
	}

	/**
	 * Given an enum constant builds a choice description data for all the values of
	 * the enum class, setting the specific value of the enum constant as the
	 * default
	 * 
	 * @param <T>          the enum class
	 * @param enumconstant the enum value to use to determine the enum and the
	 *                     default
	 * @return the generated ChoiceDescriptionData
	 */
	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromSampleEnumValue(
			Enum<T> enumconstant) {
		return buildChoiceDescriptionDataFromSampleEnumValue(enumconstant, true);
	}

	/**
	 * Given an enum constant builds a choice description data for all the values of
	 * the enum class, if setAsDefault is true setting the specific value of the
	 * enum constant as the default
	 * 
	 * @param <T>          the enum class
	 * @param enumconstant the enum value to use to determine the enum and the
	 *                     default
	 * @param setAsDefault if true a default entry is set for the enum value, if
	 *                     false no default is created
	 * @return the generated ChoiceDescriptionData
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromSampleEnumValue(
			Enum<T> enumconstant, boolean setAsDefault) {
		ChoiceDescriptionData<Enum<T>> cdd = buildChoiceDescriptionDataFromEnumClass(
				(Class<? extends Enum<T>>) enumconstant.getClass());
		if (setAsDefault) {
			ChoiceDescription<Enum<T>> defaultChoice = cdd.locateChoiceDescriptionByParam(enumconstant);
			if (defaultChoice == null) {
				// this should not be possible as we build it using all of the enum values but
				// defensive programming and all that
				System.err.println("Major programming problem, cannot locate the CDD with param " + enumconstant
						+ " unable to set it as default");
			} else {
				cdd.setDefaultByChoiceDescription(defaultChoice);
			}
		}
		return cdd;
	}

	/**
	 * Given the enum class build a ChoiceDescriptionData representing all of the
	 * constants in the enum
	 * 
	 * @param <T>       The class of the enum
	 * @param enumclass The class of the enum
	 * @return the generated ChoiceDescriptionData
	 */
	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromEnumClass(
			Class<? extends Enum<T>> enumclass) {
		return buildChoiceDescriptionDataFromEnumValues(enumclass.getEnumConstants());
	}

	/**
	 * Given an array of possible the enum constants build a ChoiceDescriptionData
	 * representing the supplied values
	 * 
	 * @param <T>           The class of the enum
	 * @param enumconstants An array of enum constants
	 * @return the generated ChoiceDescriptionData
	 */
	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromEnumValues(
			Enum<T> enumconstants[]) {
		ChoiceDescriptionData<Enum<T>> cdd = new ChoiceDescriptionData<>();
		for (Enum<T> enumconstant : enumconstants) {
			ChoiceDescription<Enum<T>> cd = new ChoiceDescription<>(enumconstant.toString(), enumconstant);
			cdd.addChoiceDescription(cd);
		}
		return cdd;
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and reset any choices back to not selected so the choice
	 * description data can be reused.
	 * 
	 * The result may be an empty list or a list containing up to all of the options
	 * provided.
	 * 
	 * @param <P>                   the type of param in the choice description data
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more choice descriptions of the selected
	 *         options
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static <P> List<ChoiceDescription<P>> makeMultiChoiceChoiceDescriptionSelection(String prompt,
			ChoiceDescriptionData<P> choiceDescriptionData) throws IOException {
		return makeMultiChoiceChoiceDescriptionSelection(prompt, choiceDescriptionData, true);
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and reset any choices back to not selected so the choice
	 * description data can be reused.
	 * 
	 * The result may be an empty list or a list containing up to all of the options
	 * provided.
	 * 
	 * As part of the process the selection data can be optionally cleared or
	 * retained for later use allowing a follow on with the already selected items
	 * still in place
	 * 
	 * @param <P>                   the type of param in the choice description data
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @param clearSelection        if true then the selection will be cleared, if
	 *                              false it will be retained
	 * @return a list containing zero or more choice descriptions of the selected
	 *         options
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static <P> List<ChoiceDescription<P>> makeMultiChoiceChoiceDescriptionSelection(String prompt,
			ChoiceDescriptionData<P> choiceDescriptionData, boolean clearSelection) throws IOException {
		if (choiceDescriptionData == null) {
			throw new IllegalArgumentException("ChoiceDescriptionData cannot be null");
		}
		// the choice must have the multi choice / abandon option set as otherwise we
		// can't figure out our "escape"
		if (!choiceDescriptionData.isMultiChoiceComplete()) {
			throw new IllegalArgumentException(
					"ChoiceDescriptionData must have a multiChoiceComplete / abandon option added, "
							+ choiceDescriptionData.toString());
		}
		while (true) {
			int choice = getIntChoice(prompt, choiceDescriptionData);
			if (choiceDescriptionData.isMultiChoiceComplete(choice)) {
				break;
			}
			ChoiceDescription<P> cd = choiceDescriptionData.getChoiceDescription(choice);
			cd.toggleSelected();
		}
		// now get the selected items, this will reset the selection status
		if (clearSelection) {
			return choiceDescriptionData.getMultiChoiceSelectedChoiceDescriptionsAndClearSelections();
		} else {
			return choiceDescriptionData.getMultiChoiceSelectedChoiceDescriptions();
		}
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and reset any choices back to not selected so the choice
	 * description data can be reused.
	 * 
	 * The result may be an empty list or a list containing up to all of the options
	 * provided.
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more strings of the option, one from each
	 *         of the selected choice descriptions
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceOptionSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData) throws IOException {
		return makeMultiChoiceOptionSelection(prompt, choiceDescriptionData, true);
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and if clearSelection is true reset any choices back to not
	 * selected so the choice description data can be reused.
	 * 
	 * The result may be an empty list or a list containing up to all of the options
	 * provided.
	 * 
	 * As part of the process the selection data can be optionally cleared or
	 * retained for later use allowing a follow on with the already selected items
	 * still in place
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @param clearSelection        if true then the selection will be cleared, if
	 *                              false it will be retained
	 * @return a list containing zero or more strings of the option, one from each
	 *         of the selected choice descriptions
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceOptionSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData, boolean clearSelection) throws IOException {
		return makeMultiChoiceChoiceDescriptionSelection(prompt, choiceDescriptionData, clearSelection).stream()
				.map(cd -> cd.getOption()).toList();
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and reset any choices back to not selected so the choice
	 * description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the description from up
	 * to all of the options provided.
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more strings of the description, one from
	 *         each of the selected choice descriptions
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceDescriptionSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData) throws IOException {
		return makeMultiChoiceDescriptionSelection(prompt, choiceDescriptionData, true);
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and if clearSelection is true reset any choices back to not
	 * selected so the choice description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the description from up
	 * to all of the options provided.
	 * 
	 * As part of the process the selection data can be optionally cleared or
	 * retained for later use allowing a follow on with the already selected items
	 * still in place
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @param clearSelection        if true then the selection will be cleared, if
	 *                              false it will be retained
	 * @return a list containing zero or more strings of the description, one from
	 *         each of the selected choice descriptions
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceDescriptionSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData, boolean clearSelection) throws IOException {
		return makeMultiChoiceChoiceDescriptionSelection(prompt, choiceDescriptionData, clearSelection).stream()
				.map(cd -> cd.getDescription()).toList();
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and reset any choices back to not selected so the choice
	 * description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the additional from up
	 * to all of the options provided.
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more strings of the additional, one from
	 *         each of the selected choice descriptions
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceAdditionalSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData) throws IOException {
		return makeMultiChoiceAdditionalSelection(prompt, choiceDescriptionData, true);
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the choices they have
	 * made in a list and if clearSelection is true reset any choices back to not
	 * selected so the choice description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the additional from up
	 * to all of the options provided.
	 * 
	 * As part of the process the selection data can be optionally cleared or
	 * retained for later use allowing a follow on with the already selected items
	 * still in place
	 * 
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more strings of the additional, one from
	 *         each of the selected choice descriptions
	 * @param clearSelection if true then the selection will be cleared, if false it
	 *                       will be retained
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static List<String> makeMultiChoiceAdditionalSelection(String prompt,
			ChoiceDescriptionData<?> choiceDescriptionData, boolean clearSelection) throws IOException {
		return makeMultiChoiceChoiceDescriptionSelection(prompt, choiceDescriptionData, clearSelection).stream()
				.map(cd -> cd.getAdditional()).toList();
	}

	/**
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the param of the choices
	 * they have made in a list and reset any choices back to not selected so the
	 * choice description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the param from up to all
	 * of the options provided.
	 * 
	 * @param <P>                   the type of param in the choice description data
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @return a list containing zero or more params from the choice descriptions of
	 *         the selected options
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static <P> List<P> makeMultiChoiceParamSelection(String prompt,
			ChoiceDescriptionData<P> choiceDescriptionData) throws IOException {
		return makeMultiChoiceParamSelection(prompt, choiceDescriptionData, true);
	}

	/**
	 * 
	 * 
	 * Allow the user to make a choice zero or times until they chose the multi
	 * choice complete option. For each choice toggle the selected state for the
	 * choice (if it's selectable).
	 * 
	 * The choice description data MUST have had a addMultiChoiceComplete call made
	 * against it so the code knows what option indicates the user is finished, if
	 * it doesn't then an IllegalArgumentException is thrown
	 * 
	 * Once the user had chosen the complete option return the param of the choices
	 * they have made in a list and if clearSelection is true reset any choices back
	 * to not selected so the choice description data can be reused.
	 * 
	 * The result may be an empty list or a list containing the param from up to all
	 * of the options provided.
	 * 
	 * As part of the process the selection data can be optionally cleared or
	 * retained for later use allowing a follow on with the already selected items
	 * still in place
	 * 
	 * @param <P>                   the type of param in the choice description data
	 * @param prompt                the prompt to display when chosing
	 * @param choiceDescriptionData the data to offer as choices
	 * @param clearSelection        if true then the selection will be cleared, if
	 *                              false it will be retained
	 * @return a list containing zero or more params from the choice descriptions of
	 *         the selected options
	 * @throws IOException if there is a problem setting up the reader on the input
	 *                     or reading the input
	 */
	public static <P> List<P> makeMultiChoiceParamSelection(String prompt,
			ChoiceDescriptionData<P> choiceDescriptionData, boolean clearSelection) throws IOException {
		return makeMultiChoiceChoiceDescriptionSelection(prompt, choiceDescriptionData, clearSelection).stream()
				.map(cd -> cd.getParam()).toList();
	}
}
