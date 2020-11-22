// Credit to IBM Corporation and Bundesinstitut für Risikobewertung for test method
package org.eclipse.stem.test.driver.referencescenariodatamap;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.eclipse.stem.test.driver.TestReporter;
import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapTestingFactory;
import org.eclipse.stem.analysis.impl.ReferenceScenarioDataMapImpl;

public class PartsPerMillionTestDriver {

	public static void main(String args[]) {
		double d1 = Double.parseDouble(args[0]);
		double d2 = Double.parseDouble(args[1]);
		String oracle = args[2];
		boolean oracleComparable = Boolean.parseBoolean(oracle);
		
		ReferenceScenarioDataMapTestingFactory rsdmTestingFactory = new ReferenceScenarioDataMapTestingFactory();
		ReferenceScenarioDataMapImpl rsdm = rsdmTestingFactory.makeReferenceScenarioDataMap();
		
		boolean retVal = rsdmTestingFactory.testCloseEnough(rsdm, d1, d2);
		TestReporter tr = new TestReporter();	
		if (retVal == oracleComparable) {
			tr.validate(true);
		} else {
			tr.validate(false);
		}
		tr.reportTest(new ArrayList<String>(Arrays.asList(Boolean.toString(retVal))));
	}

}
