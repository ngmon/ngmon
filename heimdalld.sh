#!/bin/sh
java -agentlib:hprof=cpu=samples -classpath "./out/production/heimdall:./lib/db-5.3.15.jar:./lib/jackson-core-asl-1.9.5.jar:./lib/jackson-mapper-asl-1.9.5.jar:./lib/jackson-smile-1.9.5.jar:./lib/netty-3.2.7.Final.jar:./lib/picocontainer-2.14.1.jar" cz.muni.fi.xtovarn.heimdall.Server &
echo $! > heimdalld.pid
