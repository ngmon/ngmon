#!/bin/bash

for i in 1 2 3 4 5 6 7 8 9 10; do

sleep=$(($i*800000))

./measure.sh $i &
pspid=$(($!))

./sock 1000 $sleep

sleep 2

kill $pspid
done
