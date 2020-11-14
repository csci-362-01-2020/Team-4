#!/bin/bash

Vars=('foo' 'bar' 'baz')

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
    <th colspan=2 style=\"font-size: 23px;\">Test Case foobar - Passed/Failed</th>
  </tr>
  <tr>
    <th colspan=1>Driver</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Component</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Method</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Requirement</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Test Input(s)</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Expected Results</th>
    <td colspan=1>asdf</td>
  </tr>
  <tr>
    <th colspan=1>Computed Results</th>
    <td colspan=1>asdf</td>
  </tr>
</table>

&nbsp;

"

echo $header > temp.html

for i in ${Vars[@]}; do
  echo ${table/foobar/${i}} >> temp.html
done

echo $footer >> temp.html

sensible-browser temp.html
