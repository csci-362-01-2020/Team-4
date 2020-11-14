// Credit to IBM Corporation and Bundesinstitut für Risikobewertung for test method
package org.eclipse.stem.test.driver.referencescenariodatamap;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.eclipse.stem.test.driver.TestReporter;

public class PartsPerMillionTestDriver {

	public static void main(String args[]) {
		double d1 = Double.parseDouble(args[0]);
		double d2 = Double.parseDouble(args[1]);
		String testCase = args[3];
		String oracle = args[2];
		TestReporter reporter = new TestReporter("NelderMeadTestDriver", "execute()", testCase, oracle);
		boolean retVal = true;
		if(d1 == 0.0) return (Math.abs(d2) < PPM);
		if(d2 == 0.0) return (Math.abs(d1) < PPM);
		double diff = (Math.abs(2.0*(d1-d2)/(d1+d2)));
		if(diff > PPM) retVal = false;
		return retVal;
	}

}
