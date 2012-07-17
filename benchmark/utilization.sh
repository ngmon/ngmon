#!/bin/bash
if ([ $# -ne 1 ])
	then
		echo "Usage: utilization.sh <producers>"
		exit 0
fi

cd ..
rm ./database/events/* # truncate db
./heimdalld.sh
PID=$(cat ./heimdall.pid)
cd benchmark

sleep 5

./sock 100 10 # warm up

for i in $(eval echo "{1..$1}"); do
	./sock 1000 1000 &
	killpids[i]=$!
done

sleep 5

./getusage $PID 35 > usage[$1].log

for p in "${killpids[@]}"; do
	kill -SIGTERM $p
done

sleep 1

kill -SIGTERM $PID

sleep 3
cat usage[$1].log
