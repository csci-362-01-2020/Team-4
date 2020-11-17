package org.eclipse.stem.test.driver.GetSqrdEdgeRange;
import java.io.*;
import java.util.*;
import java.awt.Polygon;
import org.eclipse.stem.test.driver.TestReporter;
import java.lang.Math.*;
public class GetSqrdEdgeRangeDriver {

	public static void main(String args[]) {
		int[] xpoints;
		int[] ypoints;
		//recieve qx and qy and oracle
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
		//create testReporter instince
		TestReporter tr = new TestReporter();
		//add points to the polygon
		int sides = Integer.parseInt(args[2]);
		for(int i = 0; i < sides; i++){
		polygon.addPoint(xlist[i], ylist[i]);
}
 		
		double range = Double.MAX_VALUE;
		int[] ix = polygon.xpoints;
		int[] iy = polygon.ypoints;
		
		for (int i = 0; i < ix.length; i++) {
			double x = ix[i];
			double y = iy[i];
			double dx = qx - x;
			double dy = qy - y;
			double r = (dx * dx) + (dy * dy);
			if (r <= range)
				range = r;
		}
		double roundrange = Math.round(range*10000.0)/10000.0;
		if (roundrange == oracle) {
			tr.validate(true);
		} else {
			tr.validate(false);
		}
		tr.reportTest(new ArrayList<String>(Arrays.asList(Double.toString(roundrange))));

	}
}
