#!/bin/sh
java -cp "core-0.1-SNAPSHOT.jar:lib/*" cz.muni.fi.xtovarn.heimdall.Application &
echo $! > heimdall.pid
