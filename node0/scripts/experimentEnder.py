"""
This script is intended to be run at the root diretory of the sistem (../ from here)
"""

import os
import time

initHeap = 4096
maxHeap = 4096
dirConfs = '/home/fgrimaldo/configs' #directory where the config xml files are
clientMaxHeap = 12
clientInitHeap = 12
clientInitYoung = 11
clientGCType = 'ParallelGC'

#-Djava.security.policy=server.policy
genMa2jCommand = 'java    -cp scripts/bin mas2jgenerator.Main %s ./masExp.mas2j' 
execServerCommand = 'java -Xms%sm -Xmx%sm   -classpath lib/jason.jar:bin/classes:lib/weka.jar:lib/cartago.jar:lib/c4jason.jar:lib/jacamo.jar:lib/moa.jar:lib/moise.jar:lib/pentaho.jar:lib/sizeofag-1.0.0.jar jason.infra.centralised.RunCentralisedMAS masExp.mas2j' % (initHeap, maxHeap)

#ips = [('compute-1-0','8080'),('compute-1-1','8080'), ('compute-1-2','8080'), ('compute-1-3','8080'), ('compute-1-4','8080'), ('compute-1-5','8080'), ('compute-1-6','8080'), ('compute-1-7','8080')]
#ips = [('compute-1-0','8080'), ('compute-1-0','8081'), ('compute-1-0','8082'), ('compute-1-0','8083'), ('compute-1-1','8080'), ('compute-1-1','8081'), ('compute-1-1','8082'), ('compute-1-1','8083'), ('compute-1-2','8080'), ('compute-1-2','8081'), ('compute-1-2','8082'), ('compute-1-2','8083'), ('compute-1-3','8080'), ('compute-1-3','8081'), ('compute-1-3','8082'), ('compute-1-3','8083'), ('compute-1-4','8080'), ('compute-1-4','8081'), ('compute-1-4','8082'), ('compute-1-4','8083'), ('compute-1-5','8080'), ('compute-1-5','8081'), ('compute-1-5','8082'), ('compute-1-5','8083'), ('compute-1-6','8080'), ('compute-1-6','8081'), ('compute-1-6','8082'), ('compute-1-6','8083'), ('compute-1-7','8080'), ('compute-1-7','8081'), ('compute-1-7','8082'), ('compute-1-7','8083')]

ips = [('compute-1-0','8080'), ('compute-1-1','8080'), ('compute-1-2','8080'), ('compute-1-3','8080'),  ('compute-1-4','8080'), ('compute-1-5','8080'), ('compute-1-6','8080'),  ('compute-1-7','8080')]



for xml in sorted(os.listdir(dirConfs)): #create a mas2j per file and executes the experiment
    if(not xml.endswith('xml') and not xml.endswith('XML')):
        continue
    os.system(genMa2jCommand % (dirConfs + '/' + xml))
    #excecute clients
    for ip in ips:
        os.system('/home/fgrimaldo/launchClient/launchClient.sh -a %s -p %s -M %sg -m %sg    start' % (ip[0], ip[1], clientMaxHeap, clientInitHeap))
    time.sleep(5)
    os.system(execServerCommand)
    time.sleep(2)
    #kill clients
    for ip in ips:
                os.system('/home/fgrimaldo/launchClient/launchClient.sh -a %s -p %s -M %sg -m %sg   stop' % (ip[0], ip[1], clientMaxHeap, clientInitHeap))
    time.sleep(5)
