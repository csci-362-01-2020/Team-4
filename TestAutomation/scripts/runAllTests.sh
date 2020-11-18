#!/bin/bash

# Construct template for html report document
header="<!DOCTYPE html>

<html>

<head>
<style>
body {
  background: cadetblue;
}
table {
  background: white;
    width: 99%;
      border: 2px solid black;
        margin: 10px;
}
th{
  border: 1px solid black;
    padding: 5px;
      text-align: left;
        width: 10%;
}
td {
  border: 1px solid black;
    padding: 5px;
      text-align: left;
}
</style>
</head>


<body>

"

footer="

</body>

</html>"

table="
<table>
  <tr>
    <th colspan=2 style=\"font-size: 23px;\">Test Case number - Passed_Failed</th>
  </tr>
  <tr>
    <th colspan=1>Driver</th>
    <td colspan=1>driver</td>
  </tr>
  <tr>
    <th colspan=1>Component</th>
    <td colspan=1>component</td>
  </tr>
  <tr>
    <th colspan=1>Method</th>
    <td colspan=1>method</td>
  </tr>
  <tr>
    <th colspan=1>Requirement</th>
    <td colspan=1>requirement</td>
  </tr>
  <tr>
    <th colspan=1>Test Input(s)</th>
    <td colspan=1>test_input</td>
  </tr>
  <tr>
    <th colspan=1>Expected Results</th>
    <td colspan=1>expected_result</td>
  </tr>
  <tr>
    <th colspan=1>Computed Results</th>
    <td colspan=1>computed_result</td>
  </tr>
</table>

&nbsp;

"
# Initialize html report document with header
echo $header > reports/report.html

# Compile drivers, supporting classes, and TestReporter.java
cd project/src
find -name "*.java" -print -a -exec javac -cp ".:../dependencies/*" {} \;
cd org/eclipse/stem/test/driver/
# Copy TestReporter.class to testCasesExecutables
find -mindepth 1 -maxdepth 1 -type f \( -exec cp {} "../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/" \; \)
# Copy rest of drivers to testCasesExecutables
find -mindepth 1 -maxdepth 1 -type d \( -exec cp -r {} "../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/" \; \)
# Remove unnecessary files
find -name "*.class" -exec rm {} \;
cd ../../../../../../../testCasesExecutables/org/eclipse/stem/test/driver/
find -name "*.java" -exec rm {} \;
# Back to top-level directory
cd ../../../../../../

# Execute test cases
cd testCases/workingTestCases
for file in *; do
	# Read test case file
	i=0
	while IFS= read -r line; do
		test_case_info[$i]=$line
		i=$i+1
	done < $file
	
	# Instantiate variables for test case execution and report
	test_num=${test_case_info[0]}
	requirement=${test_case_info[1]}
	component=${test_case_info[2]}
	method=${test_case_info[3]}
	driver_ps=${test_case_info[4]}
	input_displayable=${test_case_info[5]}
	oracle_displayable=${test_case_info[6]}
	input_to_jvm=$(echo $input_displayable | tr -d ',')
	oracle_to_jvm=$(echo $oracle_displayable | tr -d ',')
	
	# Run test and get results. Did the test pass or fail? What was the output?
	cd ../../testCasesExecutables
	readarray results < <(java -cp ".:../project/dependencies/*" $driver_ps $input_to_jvm $oracle_to_jvm $test_num)
	
	# Substitute test case information for placeholders in html document
	new_table=${table/number/$test_num}
	new_table=${new_table/Passed_Failed/${results[0]}}
	new_table=${new_table/driver/$driver_ps}
	new_table=${new_table/component/$component}
	new_table=${new_table/method/$method}
	new_table=${new_table/requirement/$requirement}
	new_table=${new_table/test_input/$input_displayable}
	new_table=${new_table/expected_result/$oracle_displayable}
	new_table=${new_table/computed_result/${results[1]}}
	echo $new_table >> ../reports/report.html
	
	# Go back to testCases
	cd ../testCases/workingTestCases
done

# Display in browser
cd ../..
echo $footer >> reports/report.html
sensible-browser reports/report.html
	
	
