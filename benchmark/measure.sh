#!/bin/bash
PID=$(cat ../heimdalld.pid)
while(true); do
	ps -o etime,pcpu -p $PID | grep -v ELAPSED >> ./out/cpu_[$1]ms.log
	./nanosleep 500000
done
