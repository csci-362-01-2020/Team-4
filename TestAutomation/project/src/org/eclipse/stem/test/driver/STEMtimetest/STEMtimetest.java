package org.eclipse.stem.test.driver.STEMtimetest;
import org.eclipse.stem.core.model.ModelFactory;
import org.eclipse.stem.core.model.STEMTime;
import org.eclipse.stem.test.driver.TestReporter;

public class STEMtimetest{

	// One day's worth of milliseconds
	private static final long TEST_INCREMENT;
	protected STEMTime fixture = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		
		System.out.println("hello");
		
	}

	public void testAddIncrement__long() {
		TEST_INCREMENT = args[0];
		final STEMTime startTime = getFixture();
		final STEMTime newTime = startTime.addIncrement(TEST_INCREMENT);
		boolean compare1 = newTime.getTime().compareTo(startTime.getTime()) > 0;
		boolean TestIncrement = newTime.getTime().getTime() - startTime.getTime().getTime() == TEST_INCREMENT;
	} // testAddIncrement__long


	public void testValueEquals__Object() {
		final STEMTime time1 = getFixture();
		final STEMTime time1a = getFixture();
		time1.valueEquals(time1a);
		final STEMTime time2 = time1a.addIncrement(TEST_INCREMENT);
		time1.valueEquals(time2);
		
	}
	
	protected STEMTime getFixture() {
		return fixture;
	}

} // STEMTimeTest
