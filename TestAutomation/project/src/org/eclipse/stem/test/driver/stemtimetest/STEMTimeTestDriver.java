package org.eclipse.stem.test.driver.STEMtimetest;
import org.eclipse.stem.core.model.ModelFactory;
import org.eclipse.stem.core.model.STEMTime;
import org.eclipse.stem.test.driver.TestReporter;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import org.eclipse.emf.ecore.EObject;


public class STEMTimeTestDriver{

	public static void main(String[] args) {
		//parses command line args into usable variables
		int TEST_INCREMENT = Integer.parseInt(args[0]);
		boolean oracle = Boolean.parseBoolean(args[1]);
		
		//creates a new instance of stemtime to test and tests it
		STEMTime startTime = ModelFactory.eINSTANCE.createSTEMTime();
		STEMTime newTime = startTime.addIncrement(TEST_INCREMENT);
		boolean TestIncrement = newTime.getTime().getTime() - startTime.getTime().getTime() == TEST_INCREMENT;

		//sends results off to the report
		TestReporter tr = new TestReporter();
		tr.validate(TestIncrement);
		tr.reportTest(new ArrayList<String>(Arrays.asList(Boolean.toString(TestIncrement))));
	}

} // STEMTimeTest
