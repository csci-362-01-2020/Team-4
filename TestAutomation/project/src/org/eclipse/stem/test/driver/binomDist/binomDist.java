package org.eclipse.stem.test.driver.binomDist;
import java.util.Random;
import org.eclipse.stem.core.model.ModelFactory;
import org.eclipse.stem.core.model.STEMTime;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.eclipse.stem.core.math.BinomialDistributionUtil;
import org.eclipse.stem.test.driver.TestReporter;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;


public class binomDist{
	public static void main(String[] args) {
		//parses command line args into variables to be used
		double testP = Double.parseDouble(args[0]);
		int testN = Integer.parseInt(args[1]);
		boolean oracle = Boolean.parseBoolean(args[2]);
		boolean validResult;
		
		//calls the method we are testing and tests the output
		BinomialDistributionUtil binom = new BinomialDistributionUtil(1000);
		int result = binom.fastPickFromBinomialDist(testP, testN);
		validResult = ((-1<result)&&(result<testN+1));
		
		//sends results off to the report
		TestReporter tr = new TestReporter();
		tr.validate(validResult);
		tr.reportTest(new ArrayList<String>(Arrays.asList(Boolean.toString(validResult), Integer.toString(result))));
	}

}
