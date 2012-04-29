#!/bin/bash

for i in 1 2 3 4 5 6 7 8 9 10; do
sleep=(10000000*i)
./measure.sh $i &
pspid=$!

./sock 1000 $sleep

kill $pspid
done
