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
	private boolean passed;
	
	public TestReporter() {
	}
	
	public void validate(boolean passed) {
		this.passed = passed;
	}
	
	public void reportTest(ArrayList<String> computedResult) {
		if (passed) { System.out.println("Passed"); }
		else { System.out.println("Failed"); }
		System.out.println(computedResult);
	}
	
}
