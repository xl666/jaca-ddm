#!/bin/sh

help()
{
  cat <<HELP
NAME
     launcherClient — JaCa-KDD client launcher

SYNOPSIS
     launcherClient -a address -p port [-M maxHeap] [-m minHeap] [-y youngSize] [-g gcType] [-s clientSource] -- start
     launcherClient -a address -- stop
     launcherClient [-h]
OPTIONS
     -a 
             Destination where the client will be launched. Accepts both ip addresses and domain names.
     -p 
             Port in which the client will be listening.
     -M 
             Maximum Java heap size (e.g. 2G, 2048M).
             Default value: 1/4 of the physical memory.
     -m 
             Start Java heap size (e.g. 2G, 2048M).
             Default value: 1/64 of the physical memory.
     -y 
             Size of the Java young generation (e.g. 1G, 1024M).
             Default value: 1/3 of the heap size.
     -g 
             Java Garbage Collector (e.g. ParallelGC, ParallelOldGC, SerialGC, ConcMarkSweepGC).
             More information: http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html
     -s 
             Source path of the client.
    -u      Clienet user
    -c      Client password
HELP
  exit 0
}

CLIENT_PATH="/home/fgrimaldo/src/defaultClient"
HEAPMAX=""
HEAPSTART=""
YOUNG_SIZE=""
GC_TYPE=""
CLIENT_USER=""
CLIENT_PASS=""

while [ -n "$1" ]; do
case $1 in
    -h) help;shift 1;; # llamamos a la función ayuda
    -a) IP=$2;shift 2;; 
    -p) PORT=$2;shift 2;; 
    -M) HEAPMAX=-Xmx$2;shift 2;; 
    -m) HEAPSTART=-Xms$2;shift 2;; 
    -y) YOUNG_SIZE=-XX:NewSize=$2;shift 2;; 
    -g) GC_TYPE=-XX:+Use$2;shift 2;; 
    -s) CLIENT_PATH=$2;shift 2;;
    -u) CLIENT_USER=$2;shift 2;;
    -c) CLIENT_PASS=$2;shift 2;;
    --) shift;break;; # end of options
    *)  break;;
esac
done

if [ $1 = "start" ]; then 
    sshpass -p$CLIENT_PASS ssh $CLIENT_USER@$IP <<EOF
    cd $CLIENT_PATH
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:lib
    nohup java $HEAPMAX $HEAPSTART $GC_TYPE $YOUNG_SIZE -classpath bin:lib/experimenter.jar defaultClient.Main $IP:$PORT >> /tmp/defaultClient-$IP-$PORT.out 2>> /tmp/defaultClient-$IP-$PORT.err < /dev/null &
EOF
elif [ $1 = "stop" ]; then
    sshpass -p$CLIENT_PASS ssh $CLIENT_USER@$IP "pkill -9 -f defaultClient.Main"
else
    help
fi
