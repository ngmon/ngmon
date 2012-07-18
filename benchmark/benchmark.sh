for i in {1..50}; do
	./utilization.sh $i
	sleep 1
	cat usage[$i].log
done
