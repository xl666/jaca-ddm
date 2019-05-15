/*******************************************************************************
 *  ----------------------------------------------------------------------------------------------------------
 *  Copyright (C) 2015 Xavier Limón, Alejandro Guerra-Hernández, Nicandro Cruz-Ramírez, Francisco Grimaldo-Moreno
 * Departmento de Inteligencia Artificial. Universidad Veracruzana. 
 * Departament d’Informàtica. Universitat de València
 *
 *  This file is part of JaCa-DDM.
 *
 *     JaCa-DDM is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     JaCa-DDM is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JaCa-DDM.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  To contact the authors:
 *    xavier120@hotmail.com, aguerra@uv.mx, francisco.grimaldo@uv.es
 *    http://www.uv.mx/aguerra/
 *   http://www.uv.es/grimo/
 * -----------------------------------------------------------------------------------------------------------
 *******************************************************************************/
package utils;


import java.io.BufferedReader;
import java.io.FileReader;

import javax.smartcardio.ATR;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class InstanceManager
{
	//returns part of a Instances, does not create a copy
	public static Instances slice(Instances ins, int begin, int end)
	{
		Instances res = new Instances(ins, 0);
		
		if(end > ins.numInstances())
			end = ins.numInstances();
		
		for(int i = begin; i < end; i++)
		{
			res.add(ins.get(i));
		}
		
		return res;
	}
	
	//adds same instances to existing instances, saves memory
		public static Instances joinInstances(Instances res, Instances a2)
		{		
			for(int i = 0; i < a2.numInstances(); i++)
			{
				res.add(a2.instance(i));
			}
			return res;
		}
	
	public Instances readArff(String path) throws Exception
	{
		BufferedReader reader = new BufferedReader(
	            new FileReader(path));
	    return new Instances(reader);
	}
	
	
	/*
	 * Parses a literal containing the information of the attributes
	 * format: a(nAtts, [["att1_name", ["att1_value1", ..., "att1_valuen"]], ..., ["attn_name", ["attn_value1", ..., "attn_valuen"]]])
	 * returns a FastVector
	 */
	public FastVector attsInfo(String atts)
	{
	    //get ride of a( and the final )
	    atts = atts.substring(2, atts.length()-1);
	    int nAtts = Integer.parseInt(atts.substring(0, atts.indexOf(","))); //get N of atts
	    atts = atts.substring(atts.indexOf(",")+2, atts.length()-1); //get ride of N,[ and the last]
	    FastVector vector = new FastVector(nAtts); // result
	  
	    while(!atts.equals(""))
	    {
	        int indi = atts.indexOf("]]");
	        String aa = atts.substring(0,indi+2); //get a single attribute
	        atts = atts.substring(indi+2); //delete the attribute	        	       
	        if(atts.length() > 0 && atts.charAt(0) == ',') //in the last attribute this isn't the case
	            atts = atts.substring(1);	        
	        
	        aa = aa.substring(1, aa.length()-1); //get ride of the first [ and last ]
	        int iCo = aa.indexOf(","); // separate in the first colon
	        String name = aa.substring(0, iCo);
	        name = name.substring(1, name.length()-1); //get ride of ""
	        
	       String values =  aa.substring(iCo + 1);
	       values = values.substring(1, values.length()-1); //get ride of []
	       if(values.equals("")) //it's numeric
	           vector.addElement(new Attribute(name));
	       else //we need to extract each value
	       {
	           String[] vs = values.split(","); //get each element
	           FastVector vVals = new FastVector(vs.length);
	           for(int i = 0; i < vs.length; i++)
	           {
	               vVals.addElement(vs[i].substring(1, vs[i].length()-1)); //get ride of ""
	           }
	           vector.addElement(new Attribute(name, vVals));
	       }	           	       
	    }
	   return vector;	 
	}
}
