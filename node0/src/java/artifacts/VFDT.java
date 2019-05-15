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
import java.io.BufferedReader;
import java.io.FileReader;

import classifiers.BaseClassifier;
import classifiers.WrapperVFDT;
import moa.classifiers.Classifier;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.InstancesHeader;
import utils.InstanceManager;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import cartago.ARTIFACT_INFO;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OUTPORT;
import cartago.OpFeedbackParam;
import moa.classifiers.core.splitcriteria.MODLSplitCriterion;



@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="port1")  
		  }
		)

public class VFDT extends Artifact
{
	private BaseClassifier model;
	private FastVector attInfo;
	
	
	void init(String params)
	{
		params = params.substring(2, params.length()-1); //get ride of b( and the last )
		String atributos = params.substring(params.indexOf(",")+1);		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    Instances data = new Instances("DB", this.attInfo,0);			   
	    
	    //Setting class attribute
		data.setClassIndex(data.numAttributes() - 1);				
				
        
        WrapperVFDT tree = new WrapperVFDT();
        tree.splitCriterionOption.setValueViaCLIString("moa.classifiers.core.splitcriteria.MODLSplitCriterion");
		
		this.model = tree;
		
		this.model.setModelContext(new InstancesHeader(data));
		this.model.prepareForUse();		
	}
	
	@LINK
	//Add a new instance into a data set
	void trainOnInstance(OpFeedbackParam<Instance> ins) 
	{		
		this.model.trainOnInstance(ins.get());	
		signal("inducing", getOpUserName()); //returns the name of the agent doing the induction
	}
	
	//Send a tree to a another artifact. doesn't work currently because opfeedback objects aren't selrialized	  
	  @LINK 
	  void getModel(OpFeedbackParam<BaseClassifier> model){				 
		  model.set(this.model);
		  try
		  {
			  while(model.get() == null) //to make sure that the action is finished 
		            Thread.sleep(10);
		  }catch(Exception e){}
		  signal("sendingModel", getOpUserName());
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
	 
	  @LINK
	  void addInstance(Instance ins) 
	  {
		  this.model.trainOnInstance(ins);	
		  signal("inducing", getOpUserName());
	  }
	  
	  @LINK
		 //Add a packet of instances
		void addInstances(Instances instances) 
		{
			for(Instance ins : instances)
			{
				this.model.trainOnInstance(ins);	
			}
		}
	  
	  @LINK
		 //Add a packet of instances
		void pp(OpFeedbackParam<String> res) 
		{
			res.set("vfdt");
		}	  
	  
	  @LINK
	  void correctlyClassifies(Instance ins, OpFeedbackParam<Boolean> res)
	  {
		res.set(this.model.correctlyClassifies(ins));
	  }
	  
	  @OPERATION
	  void induce()
	  {
		  //do nothing, keep for compatibility
	  }
	  

}
