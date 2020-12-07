package org.eclipse.stem.test.driver.GetSqrdEdgeRange;
import java.io.*;
import java.util.*;
import java.awt.Polygon;
import org.eclipse.stem.test.driver.TestReporter;
import java.lang.Math.*;
import org.eclipse.stem.graphgenerators.impl.*;
//Tests GetSqrdEdgeRange, which gets the minimal range between two points on a polygon 
public class GetSqrdEdgeRangeDriver {

	public static void main(String args[]) {
		int[] xpoints;
		int[] ypoints;
		//recieve variables qx and qy and recieve the oracle
		Double oracle = Double.parseDouble(args[args.length - 2]);
		double qx = Double.parseDouble(args[0]);
		double qy = Double.parseDouble(args[1]);
		//create a polygon object
		Polygon polygon = new Polygon();
		//create lists for the x and y's of the polygon
		int[] xlist = new int[args.length - 5];
		int[] ylist = new int[args.length - 5];
		int xcounter = 0;
		int ycounter = 0;
		for (int i = 3; i < (args.length - 2); i++){
		if (i % 2 == 0){
		xlist[xcounter] = Integer.parseInt(args[i]);
		xcounter++;
		}
		else {
		ylist[ycounter] = Integer.parseInt(args[i]);
		ycounter++;
		}
		}
		//add points to the polygon
		int sides = Integer.parseInt(args[2]);
		for(int i = 0; i < sides; i++){
		polygon.addPoint(xlist[i], ylist[i]);
		}
		File testfile = new File("test.txt");
		//create an object of the PajekNetGraphGeneratorImplOld class to run the method
		double testdouble = 3.58;
 		PajekNetGraphGeneratorImplOld Pajek = new PajekNetGraphGeneratorImplOld(testfile, testdouble);
		//run the method
		double range = Pajek.getSqrdEdgeRange(qx,qy,polygon);
		//round results
		double roundrange = Math.round(range*10000.0)/10000.0;
		//create testReporter instince
		TestReporter tr = new TestReporter();
		//validate true if oracle and the rounded range are equal and false otherwise
		if (roundrange == oracle) {
			tr.validate(true);
		} else {
			tr.validate(false);
		}
		tr.reportTest(new ArrayList<String>(Arrays.asList(Double.toString(roundrange))));

	}
}
