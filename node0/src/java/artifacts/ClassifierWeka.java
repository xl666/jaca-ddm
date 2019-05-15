package artifacts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utils.InstanceManager;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import classifiers.BaseClassifier;
import classifiers.CUDATree;
import classifiers.J48CUDATree;
import classifiers.WekaClassifier;
import classifiers.WrapperJ48;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class ClassifierWeka extends Artifact
{

	public ClassifierWeka() {
		// TODO Auto-generated constructor stub
	}
	
	private Instances trainData;
	private BaseClassifier model;	
	private FastVector attInfo;
	

	
	void init(String params, String wekaClassifierClass) throws Exception 
	{			    
		params = params.substring(2, params.length()-1); //get ride of b( and the last )

		String atributos = params.substring(params.indexOf(",")+1);
		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);
		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    this.trainData = new Instances("DB", attInfo,0);
		
	    //Setting class attribute
		trainData.setClassIndex(trainData.numAttributes() - 1);	
		
       
             
		this.model = new WekaClassifier(wekaClassifierClass);
		
		
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
