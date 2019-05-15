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
import weka.core.Instance;
import weka.core.Instances;

/*
 * Base interface to wrap classifiers from thir party librarias such as moa or weka
 */

public interface BaseClassifier 
{
	public void induce(Instances instances);
	public void trainOnInstance(Instance instance);
	public double classifyInstance(Instance instance);
	public boolean correctlyClassifies(Instance instance);	
	public void setModelContext(InstancesHeader head);
	public void prepareForUse();
	public int getExamplesTrained();
	public void setExamplesTrained(int examples);
	public String getComplexity();
	public void saveModel(String path);
}
