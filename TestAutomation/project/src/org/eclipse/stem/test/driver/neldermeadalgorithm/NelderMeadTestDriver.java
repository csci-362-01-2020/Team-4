package org.eclipse.stem.test.driver.neldermeadalgorithm;

import java.util.Arrays;

import java.lang.reflect.*;

import org.eclipse.stem.analysis.ErrorResult;
import org.eclipse.stem.analysis.AnalysisPackage;
import org.eclipse.stem.analysis.automaticexperiment.SimplexFunction;
import org.eclipse.stem.analysis.automaticexperiment.NelderMeadAlgorithm;

public class NelderMeadTestDriver {

	public static void main(String[] args) {
		try {	
			Class<?> sf = ClassLoader.getSystemClassLoader().loadClass(
				"org.eclipse.stem.test.driver.neldermeadalgorithm.NelderMeadTestDriver$" + args[10]);
			NelderMeadAlgorithm nelder = new NelderMeadAlgorithm();
			nelder.setParameterLimits(Integer.parseInt(args[0]), Double.parseDouble(args[1]),
							 Double.parseDouble(args[2]));
                	nelder.setParameterLimits(Integer.parseInt(args[3]), Double.parseDouble(args[4]),
							 Double.parseDouble(args[5]));
                	//nelder.setParameterLimits(0, 0.0, 9999999.0);
                	//nelder.setParameterLimits(1, 0.0, 9999999.0);
                	double[] initStart = { Double.parseDouble(args[6]), Double.parseDouble(args[7]) };
		        double[] step = { Double.parseDouble(args[8]), Double.parseDouble(args[9]) };
		        //double[] initStart = { 1.8, 1.2 };
		        //double[] step = { 0.5, 0.5 };
		        nelder.execute((SimplexFunction) sf.newInstance(), initStart, step, 0.01, -1);
		        //nelder.execute((SimplexFunction) sfConstructor.newInstance(), initStart, step, 0.01, -1);
		        
		        //System.out.println("Results:");
		        //System.out.println("Minimum Parameters - " +
		        	//Arrays.toString(nelder.getMinimumParametersValues()));
		        //System.out.println("Minimum Function Value - " +
		        	//nelder.getMinimumFunctionValue());
		        System.out.println(Math.round(nelder.getMinimumParametersValues()[0]));
		        System.out.println(Math.round(nelder.getMinimumParametersValues()[1]));
		        System.out.println(Math.round(nelder.getMinimumFunctionValue()));
			//boolean minParmVal_X = Math.round(nelder.getMinimumParametersValues()[0]) == 2;
			//boolean minParmVal_Y = Math.round(nelder.getMinimumParametersValues()[1]) == 1;
			//boolean minFuncVal = Math.round(nelder.getMinimumFunctionValue()) == -4;
		
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
