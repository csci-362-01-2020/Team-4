package org.eclipse.stem.test.driver;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


public class TestReporter {
	private Class<?> driver;
	private String testNumber;
	private ArrayList<String> expectedResult;
	private boolean passed;
	
	public TestReporter(Class<?> c, String testNumber, ArrayList<String> oracle) {
		this.driver = c;
		this.testNumber = testNumber;
		this.expectedResult = oracle;
	}
	
	public void validate(boolean passed) {
		this.passed = passed;
	}
	
	public void reportTest(ArrayList<String> computedResult) {
		try {
			Path reportPath = Paths.get("../temp/");
			String[] driverPackageStruct = driver.getName().split("\\.");
			String driverName = driverPackageStruct[driverPackageStruct.length-1];
			StringBuilder sb = new StringBuilder();
			sb.append(reportPath.toString());
			sb.append("/");
			sb.append(driverName);
			sb.append("_");
			sb.append("testnumber" + this.testNumber);
			sb.append("_");
			sb.append(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
			sb.append(".txt");
			
			File report = new File(sb.toString());
			report.createNewFile();
			FileOutputStream fouts = new FileOutputStream(report);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fouts));
			if (this.passed == true) {
				bw.write("Test number " + testNumber + ": Passed");
			} else {
				bw.write("Test number " + testNumber + ": Failed");
			}
			bw.newLine();
			bw.write("----------------------------------------");
			bw.newLine();
			bw.write("Expected: " + expectedResult.toString());
			bw.newLine();
			bw.write("Computed: " + expectedResult.toString());
			bw.newLine();
			bw.write("----------------------------------------");
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
