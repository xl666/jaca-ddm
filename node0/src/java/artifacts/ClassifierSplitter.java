package artifacts;

import utils.InstanceManager;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import classifiers.BaseClassifier;
import classifiers.Splitter;
import classifiers.Triplet;
import classifiers.WekaClassifier;
import cartago.ARTIFACT_INFO;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OUTPORT;

@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="port1")  
		  }
		)

public class ClassifierSplitter  extends Artifact  
{
	private Splitter model;
	private Instances trainData;
	private FastVector attInfo;

	public ClassifierSplitter()
	{
		// TODO Auto-generated constructor stub
	}
	
	void init()
	{
		this.model = new Splitter();
	}
	
	void init(String params, String baseClass)
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
		this.model = new Splitter(baseClass);
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
	void induce(int threads, boolean stratify)
	{
		System.out.println("Inducing with " + this.trainData.numInstances());
		this.model.induce(this.trainData, threads, stratify);
	}
	
	 @LINK
	  void receiveModel(BaseClassifier model) 
	  {
		 //add every single model
		 if(model instanceof Splitter)
		 {
			 Splitter aux = (Splitter) model;
			 for(BaseClassifier ba : aux.getModels())
			 {
				 this.model.addBaseModel(ba);
			 }			 
		 }
		 else
			 this.model.addBaseModel(model);
		  signal("modelReceived");
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
	
	
	
	

}
