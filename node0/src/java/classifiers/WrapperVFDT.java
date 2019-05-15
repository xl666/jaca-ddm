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

import weka.core.Instance;
import weka.core.Instances;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;

public class WrapperVFDT extends HoeffdingTree implements BaseClassifier
{

	public WrapperVFDT() 
	{
		super();
	}

	public void induce(Instances instances)
	{
		//train with all instances
		for(int i = 0; i < instances.numInstances(); i++)
		{
			super.trainOnInstance(instances.get(i));
		}

	}

	public void trainOnInstance(Instance instance) 
	{
		super.trainOnInstance(instance);

	}
	
	
	public double classifyInstance(Instance instance) 
	{
		double[] votes = super.getVotesForInstance(instance);
		//look for the one that maximizes
		double max = -1;
		int res = 0;
		for(int i = 0; i < votes.length; i++)
		{
			if(votes[i] > max)
			{
				res = i;
				max = votes[i];
			}
		}
		
		return res;
	}
	
	public boolean correctlyClassifies(Instance instance)
	{
		return super.correctlyClassifies(instance);
	}
	
	
	public void setModelContext(InstancesHeader head) 
	{
		super.setModelContext(head);	
	}

		
	public void prepareForUse()
	{
		super.prepareForUse();			
	}
	
	public int getExamplesTrained()
	{
		return (int)this.getModelMeasurements()[0].getValue();
	}
	
	public void setExamplesTrained(int examples)
	{
		
	}
	
	public String getComplexity()
	{
		return "Number of leaves: " + this.getModelMeasurementsImpl()[1] + "\nSize of the tree: " + this.getModelMeasurementsImpl()[0];	
	}

	public void saveModel(String path) {
		// TODO Auto-generated method stub
		
	}

}
