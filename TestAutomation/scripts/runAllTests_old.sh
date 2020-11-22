#!/bin/bash

# Build project
cd scripts
cd ../project/src
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
# Back to scripts directory
cd ../../../../../../scripts

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
echo $header > ../reports/report.html

# Parse test cases for test execution and reporting
cd ../testCases/workingTestCases
readarray test_nums < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==1) print $0}' "$filename"; done)
readarray requirements < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==2) print $0}' "$filename"; done)
readarray components < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==3) print $0}' "$filename"; done)
readarray methods < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==4) print $0}' "$filename"; done)
readarray drivers < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==5) print $0}' "$filename"; done)
readarray inputs < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==6) print $0}' "$filename"; done)
readarray oracles < <(find -name "*.txt" | sort -k 11 | while IFS= read -r filename; do awk '{if(NR==7) print $0}' "$filename"; done)

# Execute test cases and report
cd ../../testCasesExecutables
for ind in ${!drivers[@]}
do	
	# Parse results of find commands to get test case info
	test_num=$(echo ${test_nums[ind]} | awk '{sub(/Id: /,"")} 1' );
	requirement=$(echo ${requirements[ind]} | awk '{sub(/Requirement: /,"")} 1' );
	component=$(echo ${components[ind]} | awk '{sub(/Component: /,"")} 1' );
	method=$(echo ${methods[ind]} | awk '{sub(/Method: /,"")} 1' );
	driver=$(echo ${drivers[ind]} | awk '{sub(/Driver: /,"")} 1' );
	input=$(echo ${inputs[ind]} | awk '{sub(/Inputs: /,"")} 1' | tr -d ',');
	oracle=$(echo ${oracles[ind]} | awk '{sub(/Oracles: /,"")} 1' | tr -d ',');
	input_for_display=$(echo ${inputs[ind]} | awk '{sub(/Inputs: /,"")} 1');
	oracle_for_display=$(echo ${oracles[ind]} | awk '{sub(/Oracles: /,"")} 1');

	# Run test and get results. Did the test pass or fail? What was the output?
	readarray results < <(java -cp ".:../project/dependencies/*" $driver $input $oracle $test_num)
	
	# Substitute test case information for placeholders in html document
	new_table=${table/number/$test_num}
	new_table=${new_table/Passed_Failed/${results[0]}}
	new_table=${new_table/driver/$driver}
	new_table=${new_table/component/$component}
	new_table=${new_table/method/$method}
	new_table=${new_table/requirement/$requirement}
	new_table=${new_table/test_input/$input_for_display}
	new_table=${new_table/expected_result/$oracle_for_display}
	new_table=${new_table/computed_result/${results[1]}}
	echo $new_table >> ../reports/report.html
done

# Append footer to html report document and display
echo $footer >> ../reports/report.html

sensible-browser ../reports/report.html
