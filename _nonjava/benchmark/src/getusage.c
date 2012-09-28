#include <stdlib.h> 
#include <sys/types.h>
#include <stdio.h>
#include <strings.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>   
#include <time.h>


struct pstat{
    long unsigned int utime_ticks;
    long int cutime_ticks;
    long unsigned int stime_ticks;
    long int cstime_ticks;
    long unsigned int vsize; // virtual memory size in bytes
    long unsigned int rss; //Resident  Set  Size in bytes

    long unsigned int cpu_total_time;
};

int get_usage(const pid_t pid, struct pstat* result);
void calc_cpu_usage(const struct pstat* cur_usage, const struct pstat* last_usage, double* ucpu_usage, double* scpu_usage);
int my_sleep(int nanos);

int main(int argc, char **argv)
{	
	pid_t pid;
	int seconds;
	struct pstat *last_use = malloc(sizeof(struct pstat));	
	struct pstat *current_use = malloc(sizeof(struct pstat));	
	
	double *ucpu;
	ucpu = malloc(sizeof(double));
	double *scpu;
	scpu = malloc(sizeof(double));
	
	if ((argc < 3) || (argc > 3))    	/* Test for correct number of arguments */
    {
       fprintf(stderr, "Usage: %s <pid> <seconds>\n", argv[0]);
       exit(1);
    }
    
    pid = atoi(argv[1]);
	seconds = atoi(argv[2]);
	
	get_usage(pid, last_use);
	
	my_sleep(seconds);
	
	get_usage(pid, current_use);
	
	calc_cpu_usage(current_use, last_use, ucpu, scpu);
	
	printf("%g\n", *ucpu + *scpu);

	free(last_use);
	free(current_use);
	free(ucpu);
	free(scpu);

	return 0;
}

/*
 * read /proc data into the passed struct pstat
 * returns 0 on success, -1 on error
*/
int get_usage(const pid_t pid, struct pstat* result){
    //convert  pid to string
    char pid_s[20];
    snprintf(pid_s, sizeof(pid_s), "%d", pid);
    char stat_filepath[30] = "/proc/"; 
    strncat(stat_filepath, pid_s, sizeof(stat_filepath) - strlen(stat_filepath) -1);
    strncat(stat_filepath, "/stat", sizeof(stat_filepath) - strlen(stat_filepath) -1);

    //Open /proc/stat and /proc/$pid/stat fds successive(dont want that cpu
    //ticks increases too much during measurements)
    //TODO: open /proc dir, to lock all files and read the results from the
    //same timefragem
    FILE *fpstat = fopen(stat_filepath, "r");
    if(fpstat == NULL){
        perror("FOPEN ERROR ");
        return -1;
    }

    FILE *fstat = fopen("/proc/stat", "r");
    if(fstat == NULL){
        perror("FOPEN ERROR ");
        fclose(fstat);
        return -1;
    }

    //read values from /proc/pid/stat
    bzero(result, sizeof(struct pstat));
    long int rss;
    if(fscanf(fpstat, "%*d %*s %*c %*d %*d %*d %*d %*d %*u %*u %*u %*u %*u %lu"
                "%lu %ld %ld %*d %*d %*d %*d %*u %lu %ld",
                &result->utime_ticks, &result->stime_ticks,
                &result->cutime_ticks, &result->cstime_ticks, &result->vsize,
                &rss) == EOF) {
        fclose(fpstat);
        return -1;
    }
    fclose(fpstat);
    result->rss = rss * getpagesize();

    //read+calc cpu total time from /proc/stat
    long unsigned int cpu_time[10];
    bzero(cpu_time, sizeof(cpu_time));
    if(fscanf(fstat, "%*s %lu %lu %lu %lu %lu %lu %lu %lu %lu %lu",
                &cpu_time[0], &cpu_time[1], &cpu_time[2], &cpu_time[3],
                &cpu_time[4], &cpu_time[5], &cpu_time[6], &cpu_time[7],
                &cpu_time[8], &cpu_time[9]) == EOF) {
        fclose(fstat);
        return -1;
    }

    fclose(fstat);

    for(int i=0; i < 10;i++){
        result->cpu_total_time += cpu_time[i];
    }

    return 0;
}

/*
* calculates the actual CPU usage(cur_usage - last_usage) in percent
* cur_usage, last_usage: both last measured get_usage() results
* ucpu_usage, scpu_usage: result parameters: user and sys cpu usage in %
*/
void calc_cpu_usage(const struct pstat* cur_usage, const struct pstat*
                    last_usage, double* ucpu_usage, double* scpu_usage){
    const long unsigned int total_time_diff = cur_usage->cpu_total_time -
                                              last_usage->cpu_total_time;

    *ucpu_usage = 100 * (((cur_usage->utime_ticks + cur_usage->cutime_ticks)
                    - (last_usage->utime_ticks + last_usage->cutime_ticks))
                    / (double) total_time_diff);

    *scpu_usage = 100 * ((((cur_usage->stime_ticks + cur_usage->cstime_ticks)
                    - (last_usage->stime_ticks + last_usage->cstime_ticks))) /
                    (double) total_time_diff);
}

int my_sleep(int seconds) {
	struct timespec t_req, t_rem;
	
	t_req.tv_sec = seconds;
	t_req.tv_nsec = 0;
	
	 if(nanosleep(&t_req, &t_rem) < 0 ) {
		 printf("Nano sleep system call failed \n");
		 return -1;
	 }
	
	return 0;
}
