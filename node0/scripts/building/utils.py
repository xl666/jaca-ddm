"""
Contains varios utils to add external artifacts and learning strategies to the build 

Requires:
ssh
sshpass
scp
ant
"""

import zipfile
import os
import shutil
import xml.etree.ElementTree as ET


TRASH_PATH = '/tmp/trash'
JAR_NAME = 'experimenter.jar' #the name of the generated jar
BACKUP_PATH = '/tmp/jacaddmbackup' # if at any moment something goes wrong don't save anything

def unzipPackage(filePath, unzipPath):
    zip_ref = zipfile.ZipFile(filePath, 'r')
    zip_ref.extractall(unzipPath)
    zip_ref.close()

def copyFile(filePath, destPath, overwrite=False):
    if not os.path.exists(destPath): #note that destPath does't includes the file name
        os.mkdir(destPath)
    fileName = filePath.split('/')[-1]
    if os.path.exists(destPath+'/'+fileName) and not overwrite:
        return False
    shutil.copyfile(filePath, destPath+'/'+fileName)
    return True



def recursivelyListFiles(basePath, currentPath=''):
    res = []
    for ele in os.listdir(basePath+'/'+currentPath):
        if os.path.isfile(basePath+'/'+currentPath+'/'+ele):
            if currentPath == '':
                res.append(ele)
            else:
                res.append(currentPath+'/'+ele)
        elif os.path.isdir(basePath+'/'+currentPath+'/'+ele):
            if currentPath == '':
                res += recursivelyListFiles(basePath, ele)
            else:
                res += recursivelyListFiles(basePath, currentPath+'/'+ele)
    return res

#creates all the needed sub dirs in a path
def completeMkdir(path):
    actual = ''
    for ele in path.split('/'):
        actual += (ele + '/')
        if not os.path.exists(actual):
            os.mkdir(actual)


#checks wich files already exists
def alreadyExist(path1, path2):
    artifactsCount = 0
    res = []
    for ele in recursivelyListFiles(path1):
        if ele.find('/') == -1 and ele.split('.')[-1] == 'java': #is an artifact
            artifactsCount += 1
            if os.path.exists(path2+'/src/java/artifacts/'+ele):
                res.append(ele)
        elif ele.startswith('classifiers'): #is a new classifier
            if os.path.exists(path2+'/src/java/'+ele):
                res.append(ele)
        elif os.path.exists(path2+'/'+ele):
            res.append(ele)
    return res
            
    
#if no overwrite and there are files, then a list with the repeted files is returned, else
# a list with the added libs and other files is returned
def addArtifactToBuild(zipPath, basePath, overwrite=False):
    artifactsCount = 0
    existentes = []
    libs = []
    extra = set([])
    if os.path.exists(TRASH_PATH):
        shutil.rmtree(TRASH_PATH)
    os.mkdir(TRASH_PATH)
    unzipPackage(zipPath, TRASH_PATH)
    if not overwrite:
        repetidos =  alreadyExist(TRASH_PATH, basePath)
        if repetidos != []:
            return False, repetidos, set([]) 
    for ele in recursivelyListFiles(TRASH_PATH):
        pathEle = ele[:ele.rfind('/')] 
        if ele.startswith('artifacts'): #is an artifact
            copyFile(TRASH_PATH+'/'+ele, basePath+'/src/java/artifacts', overwrite)
        elif ele.startswith('classifiers'): #is a new classifier
            completeMkdir(basePath+'/src/java/'+pathEle)
            copyFile(TRASH_PATH+'/'+ele, basePath+'/src/java/'+pathEle, overwrite)
        else:
            completeMkdir(basePath+'/'+pathEle)
            copyFile(TRASH_PATH+'/'+ele, basePath+'/'+pathEle, overwrite)
        if ele.startswith('lib/'):
            libs.append(ele)
        elif ele.find('/') != -1 and not ele.startswith('classifiers'):
            extra.add(ele.split('/')[0])
    return True, libs, extra
            
    
def modifyBuild(buildXMLPath, libs, extras):
    tree = ET.parse(buildXMLPath)
    partes = [e for e in tree.getroot() if e.attrib == {'name': 'jar', 'depends' : 'compile'}]
    if len(partes) == 0: #this is not a proper build.xml
        return False
    ar = open(buildXMLPath)
    content = ar.read()
    ar.close()
    ele = partes[0]
    jar = [e for e in ele if e.tag == 'jar'][0]
    for l in libs:
        if '${basedir}/' + l not in content and l.endswith('jar'): #don't add the same thing twice
            jar.append(ET.Element('zipfileset', {'src' : '${basedir}/' + l}))
    ex = [e for e in jar if e.tag == 'fileset'][0]
    for ee in extras:
        if ee + '/**' not in content:
            ex.append(ET.Element('include', {'name' : ee + '/**'}))
    tree.write(buildXMLPath)
    return True
    
def createJar(buildXMLPath):
    res = os.system('ant jar  -buildfile ' + buildXMLPath)
    if res != 0:
        return False
    return True

#note that for this function to work a proper installation and configuration of scp, sshpass and ssh is needed
def sendJarToNodes(jarPath, nodesInfo):
    errors = []
    for ip, path, defaultUser, defaultPass in nodesInfo:
        result = os.system('sshpass -p%s scp %s %s@%s:%s' % (defaultPass, jarPath, defaultUser, ip, path))
        if result != 0: #an error ocurred
            errors.append((ip,path))
    if errors != []:
        return False, errors
    return True, []

#replaces one directory tree with other
def replaceDirs(src, dest):
    shutil.rmtree(dest)
    shutil.copytree(src, dest)


#this functions does all the process, the jar must be generated on basePath
#the backup mechanism could be dangerous, if a process is using something from basePath
#things could go wrong. Only use this function from an external system and only if an experiment is not in course
def addArtifact(zipPath, basePath, nodesInfo, overwrite=False):
    if os.path.exists(BACKUP_PATH):
        shutil.rmtree(BACKUP_PATH)
    completeMkdir(BACKUP_PATH)
    shutil.copytree(basePath, BACKUP_PATH+'/experimenter') #backup everithing before starting
    res, libs, extra = addArtifactToBuild(zipPath, basePath, overwrite)
    if res == False:
        replaceDirs(BACKUP_PATH+'/experimenter', basePath)
        return False, 'Existing files', libs #libs has the repeated files
    modified = modifyBuild(basePath + '/scripts/building/build.xml', libs, extra)
    if modified == False:
        replaceDirs(BACKUP_PATH+'/experimenter', basePath)
        return False, 'Invalid build.xml', []
    created = createJar(basePath + '/scripts/building/build.xml')
    if created == False:
        replaceDirs(BACKUP_PATH+'/experimenter', basePath)
        return False, 'Compilation errors' , []
    enviados, errores = sendJarToNodes(basePath + '/' + JAR_NAME, nodesInfo)
    if enviados == False:
        return False, 'Transmission errors', errores
    return True, '', []
