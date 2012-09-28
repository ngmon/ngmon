#!/bin/sh
mvn exec:java &
echo $! > heimdall.pid
