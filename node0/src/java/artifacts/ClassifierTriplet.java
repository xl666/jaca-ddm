package artifacts;

import utils.InstanceManager;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import classifiers.BaseClassifier;
import classifiers.CUDATree;
import classifiers.Triplet;
import classifiers.WekaClassifier;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;

public class ClassifierTriplet extends Artifact
{
	private BaseClassifier counter = null;
	private BaseClassifier left = null;
	private BaseClassifier right = null;
	private Triplet tripleta;
	private Instances trainData;
	private FastVector attInfo;
	
	
	void init(String params, String classCounter)
	{
		params = params.substring(2, params.length()-1); //get ride of b( and the last )

		String atributos = params.substring(params.indexOf(",")+1);
		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);
		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    this.trainData = new Instances("DB", attInfo,0);
		
	    //Setting class attribute
		trainData.setClassIndex(trainData.numAttributes() - 1);
		
		//only supports weka classifiers currently
		this.counter = new WekaClassifier(classCounter);
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
		System.out.println("Inducing with " + this.trainData.numInstances());
		this.counter.induce(this.trainData);
	}
	
	//to parallelly create the base level
	@OPERATION
	void induceBase(int threads, String baseClass, String counterClass, boolean stratify)
	{
		this.tripleta = new Triplet();
		System.out.println("Inducing with " + this.trainData.numInstances());
		this.tripleta.induce(this.trainData, threads, baseClass, counterClass, stratify);
	}
	
	@OPERATION
	void sendTriplet(ArtifactId aID)
	{
		  try
		  {
			  execLinkedOp(aID, "receiveModel", this.tripleta);
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
	  
	  @LINK
	  void receiveModel(BaseClassifier model) 
	  {
		  if(this.left == null)
		  {
			  System.out.println("Received left");
			  this.left = model;
		  }
		  else
		  {
			  System.out.println("Received right");
			  this.right = model;
			  //all models received triplet can be made
			  this.tripleta = new Triplet(this.left, this.right, this.counter);
		  }
		  signal("modelReceived");
	  }
	  
	  @OPERATION
		void sendModel(ArtifactId aID)
		{
			  try
			  {
				  execLinkedOp(aID, "receiveModel", this.tripleta);
			  }
			  catch(Exception e)
			  {
				  e.printStackTrace();
			  }
		  }
	
}
