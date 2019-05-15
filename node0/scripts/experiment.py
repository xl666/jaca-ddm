"""
This script is intended to be run at the root diretory of the sistem (../ from here)
"""

import os
import time

initHeap = 256
maxHeap = 512
dirConfs = '/home/xl666/ownCloud/investigacion/windowing/confs/' #directory where the config xml files are

genMa2jCommand = 'java -cp scripts/bin mas2jgenerator.Main %s ./masExp.mas2j' 
execServerCommand = 'java -Xms%sm -Xmx%sm   -classpath lib/jason.jar:bin/classes:lib/weka.jar:lib/cartago.jar:lib/c4jason.jar:lib/jacamo.jar:lib/moa.jar:lib/moise.jar:lib/pentaho.jar:lib/sizeofag-1.0.0.jar:lib/jcuda.jar jason.infra.centralised.RunCentralisedMAS masExp.mas2j' % (initHeap, maxHeap)

for xml in sorted(os.listdir(dirConfs)): #create a mas2j per file and executes the experiment
    if(not xml.endswith('xml') and not xml.endswith('XML')):
        continue
    os.system(genMa2jCommand % (dirConfs + '/' + xml))
    time.sleep(1)
    os.system(execServerCommand)
    time.sleep(1)
    
