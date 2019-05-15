
#infoNodo = (ip,puerto, maxHeap,initHeap, gctype, pathEx, user, pass,)
#infoNodo0 = (maxHeap, initHeap, gctype)

import os
import time
import subprocess
import multiprocessing

class ProcesoExp(multiprocessing.Process):
    """Documentation for ProcesoExp
    
    """
    def __init__(self, infoNodos, infoNodo0, dirConfs, basePath, resultPath, strategyPath):
        self.infoNodos = infoNodos
        self.infoNodo0 = infoNodo0
        self.dirConfs = dirConfs
        self.basePath = basePath
        self.resultPath = resultPath
        self.strategyPath = strategyPath
        multiprocessing.Process.__init__(self)

    def run(self):
        os.chdir(self.strategyPath)
        runExperiment(self.infoNodos, self.infoNodo0, self.dirConfs, self.basePath, self.resultPath)


def runExperiment(infoNodos, infoNodo0, dirConfs, basePath, resultPath):
    genMa2jCommand = 'java    -cp '+basePath+'/scripts/bin mas2jgenerator.Main %s '+basePath+'/masExp.mas2j ' + resultPath  #corregir schema
    execServerCommand = 'java -Xms'+infoNodo0[1]+'m -Xmx'+infoNodo0[0]+'m   -classpath '+basePath+'/lib/jason.jar:'+basePath+'/bin/classes:'+basePath+'/lib/weka.jar:'+basePath+'/lib/cartago.jar:'+basePath+'/lib/c4jason.jar:'+basePath+'/lib/moa.jar:'+basePath+'/lib/pentaho.jar:'+basePath+'/lib/jcuda.jar jason.infra.centralised.RunCentralisedMAS '+basePath+'/masExp.mas2j'
    for xml in sorted(os.listdir(dirConfs)): #create a mas2j per file and executes the experiment
        if(not xml.endswith('xml') and not xml.endswith('XML')):
            continue
        os.system(genMa2jCommand % (dirConfs + '/' + xml))
    
        for info in infoNodos:
            os.system(basePath+'/scripts/launchClient.sh -a %s -p %s -M %sm -m %sm -s %s -u %s -c %s    start' % (info[0], info[1], info[2], info[3], info[5], info[6], info[7]))
        time.sleep(5)
        os.system(execServerCommand)
        #pid = proc.pid
        time.sleep(2)
    
        for info in infoNodos:
            os.system(basePath+'/scripts/launchClient.sh -a %s -p %s -M %sg -m %sg -s %s -u %s -c %s    stop' % (info[0], info[1], info[2], info[3], info[5], info[6], info[7]))
        time.sleep(5)
        

def killPackage(infoNodos, procesoPID):
    for node in infoNodos:
        os.system(basePath+'/scripts/launchClient/launchClient.sh -a %s  -u %s -c %s    stop' % (info[0], info[6], info[7]))
    os.kill(procesoPID, 9) # 9 for SIGKILL



    
