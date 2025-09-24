package timgutilities.textio.test;

import java.io.IOException;

import timgutilities.textio.ChoiceDescriptionData;
import timgutilities.textio.TextIOUtils;

public class EnumChoiceBuilderTester {

	public enum myoptions {
		TOM("mot"), DICK("kcid"), HARRY("yrrah");

		private String name;

		private myoptions(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static void main(String[] args) throws IOException {
		TextIOUtils.doOutput("Testing from enum values array");
		ChoiceDescriptionData<Enum<myoptions>> cddvalues = TextIOUtils
				.buildChoiceDescriptionDataFromEnumValues(myoptions.values());
		Enum<myoptions> choicevalues = TextIOUtils.getParamChoice("Please chose from", cddvalues);
		String respvalues = choicevalues.name();
		TextIOUtils.doOutput("Using values you selected an option with name " + respvalues + " of type "
				+ choicevalues.getClass().getName());
		TextIOUtils.doOutput("Testing from enum class");
		ChoiceDescriptionData<Enum<myoptions>> cddclass = TextIOUtils
				.buildChoiceDescriptionDataFromEnumClass(myoptions.class);
		Enum<myoptions> choiceclass = TextIOUtils.getParamChoice("Please chose from", cddclass);
		String respclass = choiceclass.name();
		TextIOUtils.doOutput("Using class you selected an option with name " + respclass + " of type "
				+ choiceclass.getClass().getName());
		TextIOUtils.doOutput("Testing from enum specific value");
		ChoiceDescriptionData<Enum<myoptions>> cddvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		cddvalue.setDoSort(true);
		cddvalue.addAbandonOption("Quit", true);
		Enum<myoptions> choicevalue = TextIOUtils.getParamChoice("Please chose from", cddvalue);
		String respvalue = choicevalue.name();
		TextIOUtils.doOutput("Using a value you selected an option with name " + respvalue + " of type "
				+ choicevalue.getClass().getName());
	}

}
