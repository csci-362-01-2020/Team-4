// Credit to IBM Corporation and Bundesinstitut für Risikobewertung for test method
package org.eclipse.stem.test.driver.referencescenariodatamap;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.eclipse.stem.test.driver.TestReporter;

public class PartsPerMillionTestDriver {
	private static double PPM = 1.0/1000000.0;
	public static void main(String args[]) {
		double d1 = Double.parseDouble(args[0]);
		double d2 = Double.parseDouble(args[1]);
		String oracle = args[2];
		boolean oracleComparable = Boolean.parseBoolean(oracle);
		TestReporter tr = new TestReporter();
		
		boolean retVal = true;
		if(d1 == 0.0) retVal = (Math.abs(d2) < PPM);
		if(d2 == 0.0) retVal = (Math.abs(d1) < PPM);
		double diff = (Math.abs(2.0*(d1-d2)/(d1+d2)));
		if(diff > PPM) retVal = false;
		
		if (retVal == oracleComparable) {
			tr.validate(true);
		} else {
			tr.validate(false);
		}
		tr.reportTest(new ArrayList<String>(Arrays.asList(Boolean.toString(retVal))));
	}

}
