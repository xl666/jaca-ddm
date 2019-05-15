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
import java.util.ArrayList;
import java.util.Random;

import utils.InstanceManager;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import cartago.*;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class Oracle extends Artifact 
{
	void init() 
	{
		
	}
	
	@OPERATION
	/* file is the original arff file
	 * sites is an array of filemanager ids, one for each site, to know where to send the file
	 * testpath is the path in teh server where the test partitions are saved
	 * This operation sends all the data in one shot
	 * */
	void holdOut(String file, double trainPer, Object[] sites, int reps, String testPath, OpFeedbackParam<String> baseName)
	{
				
		String[] parts = file.split("/");
		String fileName = parts[parts.length-1];
		baseName.set(fileName);
		
		Instances examples = null;
		try
		{
			examples = new InstanceManager().readArff(file);
			examples.setClassIndex(examples.numAttributes() - 1);
			
			/*Discretize filter = new Discretize();
			filter.setInputFormat(examples); 
			examples = Filter.useFilter(examples, filter);
			
			ReplaceMissingValues rep = new ReplaceMissingValues();
		    rep.setInputFormat(examples);
		    examples = rep.useFilter(examples, rep);
		    examples.deleteWithMissingClass();*/
						
			for(int i = 0; i < reps; i++)
			{
				String currentName = attachNumberToFile(fileName, i+1); //the current name of the file acording with the iteration
				examples.randomize(new Random());
				Instances[] trainTest = holdoutCut(examples, trainPer);
				Instances train = trainTest[0];
				Instances test = trainTest[1];
				saveTest(test, testPath, currentName);
				
				//create a partition for each site
				ArrayList<Instances> partitions;
				if(sites.length > 1)
					partitions = generatePartitions(train, sites.length);
				else
				{
					partitions = new ArrayList<Instances>();
					partitions.add(train);
				}
				
				int indi = 0;
				for(Object oaid: sites)
				{
					ArtifactId aid = (ArtifactId)oaid;
				
					 try
					  {
						 execLinkedOp(aid, "receiveFile", partitions.get(indi), currentName);
					  }
					  catch(Exception e)
					  {
						  e.printStackTrace();
					  }
					indi++;
				}
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
	}
	
	@OPERATION
	void crossValidation(String file, int folds, Object[] sites, int reps, String testPath, OpFeedbackParam<String> baseName)
	{
				
		String[] parts = file.split("/");
		reps = (int)(reps/folds); //normalize the reps 
		String fileName = parts[parts.length-1];
		baseName.set(fileName);
		
		Instances examples = null;
		try
		{
			examples = new InstanceManager().readArff(file);
			examples.setClassIndex(examples.numAttributes() - 1);
			
			/*Discretize filter = new Discretize();
			filter.setInputFormat(examples); 
			examples = Filter.useFilter(examples, filter);
			
			ReplaceMissingValues rep = new ReplaceMissingValues();
		    rep.setInputFormat(examples);
		    examples = rep.useFilter(examples, rep);
		    examples.deleteWithMissingClass();*/
						
			for(int i = 0; i < reps; i++)
			{
				examples.randomize(new Random());
				examples.stratify(folds); //stratify data
				for(int j = 0; j < folds; j++)
				{
					String currentName = attachNumberToFile(fileName, (j+1)+(i*folds)); //the current name of the file acording with the iteration
					Instances train = examples.trainCV(folds, j);
					Instances test = examples.testCV(folds, j);
					saveTest(test, testPath, currentName);
					//create a partition for each site
					ArrayList<Instances> partitions;
					if(sites.length > 1)
						partitions = generatePartitions(train, sites.length);
					else
					{
						partitions = new ArrayList<Instances>();
						partitions.add(train);
					}
					
					int indi = 0;
					for(Object oaid: sites)
					{
						ArtifactId aid = (ArtifactId)oaid;
					
						 try
						  {
							 execLinkedOp(aid, "receiveFile", partitions.get(indi), currentName);
						  }
						  catch(Exception e)
						  {
							  e.printStackTrace();
						  }
						indi++;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	private Instances[] holdoutCut(Instances data, double trainPercent)
	{
		Instances[] res = new Instances[2];
		int tam = data.numInstances();
		int nTrain = (int)(trainPercent * tam);
		int nTest = tam - nTrain;
		res[0] = new Instances(data, 0, nTrain);
		res[1] = new Instances(data, nTrain, nTest);
		
		return res;
	}
	
	private void saveTest(Instances ins, String testPath, String fileName)
	{
		ArffSaver saver = new ArffSaver();
		saver.setInstances(ins);
		try
		 {
			 saver.setFile(new File(testPath + "/" + fileName));
			 saver.setDestination(new File(testPath + "/" + fileName));  
			 saver.writeBatch();
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
	}
	
	private String attachNumberToFile(String baseName, int num)
	{
		
		String[] parts = baseName.split("\\."); //at leas has an extension at the end		
		String res = "";
		for(int i = 0; i < parts.length -1; i++)
		{
			if(i < parts.length -2)
				res += parts[i] + ".";
			else
				res += parts[i];
		}
		res += "" + num +".";
		res += parts[parts.length -1];
		
		return res;
		
	}
	
	public static ArrayList<Instances> generatePartitions(Instances data, int folds)
	{
		ArrayList<Instances> res = new ArrayList<Instances>();
		generatePartitions(data, folds, res);
		return res;
	}
	
	//recursive function that uses cross validation partition
	private static void generatePartitions(Instances data, int folds, ArrayList<Instances> result)
	{
		data.stratify(folds);
		Instances train = data.trainCV(folds, 0);
		Instances test = data.testCV(folds, 0);
		if(folds == 2) //base case, even split
		{			
			result.add(train);
			result.add(test);
			return;
		}
		//the test is always good
		result.add(test);
		generatePartitions(train, folds-1, result);
	}
	
}

