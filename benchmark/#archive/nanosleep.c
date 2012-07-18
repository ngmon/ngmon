#include <stdio.h>
#include <stdlib.h>  
#include <sys/time.h>   
#include <time.h>

int my_sleep(int nanos);

int main(int argc, char **argv)
{	
	int sleep_time;
	
	if ((argc < 2) || (argc > 2))    	/* Test for correct number of arguments */
    {
       fprintf(stderr, "Usage: %s <sleep_time>\n",
               argv[0]);
       exit(1);
    }
	
	sleep_time = atoi(argv[1]);
	
	my_sleep(sleep_time);
	
	return 0;
}

struct timespec diff(struct timespec start, struct timespec end) {	
	struct timespec temp;
	if ((end.tv_nsec-start.tv_nsec)<0) {
		temp.tv_sec = end.tv_sec-start.tv_sec-1;
		temp.tv_nsec = 1000000000+end.tv_nsec-start.tv_nsec;
	} else {
		temp.tv_sec = end.tv_sec-start.tv_sec;
		temp.tv_nsec = end.tv_nsec-start.tv_nsec;
	}
	return temp;
}

int my_sleep(int nanos) {
	struct timespec t_req, t_rem, start, stop, difference;
	int real_sleep;	
	
	t_req.tv_sec = 0;
	t_req.tv_nsec = nanos;
	
	clock_gettime(CLOCK_REALTIME, &start);
		
	 if(clock_nanosleep(CLOCK_REALTIME, 0, &t_req, &t_rem) < 0 ) {
		 printf("Nano sleep system call failed \n");
	 }
	 
	clock_gettime(CLOCK_REALTIME, &stop);
	
	difference = diff(start, stop);
	
	real_sleep = difference.tv_nsec;
	
	return real_sleep;
}
