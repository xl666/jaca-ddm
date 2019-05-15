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
// CArtAgO artifact code for project aprendizajePropV4

package artifacts;

import java.io.FileWriter;
import java.io.PrintWriter;

import cartago.*;

public class Experimenter extends Artifact 
{
    private PrintWriter outRaw; //to write in the result archive
    private PrintWriter outSum;
    private PrintWriter outConf;
	void init(String fileNameRaw, String fileNameSum, String fileConfusion) 
	{
	    try
	    {	        	        
	        FileWriter outFile = new FileWriter(fileNameRaw, true); //append
	        outRaw = new PrintWriter(outFile);
	        FileWriter outFile2 = new FileWriter(fileNameSum, true); //append
	        outSum = new PrintWriter(outFile2);
	        FileWriter outFile3 = new FileWriter(fileConfusion, true); //append
	        outConf = new PrintWriter(outFile3);
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }
	}
	
	 @OPERATION
	 	//to raw
	    void printResult(Object... args){       
	        StringBuilder sout = new StringBuilder();
	        for (int i = 0; i < args.length; i++) {
	            sout.append(args[i].toString());
	        }	        
	        outRaw.print(sout.toString());     
	        outRaw.flush();
	     
	    }
	 
	 
	 @OPERATION
	 	//to raw
	    void printConfusion(Object... args){       
	        StringBuilder sout = new StringBuilder();
	        for (int i = 0; i < args.length; i++) {
	            sout.append(args[i].toString());
	        }	        
	        outRaw.print(sout.toString());
	        outRaw.print("\n");
	        outRaw.flush();
	     
	    }
	 
	 @OPERATION
	 	//to raw
	    void printSummary(Object... args){       
	        StringBuilder sout = new StringBuilder();
	        for (int i = 0; i < args.length; i++) {
	        	if(args[i].toString().equals(" $\\\\pm$ "))
	        		args[i] = " $\\pm$ ";
	            sout.append(args[i].toString());
	        }	   
	        sout.append("\\\\ \n \\hline \n");
	        outSum.print(sout.toString());     
	        outSum.flush();
	     
	    }
	 @OPERATION
     void exit()
	 {       
         outRaw.close();
         outSum.close();
         outConf.close();
         System.exit(0);      
     }
	
	 
}

