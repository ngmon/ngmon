#define _GNU_SOURCE
#include <sys/time.h>    
#include <time.h>      
#include <stdio.h>      /* for printf() and fprintf() */
#include <sys/socket.h> /* for socket(), connect(), send(), and recv() */
#include <arpa/inet.h>  /* for sockaddr_in and inet_addr() */
#include <stdlib.h>     /* for atoi() and exit() */
#include <string.h>     /* for memset() */
#include <unistd.h>     /* for close() */

void error(char *errorMessage);
char* event_string();
int my_sleep(int nanos);

int main(int argc, char *argv[])
{
    int sock;                        	/* Socket descriptor */
    struct sockaddr_in echoServAddr; 	/* Echo server address */
    unsigned short echoServPort = 5000; /* Echo server port */
    char *servIP = "127.0.0.1";      	/* Server IP address (dotted quad) */
    char *echoString;                	/* String to send */
    unsigned int echoStringLen;      	/* Length of string */
    int loops;
    int sleep_time;

    if ((argc < 3) || (argc > 3))    	/* Test for correct number of arguments */
    {
       fprintf(stderr, "Usage: %s <loops> <sleep_time>\n",
               argv[0]);
       exit(1);
    }
	
	loops = atoi(argv[1]);
	sleep_time = atoi(argv[2]);	

    /* Create a reliable, stream socket using TCP */
    if ((sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) error("socket() failed");

    /* Construct the server address structure */
    memset(&echoServAddr, 0, sizeof(echoServAddr));     /* Zero out structure */
    echoServAddr.sin_family      = AF_INET;             /* Internet address family */
    echoServAddr.sin_addr.s_addr = inet_addr(servIP);   /* Server IP address */
    echoServAddr.sin_port        = htons(echoServPort); /* Server port */

    /* Establish the connection to the echo server */
    if (connect(sock, (struct sockaddr *) &echoServAddr, sizeof(echoServAddr)) < 0) error("connect() failed");


	int avg = sleep_time;		
	int i;
	
	for (i = 0; i < loops; i++) {
		
		echoString = event_string();
		echoStringLen = strlen(echoString);          /* Determine input length */
		
		if (send(sock, echoString, echoStringLen, 0) != echoStringLen) error("send() sent a different number of bytes than expected"); /* Send the string to the server */
		
		avg += my_sleep(sleep_time);
		avg = avg/2;
	}
	
    printf("%d:%d",avg, i);   
    printf("\n");    /* Print a final linefeed */

    close(sock);
    exit(0);
}

void error(char *errorMessage) /* Error handling function */
{
    perror(errorMessage);
    exit(1);
}

char* iso_time() {
	struct timeval tv;
	time_t now_t;
	int millis;
	struct tm now;
	char *timestring;

	gettimeofday(&tv, NULL); 
	now_t=tv.tv_sec;
	millis = tv.tv_usec;
	now = *localtime(&now_t);
	
	asprintf(&timestring, "%4d-%02d-%02dT%02d:%02d:%02d.%03d+0200", now.tm_year+1900, now.tm_mon+1, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec, millis);
	
	return timestring;
}

char* event_string() {
	char *event_json;   
	
	asprintf(&event_json,"{\"Event\":{\"occurrenceTime\":\"%s\",\"hostname\":\"domain.localhost.cz\",\"type\":\"org.linux.cron.Started\",\"application\":\"Cron\",\"process\":\"proc_cron NAme\",\"processId\":\"id005\",\"severity\":5,\"priority\":4,\"Payload\":{\"schema\":null,\"schemaVersion\":null,\"value\":4648,\"value2\":\"aax4x46aeEF\"}}}\n", iso_time());
	
	return event_json;
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
