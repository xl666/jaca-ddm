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
// CArtAgO artifact code for project experimenter

package artifacts;

import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import cartago.*;

public class FileManager extends Artifact 
{
	private String savePath;
	void init(String savePath) 
	{
		this.savePath = savePath; 		 
	}
	
		
	@OPERATION @LINK
	void receiveFile(Instances ins, String fileName)
	{				
		ArffSaver saver = new ArffSaver();
		saver.setInstances(ins);
		try
		 {
			 saver.setFile(new File(this.savePath + "/" + fileName));
			 saver.setDestination(new File(this.savePath + "/" + fileName));  
			 saver.writeBatch();
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	}
	
}

