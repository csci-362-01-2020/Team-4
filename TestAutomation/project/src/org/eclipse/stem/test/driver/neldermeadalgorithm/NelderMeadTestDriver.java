package org.eclipse.stem.test.driver.neldermeadalgorithm;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.*;

import org.eclipse.stem.test.driver.TestReporter;
import org.eclipse.stem.analysis.ErrorResult;
import org.eclipse.stem.analysis.AnalysisPackage;
import org.eclipse.stem.analysis.automaticexperiment.SimplexFunction;
import org.eclipse.stem.analysis.automaticexperiment.NelderMeadAlgorithm;

public class NelderMeadTestDriver {

	public static void main(String[] args) {
		try {	
			// Instantiate test reporter with driver, test number, and oracles
			String testCase = args[14];
			ArrayList<String> oracle = new ArrayList<>(Arrays.asList(args[11], args[12], args[13]));
			TestReporter tr = new TestReporter(NelderMeadTestDriver.class, testCase, oracle);
			
			Class<?> sf = ClassLoader.getSystemClassLoader().loadClass(
				"org.eclipse.stem.test.driver.neldermeadalgorithm.NelderMeadTestDriver$" + args[10]);
				
			NelderMeadAlgorithm nelder = new NelderMeadAlgorithm();
			nelder.setParameterLimits(Integer.parseInt(args[0]), Double.parseDouble(args[1]),
							 Double.parseDouble(args[2]));
                	nelder.setParameterLimits(Integer.parseInt(args[3]), Double.parseDouble(args[4]),
							 Double.parseDouble(args[5]));
                	double[] initStart = { Double.parseDouble(args[6]), Double.parseDouble(args[7]) };
		        double[] step = { Double.parseDouble(args[8]), Double.parseDouble(args[9]) };	        
		        nelder.execute((SimplexFunction) sf.newInstance(), initStart, step, 0.01, -1);
		        
		        long minParmVal_X = Math.round(nelder.getMinimumParametersValues()[0]);
		        long minParmVal_Y = Math.round(nelder.getMinimumParametersValues()[1]);
			long minFuncVal = Math.round(nelder.getMinimumFunctionValue());
		        
		        // Report test
			boolean minParmVal_X_passed = minParmVal_X == Integer.parseInt(args[11]);
			boolean minParmVal_Y_passed = minParmVal_Y == Integer.parseInt(args[12]);
			boolean minFuncVal_passed = minFuncVal == Integer.parseInt(args[13]);
			tr.validate(minParmVal_X_passed && minParmVal_Y_passed && minFuncVal_passed);
			tr.reportTest(new ArrayList<String>(Arrays.asList(
				Long.toString(minParmVal_X),
				Long.toString(minParmVal_Y),
				Long.toString(minFuncVal))));
			
		
		} catch(ClassNotFoundException | 
				InstantiationException |
					 IllegalAccessException e) {
					 
			e.printStackTrace();
			
		}
					 
		

	}
	
	static class SampleFunction implements SimplexFunction {

	        public ErrorResult getValue(double[] parameters) {
        	         //f(x,y) = -4x + x^2 - y - xy + y^2
                	 //Local minimum for this function is -7 at x=3 and y=2
                 	 double x = parameters[0];
               		 double y = parameters[1];
                 	 double result = -4 * x;
                 	 result += Math.pow(x, 2);
                 	 result -= y;
                 	 result -= x * y;
                 	 result += Math.pow(y, 2);
                 
		 	 ErrorResult res = 
		 	 	AnalysisPackage.eINSTANCE.getAnalysisFactory().createErrorResult();
                 	 res.setError(result);
                 	 return res;
       		 }
	}
	
	static class SampleFunction2 implements SimplexFunction {

                public ErrorResult getValue(double[] parameters) {
                        //f(x,y) = -3x + x^2 - y - xy + y^2
                        //Local minimum for this function is -4 at x=2 and y=1
                        double x = parameters[0];
                        double y = parameters[1];
                        double result = -3 * x;
                        result += Math.pow(x, 2);
                        result -= y;
                        result -= x * y;
                        result += Math.pow(y, 2);

                        ErrorResult res = 
                        	AnalysisPackage.eINSTANCE.getAnalysisFactory().createErrorResult();
                        res.setError(result);
                        return res;
                }
	}
}
