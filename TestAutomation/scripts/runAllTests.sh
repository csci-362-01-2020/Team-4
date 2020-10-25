#!/bin/bash

n=1
while IFS= read -r line; do
	drivers[$n]=$line;
	n=$((n+1));
done < data/driverlist.txt

cd ../project/bin

n=1
while [[ $n -le ${#drivers[@]} ]]
do
	java -cp ".:../dependencies/*" ${drivers[$n]};
	n=$((n+1));
done
