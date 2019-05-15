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
	package artifacts;

import java.awt.geom.CubicCurve2D;
import java.io.File;

import classifiers.BaseClassifier;
import classifiers.CUDATree;
import classifiers.J48CUDATree;
import classifiers.WrapperJ48;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import cartago.*;
import utils.InstanceManager;


@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="port1")  // Port used to link with Oraculo
		  }
		)

public class ClassifierJ48 extends Artifact {
	private Instances trainData;
	private BaseClassifier model;	
	private FastVector attInfo;
	private CUDATree gpuModel;

	
	void init(String params, boolean pruning) throws Exception 
	{			    
		params = params.substring(2, params.length()-1); //get ride of b( and the last )

		String atributos = params.substring(params.indexOf(",")+1);
		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);
		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    this.trainData = new Instances("DB", attInfo,0);
		
	    //Setting class attribute
		trainData.setClassIndex(trainData.numAttributes() - 1);	
		
				
		//Creates a new Tree object
        String[] optionsTree = new String[1];
        optionsTree[0] = "-U";    //unpruned tree  
                
        WrapperJ48 tree = new WrapperJ48();
        
        if(!pruning)
        	tree.setOptions(optionsTree);        
		this.model = tree;		
		
		
	}
	
	void init(String params, boolean pruning, boolean gpuEnabled) throws Exception 
	{			    
		params = params.substring(2, params.length()-1); //get ride of b( and the last )

		String atributos = params.substring(params.indexOf(",")+1);
		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);
		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    this.trainData = new Instances("DB", attInfo,0);
		
	    //Setting class attribute
		trainData.setClassIndex(trainData.numAttributes() - 1);	
		
				
		//Creates a new Tree object
        String[] optionsTree = new String[1];
        optionsTree[0] = "-U";    //unpruned tree  
                
        J48CUDATree tree = new J48CUDATree(this.trainData);
        
        if(!pruning)
        	tree.setOptions(optionsTree);        
		this.model = tree;		
		
		
	}
	
	
	@LINK
	 //Add a new instance into a data set
	void addInstance(Instance ins) 
	{
		 trainData.add(ins);		
	}
	
	@LINK
	 //Add a packet of instances
	void addInstances(Instances instances) 
	{
		for(Instance ins : instances)
		{
			trainData.add(ins);
		}
	}
				
	  	  
	@OPERATION
	void induce()
	{
		this.model.induce(this.trainData);
		this.model.setExamplesTrained(this.trainData.numInstances()); //the number of examples used for the induction
		signal("inducing", getOpUserName());
	}
	
	@OPERATION
	void induceGPU() //makes an induction and creates the corresponding GPU representation
	{
		induce();
		this.gpuModel = ((J48CUDATree)this.model).convert();
	}
	  
	@OPERATION
	void sendModel(ArtifactId aID)
	{
		  try
		  {
			  execLinkedOp(aID, "receiveModel", this.model);
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
	  
	  @LINK
	  void receiveModel(BaseClassifier model) 
	  {
		  this.model = model;
		  signal("modelReceived");
	  }
	  
	  @OPERATION
		void sendModelGPU(ArtifactId aID)
		{
			  try
			  {
				  execLinkedOp(aID, "receiveModelGPU", this.gpuModel);
			  }
			  catch(Exception e)
			  {
				  e.printStackTrace();
			  }
		  }
		  
		  @LINK
		  void receiveModelGPU(CUDATree gpuModel) 
		  {
			  this.gpuModel = gpuModel;
			  signal("modelReceived");
		  }
	 	 
	  
	  @LINK
	  void correctlyClassifies(OpFeedbackParam<Instance> ins, OpFeedbackParam<Boolean> res)
	  {
		res.set(this.model.correctlyClassifies(ins.get()));
	  }
	 
	  @OPERATION 
		 public void dumpInstances2File(String path)
		 {
			 ArffSaver saver = new ArffSaver();
				saver.setInstances(this.trainData);
				try
				 {
					 saver.setFile(new File(path));
					 saver.setDestination(new File(path));  
					 saver.writeBatch();
				 }
				 catch(Exception e)
				 {
					 e.printStackTrace();
				 }
		 }
	
}


