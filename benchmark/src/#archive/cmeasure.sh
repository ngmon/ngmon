#!/bin/bash
PID=$(cat ../heimdalld.pid)
while(true); do
	ps -o etime,pcpu -p $PID | grep -v ELAPSED
	./nanosleep 5000000
done
