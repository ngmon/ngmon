#!/bin/bash

for i in 1 2 3 4 5; do

sleep=$(($i*8000))

./measure.sh $i &
pspid=$(($!))

./sock 1000 $sleep

kill $pspid
done
