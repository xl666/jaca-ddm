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
package classifiers;



import moa.core.InstancesHeader;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WrapperJ48 extends J48 implements BaseClassifier {

	private int trainExamples = 0;
	
	public WrapperJ48()
	{
		super();
	}
	
	public void induce(Instances instances) 
	{
		try
		{
			super.buildClassifier(instances);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}

	}

	//it doesn't do anything, put for compatibility
	public void trainOnInstance(Instance instance) 
	{		

	}


	public double classifyInstance(Instance instance) 
	{
		try
		{
			return super.classifyInstance(instance);
		}
		catch(Exception e){e.printStackTrace();}
		return -1;
	}

	public boolean correctlyClassifies(Instance instance)
	{
		double predicted = this.classifyInstance(instance);
		return predicted == instance.classValue();
		
	}

	//for compatibility
	public void setModelContext(InstancesHeader head) 
	{
		
	}

	//for compatibility
	public void prepareForUse() {
	
		
	}
	
	public int getExamplesTrained()
	{
		return this.trainExamples;
		
	}
	
	public void setExamplesTrained(int examples)
	{
		this.trainExamples = examples;
	}
	
	public String getComplexity()
	{
		return "Number of leaves: " + this.measureNumLeaves() + "\nSize of the tree: " + this.measureTreeSize();
	}

	public void saveModel(String path) {
		try {
			SerializationHelper.write(path, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
