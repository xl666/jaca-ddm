package classifiers;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class J48CUDATree extends WrapperJ48 {

	private int MAXARCS;
	private Instances metaData;
	int nLeaf = 1; //to count number of leaves
	
	/*
	 * Eval codes:
	 * <= 0
	 * > 1
	 * = 2
	 */
	
	public J48CUDATree(Instances metaData) 
	{		
		super();
		this.metaData = new Instances(metaData, 0); //only copy metadata
		
		int mats = 2;
		for(int i = 0; i < metaData.numAttributes(); i++)
		{
			if(i == metaData.classIndex()) //ignore class
				continue;
			if(metaData.attribute(i).numValues() > mats)
				mats = metaData.attribute(i).numValues();
		}
		MAXARCS = mats; // +1 for none	
		nLeaf = 1; //1 because 0 is reserved for not leaf
	}

	public int labelClassToInt(Instance metadata, String label)
	{
		return metadata.classAttribute().indexOfValue(label);
	}
	
	
	public CUDATree convert() 
	{		
		nLeaf = 1;
		int nodos = this.m_root.numNodes();
		CUDATree res = new CUDATree(nodos, MAXARCS);
		
		res.examplesTrained = this.getExamplesTrained();

	    m_root.assignIDs(-1);	   
	    if (m_root.m_isLeaf) 
	    {	    
	      
	      res.isLeaf[m_root.m_id] = nLeaf;
	      nLeaf++;
	      
	      String cc;
	      try 
	      {
			cc = Utils.backQuoteChars(m_root.m_localModel.dumpLabel(0, m_root.m_train)).split(" ")[0];
			int cv = this.metaData.classAttribute().indexOfValue(cc);
			res.attributes[m_root.m_id] = cv; //as is a leaf, the attribute is actually the class value
			res.numbersOfArcs[m_root.m_id] = 0; //is a leaf						
	      } 
		
	      catch (Exception e) {
			// TODO Auto-generated catch block
	    	  e.printStackTrace();
	      }
	      
	      
	    } 
	   else 
	   {	      		 		   
		   res.isLeaf[m_root.m_id] = 0;		      
		    String cc;
			try 
			{
				cc = Utils.backQuoteChars(m_root.m_localModel.leftSide(m_root.m_train)).split(" ")[0];
				int attIdx = metaData.attribute(cc).index();
				res.attributes[m_root.m_id] = attIdx; 
				res.numbersOfArcs[m_root.m_id] = m_root.m_sons.length;
				
				recorrerArcos(res, m_root, attIdx);
				
			} 
			
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		   
		   
	   }	
	    res.nLeaf = nLeaf; //how many leaves
		return res;
	}
	
	private void recorrerArcos(CUDATree arbol, ClassifierTree actual, int nodeName)throws Exception
	{
		
		for (int i = 0; i < actual.m_sons.length; i++) 
		{
			//info node
			int nNodo = actual.m_id;
			int arc = (nNodo * MAXARCS) + i;
			
			
			String[] eles = Utils.backQuoteChars(actual.m_localModel.rightSide(i, actual.m_train).trim()).split(" ");
			
			String evalT = eles[0];
			int ev = 0;
			
			if(evalT.trim().equals("<="))
				ev = 0;
			else if(evalT.trim().equals(">"))
				ev = 1;
			else if((evalT.trim().equals("=")))
				ev = 2;
			else
				throw new Exception("No avaliable eval type: " + evalT);
						
			arbol.evalTypes[arc] = ev; 
			try
			{
				if(metaData.attribute(nodeName).isNumeric())
					arbol.vals[arc] = Float.parseFloat(eles[1]);
				else
				{
					if(eles[1].startsWith("\\'"))
						eles[1] = quitarBackSlash(eles[1]);
					arbol.vals[arc] = metaData.attribute(nodeName).indexOfValue(eles[1]);
				}
			}
			catch(java.lang.NumberFormatException ex)
			{
				if(eles[1].startsWith("\\'"))
					eles[1] = quitarBackSlash(eles[1]);
				//System.out.println(""+ metaData.attribute(nodeName).name()+ ":" + eles[1]);
				arbol.vals[arc] = metaData.attribute(nodeName).indexOfValue(eles[1]);
			}
			arbol.nodeIndices[arc] = actual.m_sons[i].m_id;
			
			if (actual.m_sons[i].m_isLeaf) 
			{
				arbol.isLeaf[actual.m_sons[i].m_id] = nLeaf;
				nLeaf++;
			      
			    String cc;
				try 
				{
					cc = Utils.backQuoteChars(actual.m_localModel.dumpLabel(i, actual.m_train)).split(" ")[0];
					int cv = this.metaData.classAttribute().indexOfValue(cc);
					arbol.attributes[actual.m_sons[i].m_id] = cv; //as is a leaf, the attribute is actually the class value
					arbol.numbersOfArcs[actual.m_sons[i].m_id] = 0; //is a leaf						
				} 
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		       
		   }
			else
			{
				arbol.isLeaf[actual.m_sons[i].m_id] = 0;		      
			    String cc;
				try 
				{
					cc = Utils.backQuoteChars(actual.m_sons[i].m_localModel.leftSide(actual.m_train)).split(" ")[0];
					int attIdx = metaData.attribute(cc).index();
					arbol.attributes[actual.m_sons[i].m_id] = attIdx; 
					arbol.numbersOfArcs[actual.m_sons[i].m_id] = actual.m_sons[i].m_sons.length;
					
					recorrerArcos(arbol, actual.m_sons[i], attIdx);
					
				} 
				
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
		}					
			
	}
	
	public String quitarBackSlash(String cad)
	{
		String aux = cad.substring(1);
		return aux.substring(0, aux.length()-2) + aux.substring(0, 1); //just put the same begining symbol that the backslash scapes
	}
	

}