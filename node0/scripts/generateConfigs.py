
"""
Uses a directory with template files to generate configuration files to be consumed by the experiment script
"""
import os


templateDirectory = "/home/xl666/ownCloud/investigacion/windowing/templates/"
outDirectory = "/home/xl666/ownCloud/investigacion/windowing/confs"

bdNames = ['adult.arff', 'australian.arff', 'breast.arff', 'credit-g.arff', 'diabetes.arff', 'ecoli.arff', 'german.arff', 'hypothyroid.arff', 'kr-vs-kp.arff', 'letter.arff', 'mushroom.arff', 'segment.arff', 'sick.arff', 'splice.arff', 'waveform-5000.arff', 'poker-lsn.arff']

#bdNames = ["poker-lsn.arff", "covtypeNorm.arff", "airlines.arff", "KDDCup99_full.arff"]

num = 65 #A, naming conventions


for template in sorted(os.listdir(templateDirectory)): #the templates with data creation must to have an alphabetical preference
    if(not template.endswith('xml') and not template.endswith('XML')):
        continue
    ar = open('%s/%s' % (templateDirectory, template))
    content = ar.read()
    ar.close()
    currentFileName = chr(num)
    for n in bdNames:
        con = content  % (n, n) #the template files always have 2 wildcards
        ar = open('%s/%s.xml' % (outDirectory, currentFileName), 'w')
        ar.write(con)
        ar.close()
        currentFileName += chr(num)
    num += 1
