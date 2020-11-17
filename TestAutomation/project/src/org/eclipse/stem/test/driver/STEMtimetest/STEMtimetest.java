package org.eclipse.stem.test.driver.STEMtimetest;
import org.eclipse.stem.core.model.ModelFactory;
import org.eclipse.stem.core.model.STEMTime;
import org.eclipse.stem.test.driver.TestReporter;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import org.eclipse.emf.ecore.EObject;


public class STEMtimetest{

	// One day's worth of milliseconds
	protected long TEST_INCREMENT;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		
		int TEST_INCREMENT = Integer.parseInt(args[0]);
		boolean oracle = Boolean.parseBoolean(args[1]);
		
		STEMTime startTime = ModelFactory.eINSTANCE.createSTEMTime();
		STEMTime newTime = startTime.addIncrement(TEST_INCREMENT);
		boolean TestIncrement = newTime.getTime().getTime() - startTime.getTime().getTime() == TEST_INCREMENT;

		
		TestReporter tr = new TestReporter();
		tr.validate(TestIncrement);
		tr.reportTest(new ArrayList<String>(Arrays.asList(Boolean.toString(TestIncrement))));
	}

} // STEMTimeTest
