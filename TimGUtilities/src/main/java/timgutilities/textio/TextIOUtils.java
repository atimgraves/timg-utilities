/*Copyright (c) 2025 Tim Graves.

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public final static String DEFAULT_PAUSE_STRING = "Please press enter or return to continue";
	private static BufferedReader br;

	/**
	 * If needed setup the appropriate buffered reader on Standard-in
	 */
	private static void setupInput() {
		if (br == null) {
			br = new BufferedReader(new InputStreamReader(System.in));
		}
	}

	/**
	 * Output text in a manner that workswith prompts form the input routines
	 * 
	 * @param output
	 */
	public static void doOutput(String output) {
		System.out.println(output);
	}

	/**
	 * Waits for the user tp press return - useful if you need to allow for
	 * something in a separate environment to complete (e.g. the provisioning of a
	 * service in a cloud)
	 * 
	 * @throws IOException
	 */
	public static void pauseBeforeProgressing() throws IOException {
		pauseBeforeProgressing(DEFAULT_PAUSE_STRING);
	}

	/**
	 * Displays the prompt and then waits for the user tp press return - useful if
	 * you need to allow for something in a separate environment to complete (e.g.
	 * the provisioning of a service in a cloud)
	 * 
	 * @param prompt
	 * @throws IOException
	 */
	public static void pauseBeforeProgressing(String prompt) throws IOException {
		getString(prompt, "", true);
	}

	/**
	 * Outputs the prompt and then waits for the user to enter zero or more
	 * characters which are then returned.
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static String getString(String prompt, String defaultValue) throws IOException {
		return getString(prompt, defaultValue, false);
	}

	/**
	 * Displays the provided prompt and asks for input. If the defaultValue is non
	 * null that is diplayed and entering nothing (e.g. just pressing return) will
	 * return the default value given, if there is not default value and
	 * allowEmptyInput is false then pressing return results in a notice that input
	 * is required, if allowEmptyInput is true and the user just presses return the
	 * the empty string is returned
	 * 
	 * @param prompt
	 * @param defaultValue
	 * @param allowEmptyInput
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static boolean getYN(String prompt, boolean defaultValue) throws IOException {
		prompt = prompt + "(y/n)";
		while (true) {
			String res = getString(prompt, defaultValue ? "y" : "n");
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
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static boolean getYN(String prompt) throws IOException {
		prompt = prompt + "(y/n)";
		while (true) {
			String res = getString(prompt);
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
	 * RunnableCommands runable is called. If the runnable throws an exception it's
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
	 * If the user choses the quit option they weiio
	 * 
	 * @param prompt
	 * @param runableCommands
	 * @param insertQuit
	 * @throws IOException
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
	 * If the user choses the quit option they weiio
	 * 
	 * @param prompt
	 * @param runableCommands
	 * @param insertQuit
	 * @param confirmQuit
	 * @throws IOException
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
	 * If the user choses the quit option they weiio
	 * 
	 * @param prompt
	 * @param runableCommands
	 * @param insertQuit
	 * @param confirmQuit
	 * @param quitFirst
	 * @throws IOException
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
	 * Displays the prompt, then the strings int he choice description data entries.
	 * The user is prompted to enter an integer representing the choice which it
	 * then returned to the caller if it represents one of the choices.
	 * 
	 * @param prompt
	 * @param choiceDescriptionData
	 * @return
	 * @throws IOException
	 */

	public static int getIntChoice(String prompt, ChoiceDescriptionData<?> choiceDescriptionData) throws IOException {
		String processedPrompt = prompt;
		if (processedPrompt == null) {
			processedPrompt = "Please chose from";
		}
		if (choiceDescriptionData == null) {
			throw new IOException("ChoiceDescriptionData cannot be null");
		}
		if (choiceDescriptionData.length() == 0) {
			throw new IOException("Must provide at least once choice option");
		}
		// if there is only one option then return it automatically
		if (choiceDescriptionData.length() == 1) {
			System.out.println(
					"Only option " + choiceDescriptionData.getChoice(0) + " is available, selecting it for you");
			return 0;
		}
		processedPrompt = processedPrompt + "\nOptions are ";
		int choices = choiceDescriptionData.length();
		for (int i = 0; i < choices; i++) {
			processedPrompt = processedPrompt + "\n[" + i + "] = " + choiceDescriptionData.getData(i);
		}
		return getInt(processedPrompt, NUM_TYPE.SELECTION, 0, choices - 1);
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options array. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static int getStringChoice(String prompt, String options[]) throws IOException {
		return getIntChoice(prompt, new ChoiceDescriptionData<>(options));
	}

	/**
	 * 
	 * Displays the prompt, then the strings in the options collection. The user is
	 * prompted to enter an integer representing the choice which is then returned
	 * to the caller if it represents one of the choices.
	 * 
	 * @param prompt
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static int getIntChoice(String prompt, Collection<String> options) throws IOException {
		return getIntChoice(prompt, new ChoiceDescriptionData<>(options));
	}

	/**
	 * Displays the prompt, then the strings in the choice description data entries.
	 * The user is prompted to enter an integer representing the choice, if it
	 * represents one of the choices then the string for that choice is returned.
	 * 
	 * @param prompt
	 * @param choiceDescriptionData
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static String getStringChoice(String prompt, Collection<String> options) throws IOException {
		ChoiceDescriptionData<String> cdd = new ChoiceDescriptionData<>(options);
		return getStringChoice(prompt, cdd);
	}

	/**
	 * Displayes the choices in the choide decrription data, askes the user to enter
	 * a number representing the choice. If the choice is the "abandon" option (See
	 * ChoiceDescriptionData.addAbandion methods) then null is returned, otherwise
	 * returns the object in the parameter for the individual choice descriptions
	 * 
	 * @param <T>
	 * @param prompt
	 * @param choiceDescriptionData
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public static String getString(String prompt, String options[]) throws IOException {
		return options[getStringChoice(prompt, options)];
	}

	/**
	 * Asks the user to enter an integer in base 10, this is parsed and if that
	 * fails then the user is re-prompted, if the parsing succeeds then the entered
	 * number is returned to the caller
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt) throws IOException {
		return getInt(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Asks the user to enter an integer in base 16 (hex), this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static int getIntHex(String prompt) throws IOException {
		return getIntBase(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, 16);
	}

	/**
	 * Asks the user to enter an integer in an arbitrary base, this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static int getIntBase(String prompt, int base) throws IOException {
		return getIntBase(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, base);
	}

	/**
	 * Asks the user to enter an integer in base 10, this is parsed and if that
	 * fails then the user is re-prompted, if the parsing succeeds then the entered
	 * number is returned to the caller.
	 * 
	 * If the suer just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt, int defaultValue) throws IOException {
		return getInt(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, 10);
	}

	/**
	 * Asks the user to enter an integer in base 16 (hex), this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller.
	 * 
	 * If the suer just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getIntHex(String prompt, int defaultValue) throws IOException {
		return getInt(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, 16);
	}

	/**
	 * Asks the user to enter an integer in an arbitrary base, this is parsed and if
	 * that fails then the user is re-prompted, if the parsing succeeds then the
	 * entered number is returned to the caller.
	 * 
	 * If the suer just presses return (no input) then the defaultValue is returned
	 * 
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getIntBase(String prompt, int defaultValue, int base) throws IOException {
		return getInt(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, base);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the user is prompted to have another go
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper) throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, 10);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue) throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, 10);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the user is prompted to have another go
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static int getIntHex(String prompt, NUM_TYPE type, int lower, int upper) throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, 16);
	}

	/**
	 * Prompt the user to enter a base 16 (HEX) integer number relative to the upper
	 * / lower, if no number is entered then the default value is used
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getIntHex(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue)
			throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, 16);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the user is prompted to have
	 * another go
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static int getIntBase(String prompt, NUM_TYPE type, int lower, int upper, int base) throws IOException {
		return getInt(prompt, type, lower, upper, false, 0, base);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lower limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static int getIntBase(String prompt, NUM_TYPE type, int lower, int upper, int defaultValue, int base)
			throws IOException {
		return getInt(prompt, type, lower, upper, true, defaultValue, base);
	}

	/**
	 * Prompt the user to enter a base 10 integer number relative to the upper /
	 * lower, if no number is entered then the default value is used. If useDefault
	 * is true then the value of the default is displayed after the text describing
	 * the restriction, if false it is not
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param useDefault
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, boolean useDefault, int defaultValue)
			throws IOException {
		return getInt(prompt, type, lower, upper, useDefault, defaultValue, 10);
	}

	/**
	 * Prompt the user to enter an arbitrary base integer number relative to the
	 * upper / lower, if no number is entered then the default value is used. If
	 * useDefault is true then the value of the default is displayed after the text
	 * describing the restriction, if false it is not
	 * 
	 * The NUM_TYPE is interpreted as follows, if the restriction is not met then
	 * the user is prompted for input again. Note that a bit of text explaining the
	 * restriction is displayed for all NUM_TYPEs except SELECTION which is a
	 * special case of RANGE intended to be used for entering from a list of
	 * options.
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param useDefault
	 * @param defaultValue
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static int getInt(String prompt, NUM_TYPE type, int lower, int upper, boolean useDefault, int defaultValue,
			int base) throws IOException {
		String restriction = "";
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
			restriction = "Unknown input restrictions, this is a programming error, defaulting to allowing any input";
			type = NUM_TYPE.ANY_NUM;
			break;
		}
		int result;
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
			if (type == NUM_TYPE.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NUM_TYPE.RANGE) || (type == NUM_TYPE.SELECTION)) {
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
	 * See getInt(prompt) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt) throws IOException {
		return getLong(prompt, NUM_TYPE.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, 10);
	}

	/**
	 * See getIntHex(prompt) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLongHex(String prompt) throws IOException {
		return getLongBase(prompt, NUM_TYPE.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, 16);
	}

	/**
	 * See getIntBase(prompt) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLongBase(String prompt, int base) throws IOException {
		return getLongBase(prompt, NUM_TYPE.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, base);
	}

	/**
	 * See getInt(prompt, defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt, long defaultValue) throws IOException {
		return getLong(prompt, NUM_TYPE.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, true, defaultValue, 10);
	}

	/**
	 * See getIntHex(prompt, defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLongHex(String prompt, long defaultValue) throws IOException {
		return getLong(prompt, NUM_TYPE.ANY_NUM, Long.MIN_VALUE, Long.MAX_VALUE, true, defaultValue, 16);
	}

	/**
	 * See getIntBase(prompt, defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static long getLongBase(String prompt, long defaultValue, int base) throws IOException {
		return getLong(prompt, NUM_TYPE.ANY_NUM, Integer.MIN_VALUE, Integer.MAX_VALUE, true, defaultValue, base);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper) throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, 10);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, 10);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static long getLongHex(String prompt, NUM_TYPE type, long lower, long upper) throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, 16);
	}

	/**
	 * see getIntHex(prompt, type, lower, upper, defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static long getLongHex(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue)
			throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, 16);
	}

	/**
	 * see getInt(prompt, type, lower, upper, base) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static long getLongBase(String prompt, NUM_TYPE type, long lower, long upper, int base) throws IOException {
		return getLong(prompt, type, lower, upper, false, 0, base);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, defaultValue, base) but returns a
	 * Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static long getLongBase(String prompt, NUM_TYPE type, long lower, long upper, long defaultValue, int base)
			throws IOException {
		return getLong(prompt, type, lower, upper, true, defaultValue, base);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, useDefault, useDefault,
	 * defaultValue) but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param useDefault
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, boolean useDefault,
			long defaultValue) throws IOException {
		return getLong(prompt, type, lower, upper, useDefault, defaultValue, 10);
	}

	/**
	 * see getLongBase(prompt, type, lower, upper, useDefault, defaultValue, base)
	 * but returns a Long
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @param base
	 * @return
	 * @throws IOException
	 */
	public static long getLong(String prompt, NUM_TYPE type, long lower, long upper, boolean useDefault,
			long defaultValue, int base) throws IOException {
		String restriction = "";
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
			restriction = "Unknown input restrictions, this is a programming error, defaulting to allowing any input";
			type = NUM_TYPE.ANY_NUM;
			break;
		}
		long result;
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
			if (type == NUM_TYPE.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NUM_TYPE.RANGE) || (type == NUM_TYPE.SELECTION)) {
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
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static double getDouble(String prompt) throws IOException {
		return getDouble(prompt, NUM_TYPE.ANY_NUM, Double.MIN_VALUE, Double.MAX_VALUE);
	}

	/**
	 * See getInt(prompt, defaultValue) but returns a double
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static double getDouble(String prompt, double defaultValue) throws IOException {
		return getDouble(prompt, NUM_TYPE.ANY_NUM, Double.MIN_VALUE, Double.MAX_VALUE, true, defaultValue);
	}

	/**
	 * see getInt(prompt, type, lower, upper) but returns a double
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @return
	 * @throws IOException
	 */
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper) throws IOException {
		return getDouble(prompt, type, lower, upper, false, 0.0);
	}

	/**
	 * see getInt(prompt, type, lower, upper, defaultValue) but returns a double
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper, double defaultValue)
			throws IOException {
		return getDouble(prompt, type, lower, upper, true, defaultValue);
	}

	/**
	 * see getIntBase(prompt, type, lower, upper, useDefault, useDefault,
	 * defaultValue) but returns a double
	 * 
	 * @param prompt
	 * @param type
	 * @param lower
	 * @param upper
	 * @param useDefault
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	public static double getDouble(String prompt, NUM_TYPE type, double lower, double upper, boolean useDefault,
			double defaultValue) throws IOException {
		String restriction = "";
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
		}
		double result;
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
			if (type == NUM_TYPE.AT_OR_ABOVE) {
				if (result < lower) {
					System.out.println("Invalid input, must be >= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.ABOVE) {
				if (result <= lower) {
					System.out.println("Invalid input, must be > " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.AT_OR_BELOW) {
				if (result > lower) {
					System.out.println("Invalid input, must be <= " + lower);
					continue;
				}
			} else if (type == NUM_TYPE.BELOW) {
				if (result >= lower) {
					System.out.println("Invalid input, must be < " + lower);
					continue;
				}
			} else if ((type == NUM_TYPE.RANGE) || (type == NUM_TYPE.SELECTION)) {
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
	 * defined the various operations when entering numbers with upper / lowers.
	 * 
	 * These enum values are interpreted as follows :
	 * 
	 * ANY_NUM - ignores the lower / upper limits
	 * 
	 * AT_OR_ABOVE - input must be >= lower limit, upper limit is ignored
	 * 
	 * ABOVE - input must be > lower limit, upper limit is ignored
	 * 
	 * AT_OR_BELOW - input must be <= lower limit, upper limit is ignored
	 * 
	 * BELOW - input must be < lower limit, upper limit is ignored
	 * 
	 * RANGE - input must be >= lowwer limit and <= upper limit
	 * 
	 * SELECTION - the same a RANGE, but no restriction text is displayed
	 */
	public enum NUM_TYPE {
		ANY_NUM, AT_OR_ABOVE, ABOVE, AT_OR_BELOW, BELOW, RANGE, SELECTION;
	}

	/**
	 * Asks the user to enter a string representing a file mame and path which can
	 * be relative or absolute, ensures that the entered string represents an
	 * existing file (not a directory) if it's not a file then requests another go
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static String getFile(String prompt) throws IOException {
		return getFile(prompt, null, null);
	}

	/**
	 * Asks the user to enter a string representing a file mame and path which can
	 * be relative or absolute, ensures that the entered string represents an
	 * existing file (not a directory) if it's not a file then requests another go,
	 * if the user enters and empty string then the defaultValue is used
	 * 
	 * @param prompt
	 * @param defaultValue
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param startLocation
	 * @return
	 * @throws IOException
	 */
	public static String getFileUnder(String prompt, String startLocation) throws IOException {
		return getFileUnder(prompt, null, null);
	}

	/**
	 * Asks the user to enter a string representing a file name and path which must
	 * be relative to the starting location, ensures that the entered string
	 * represents an existing file (not a directory) if it's not a file then
	 * requests another go, if the user enters and empty string then the
	 * defaultValue is used
	 * 
	 * @param prompt
	 * @param startLocation
	 * @param defaultValue
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param regexp
	 * @param defaultValue
	 * @return
	 * @throws IOException
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
	 * defaultValue is used
	 * 
	 * @param prompt
	 * @param regexp
	 * @param defaultValue
	 * @return
	 * @throws IOException
	 */
	// Note the regexp (if not null)_ is applied to the filename ony, the rest of
	// the path is ignored
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
	 * @param prompt
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @return
	 * @throws IOException
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
	 * Asks the user to enter a data and time (but no time zone) returns a string in
	 * the format YYYY-MM-DDTHH:mm Attempts to ensure that the entered data is valid
	 * (e.g. that the day is only allowed values for the month, so 1-30 for april,
	 * 1-31 for March and either 1-28 or 1-29 for Feb. Only works on the Gregorian
	 * calendar
	 * 
	 * The UI will use the current time / date as the default values
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
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
	 * @param prompt
	 * @param askForTimezone
	 * @return
	 * @throws IOException
	 */
	public static String getISODateTime(String prompt, boolean askForTimezone) throws IOException {
		return getISODateTimeTimeZone(prompt, askForTimezone, true, true);
	}

	/**
	 * Asks the user to enter a data and time (but no time zone) returns a string in
	 * the format YYYY-MM-DDTHH:mm:SS
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
	 * @param prompt
	 * @param askForTimezone
	 * @param askForTimezoneName
	 * @param defaultToCurrentTimezone
	 * @return
	 * @throws IOException
	 */
	public static String getISODateTimeTimeZone(String prompt, boolean askForTimezone, boolean askForTimezoneName,
			boolean defaultToCurrentTimezone) throws IOException {
		String date = getISODate(prompt);
		String time = getISOTime(prompt);
		String dtg = date + "T" + time;
		if (askForTimezone) {
			if (askForTimezoneName) {
				ZoneId zoneId = getTimeZoneByName(prompt, defaultToCurrentTimezone);
				LocalDateTime ldt = LocalDateTime.parse(dtg);
				ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
				dtg = zdt.format(DateTimeFormatter.ISO_DATE_TIME);
			} else {
				String tz = getISOTimeZoneOffset(prompt, defaultToCurrentTimezone);
				dtg = dtg + tz;
			}
		}
		return dtg;
	}

	/**
	 * ask for the date / time with a timezone requested by name and offering a
	 * default of the current TZ
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static String getISODateTimeTimeZone(String prompt) throws IOException {
		return getISODateTimeTimeZone(prompt, true, true, true);
	}

	/**
	 * 
	 * @param prompt
	 * @param zoneId
	 * @return
	 * @throws IOException
	 */
	public static String getISODateTimeTimeZone(String prompt, ZoneId zoneId) throws IOException {
		String zoneInfo = " (in timezone " + zoneId.getId() + ")";
		String date = getISODate(prompt + zoneInfo);
		String time = getISOTime(prompt + zoneInfo);
		String dtg = date + "T" + time;
		LocalDateTime ldt = LocalDateTime.parse(dtg);
		ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
		return zdt.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	/**
	 * prompts for an offset from GMP in terms of hours an mins, the resulting
	 * string will be 01:00 (in the case of british summer time which is 1 hours
	 * ahead of GMT, or 04:30) for India which is 4 1/2 hours ahead.
	 * 
	 * The UI will use gmt as the default values
	 * 
	 * @param prompt
	 * @param askForTimezone
	 * @param defaulToCurrentOffset
	 * @return
	 * @throws IOException
	 */
	public static String getISOTimeZoneOffset(String prompt) throws IOException {
		return getISOTimeZoneOffset(prompt, true);
	}

	/**
	 * prompts for an offset from GMP in terms of hours an mins, the resulting
	 * string will be 01:00 (in the case of british summer time which is 1 hours
	 * ahead of GMT, or 04:30) for India which is 4 1/2 hours ahead.
	 * 
	 * The UI will use gmt as the default values
	 * 
	 * If defaultToCurrentOffsett is true then the current offset in hours and mins
	 * will be offered as the TZ offset hours and mins
	 * 
	 * @param prompt
	 * @param askForTimezone
	 * @param defaulToCurrentOffset
	 * @return
	 * @throws IOException
	 */
	public static String getISOTimeZoneOffset(String prompt, boolean defaulToCurrentOffset) throws IOException {

		int offset = defaulToCurrentOffset ? ZonedDateTime.now().getOffset().getTotalSeconds() : 0;
		int hoursOffsetDefault = offset / 3600;
		int minsOffsetDefault = (offset % 3600) / 60;
		int hoursOffset = getInt("Please enter time zone offset hours from GMT for " + prompt, NUM_TYPE.RANGE, -12, 12,
				hoursOffsetDefault);
		int minsOffset = getInt("Please enter time zone offset mins from GMT for " + prompt, NUM_TYPE.RANGE, 0, 59,
				minsOffsetDefault);
		String resp = hoursOffset < 0 ? "-" : "+";
		return resp + toTwoDigit(hoursOffset) + ":" + toTwoDigit(minsOffset);
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
	 * @param prompt
	 * @param defaulToCurrentOffset
	 * @return
	 * @throws IOException
	 */
	private static Map<String, Set<String>> tzRegions;
	private static Set<String> tzTopLevel;
	private final static String TZ_SEPARATOR = "/";

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
	 * 
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
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static String getISOTime(String prompt) throws IOException {
		LocalTime lt = LocalTime.now();
		int hour = getInt("Please enter hours for " + prompt, NUM_TYPE.RANGE, 0, 23, lt.getHour());
		int mins = getInt("Please enter mins for " + prompt, NUM_TYPE.RANGE, 0, 60, lt.getMinute());
		int secs = getInt("Please enter secs for " + prompt, NUM_TYPE.RANGE, 0, 60, lt.getSecond());
		return "" + toTwoDigit(hour) + ":" + toTwoDigit(mins) + ":" + toTwoDigit(secs);
	}

	/**
	 * Prompts the user to enter a date, the current local date is used as the
	 * default.
	 * 
	 * The day of the month will be restricted to the actual number of days in that
	 * month in that year (so for Seb 28 normally, but 28 in leap years
	 * 
	 * The return string is of the format YYYY-MM-DD
	 * 
	 * @param prompt
	 * @return
	 * @throws IOException
	 */
	public static String getISODate(String prompt) throws IOException {
		LocalDate ld = LocalDate.now();
		int year = getInt("Please enter year for " + prompt, NUM_TYPE.RANGE, ld.getYear(), ld.getYear() + 10,
				ld.getYear());
		int month = getInt("Please enter month in " + year + " for " + prompt, NUM_TYPE.RANGE, 1, 12,
				ld.getMonthValue());
		int currentDayOfMonth = ld.getDayOfMonth();
		LocalDate selectedMonth = ld.withDayOfMonth(1).withYear(year).withMonth(month);
		int maxDayOfSelectedMonth = selectedMonth.lengthOfMonth();
		if (currentDayOfMonth > maxDayOfSelectedMonth) {
			currentDayOfMonth = maxDayOfSelectedMonth;
		}
		int day = getInt("Please enter day within " + year + " and month " + month + " for " + prompt, NUM_TYPE.RANGE,
				1, maxDayOfSelectedMonth, currentDayOfMonth);
		return "" + year + "-" + toTwoDigit(month) + "-" + toTwoDigit(day);
	}

	private static String toTwoDigit(int number) {
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

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromSampleEnumValue(
			Enum<T> enumconstant) {
		return buildChoiceDescriptionDataFromEnumClass((Class<? extends Enum<T>>) enumconstant.getClass());
	}

	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromEnumClass(
			Class<? extends Enum<T>> enumclass) {
		return buildChoiceDescriptionDataFromEnumValues(enumclass.getEnumConstants());
	}

	public static <T extends Enum<T>> ChoiceDescriptionData<Enum<T>> buildChoiceDescriptionDataFromEnumValues(
			Enum<T> enumconstants[]) {
		ChoiceDescriptionData<Enum<T>> cdd = new ChoiceDescriptionData<>();
		for (Enum<T> enumconstant : enumconstants) {
			ChoiceDescription<Enum<T>> cd = new ChoiceDescription<>(enumconstant.toString(), enumconstant);
			cdd.addChoiceDescription(cd);
		}
		return cdd;
	}
}
