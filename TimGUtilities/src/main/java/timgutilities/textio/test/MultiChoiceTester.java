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

import timgutilities.textio.ChoiceDescription;
import timgutilities.textio.ChoiceDescriptionData;
import timgutilities.textio.TextIOUtils;

public class MultiChoiceTester {
	public enum myoptions {
		TOM, DICK, HARRY, CLAIRE, TIM, ANDRII;
	}

	public static void main(String[] args) throws IOException {
		ChoiceDescriptionData<String> cdd = new ChoiceDescriptionData<>(new ChoiceDescription<String>("One"),
				new ChoiceDescription<String>("Two").setNotSelectable(), new ChoiceDescription<String>("Three"),
				new ChoiceDescription<String>("Four").setNotSelectable(), new ChoiceDescription<String>("Five"));
		cdd.addMultiChoiceCompleteOption();
		List<String> choices = TextIOUtils.makeMultiChoiceOptionSelection("Please chose the options you want", cdd,
				false);
		TextIOUtils.doOutput("First choice output" + choices.toString());
		choices = TextIOUtils.makeMultiChoiceOptionSelection(
				"Please chose the options you want building on the previous choices", cdd);
		TextIOUtils.doOutput("Building on previous choice output" + choices.toString());
		choices = TextIOUtils.makeMultiChoiceOptionSelection(
				"Please chose the options you want (all selections should be cleared)", cdd);
		TextIOUtils.doOutput("Reset choice output" + choices.toString());

		// let's see if this works with an enum
		TextIOUtils.doOutput("Testing from enum class, but with no default set");
		ChoiceDescriptionData<Enum<myoptions>> cddenum = TextIOUtils
				.buildChoiceDescriptionDataFromEnumClass(myoptions.class);
		cddenum.addMultiChoiceCompleteOption();
		List<Enum<myoptions>> choiceEnums = TextIOUtils.makeMultiChoiceParamSelection("Please chose from", cddenum);
		TextIOUtils.doOutput("Selected enum entries are " + choiceEnums);

		TextIOUtils.doOutput("Testing from enum instance, but with no default set");
		cddenum = TextIOUtils.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		cddenum.addMultiChoiceCompleteOption();
		choiceEnums = TextIOUtils.makeMultiChoiceParamSelection("Please chose from", cddenum);
		TextIOUtils.doOutput("Selected enum entries are " + choiceEnums);
	}

}
