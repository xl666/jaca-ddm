import lanzar

def test():
    infoNodos = [('localhost','8080', '512', '512', '', '/home/xl666/Dropbox/doctorado/tesis/sistema/src/defaultClient', 'xl666', 'dragon')]
    infoNodo0 = ('512', '512', '')
    dirConfs = '/tmp/confs'
    basePath = '/home/xl666/Dropbox/doctorado/tesis/sistema/src/experimenter'
    resultPath = '/tmp/results'
    ex = lanzar.ProcesoExp(infoNodos, infoNodo0, dirConfs, basePath, resultPath)
    ex.start()

test()
        


    
