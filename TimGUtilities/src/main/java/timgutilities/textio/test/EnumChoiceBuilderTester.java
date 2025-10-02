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
		TextIOUtils.doOutput("Using values you selected an option with name " + respvalues);

		TextIOUtils.doOutput("Testing from enum class");
		ChoiceDescriptionData<Enum<myoptions>> cddclass = TextIOUtils
				.buildChoiceDescriptionDataFromEnumClass(myoptions.class);
		Enum<myoptions> choiceclass = TextIOUtils.getParamChoice("Please chose from", cddclass);
		String respclass = choiceclass.name();
		TextIOUtils.doOutput("Using class you selected an option with name " + respclass);

		TextIOUtils.doOutput("Testing from enum specific value no default");
		// this should use HARRY as the default
		ChoiceDescriptionData<Enum<myoptions>> cddnodefaultvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY, false);
		cddnodefaultvalue.setDoSort(true);
		cddnodefaultvalue.addAbandonOption("Quit");
		Enum<myoptions> choicenodefaultvalue = TextIOUtils.getParamChoice("Please chose from", cddnodefaultvalue);
		if (choicenodefaultvalue == null) {
			TextIOUtils.doOutput("Abandon option chosen");
		} else {
			String respnodefaultvalue = choicenodefaultvalue.name();
			TextIOUtils.doOutput("Using a value you selected an option with name " + respnodefaultvalue);
		}

		TextIOUtils.doOutput("Testing from enum specific value abandon is default");
		// this should use HARRY as the default
		ChoiceDescriptionData<Enum<myoptions>> cddabandonfaultvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		cddabandonfaultvalue.setDoSort(true);
		cddabandonfaultvalue.addAbandonOption("Quit", true, true);
		Enum<myoptions> choiceabandonfaultvalue = TextIOUtils.getParamChoice("Please chose from", cddabandonfaultvalue);
		if (choiceabandonfaultvalue == null) {
			TextIOUtils.doOutput("Abandon option chosen");
		} else {
			String respabandonfaultvalue = choiceabandonfaultvalue.name();
			TextIOUtils.doOutput("Using a value you selected an option with name " + respabandonfaultvalue);
		}

		TextIOUtils.doOutput(
				"Testing from enum specific value no abandon - the default should be " + myoptions.HARRY.name);
		// this should use HARRY as the default
		ChoiceDescriptionData<Enum<myoptions>> cddvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		// cddvalue.setDoSort(true);
		Enum<myoptions> choicevalue = TextIOUtils.getParamChoice("Please chose from", cddvalue);
		String respvalue = choicevalue.name();
		TextIOUtils.doOutput("Using a value you selected an option with name " + respvalue);

		TextIOUtils.doOutput(
				"Testing from enum specific value sorted no abandon override value based default - the default should be "
						+ myoptions.TOM.name);
		// this should use HARRY as the default
		ChoiceDescriptionData<Enum<myoptions>> cddvaluedick = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		// but we override the default with tom
		cddvaluedick.setDoSort(true);
		cddvaluedick.setDefaultByParam(myoptions.TOM);
		Enum<myoptions> choicevaluedick = TextIOUtils.getParamChoice("Please chose from", cddvaluedick);
		String respvaluedick = choicevaluedick.name();
		TextIOUtils.doOutput("Using a value you selected an option with name " + respvaluedick);

		TextIOUtils.doOutput("Testing using numeric default value no abandon, ");
		ChoiceDescriptionData<Enum<myoptions>> cddnumericdefaultvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY);
		cddnumericdefaultvalue.setDefaultByIndex(1);
		Enum<myoptions> choicenumericdefaultvalue = TextIOUtils
				.getParamChoice("Please chose from (default should be option 1)", cddnumericdefaultvalue);
		String respnumericdefaultvalue = choicenumericdefaultvalue.name();
		TextIOUtils.doOutput("Using a value you selected an option with name " + respnumericdefaultvalue);

		TextIOUtils.doOutput("Testing using no enum default but abandon default then numeric default value");
		ChoiceDescriptionData<Enum<myoptions>> cddabandonnumericdefaultvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY, false);
		cddabandonnumericdefaultvalue.addAbandonOption(true);
		cddabandonnumericdefaultvalue.setDefaultByIndex(1);
		Enum<myoptions> choiceabandonnumericdefaultvalue = TextIOUtils
				.getParamChoice("Please chose from (default should be option 1)", cddabandonnumericdefaultvalue);
		if (choiceabandonnumericdefaultvalue == null) {
			TextIOUtils.doOutput("Abandon option selected");
		} else {
			String respabandonnumericdefaultvalue = choiceabandonnumericdefaultvalue.name();
			TextIOUtils.doOutput("Using a value you selected an option with name " + respabandonnumericdefaultvalue);
		}

		TextIOUtils.doOutput("Testing using  enum default then numeric default value, then abandon overriding it");
		ChoiceDescriptionData<Enum<myoptions>> cddnumericabandondefaultvalue = TextIOUtils
				.buildChoiceDescriptionDataFromSampleEnumValue(myoptions.HARRY, false);
		cddnumericabandondefaultvalue.setDefaultByIndex(1);
		cddnumericabandondefaultvalue.addAbandonOption(true, true);
		Enum<myoptions> choicenumericabandondefaultvalue = TextIOUtils
				.getParamChoice("Please chose from (default should be abandon)", cddnumericabandondefaultvalue);
		if (choicenumericabandondefaultvalue == null) {
			TextIOUtils.doOutput("Abandon option chosen");
		} else {
			String respnumericabandondefaultvalue = choicenumericabandondefaultvalue.name();
			TextIOUtils.doOutput("Using a value you selected an option with name " + respnumericabandondefaultvalue);
		}
	}
}
