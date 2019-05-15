package artifacts;

import java.util.ArrayList;

import utils.InstanceManager;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import classifiers.BaseClassifier;
import classifiers.WrapperBagging;
import cartago.ARTIFACT_INFO;
import cartago.Artifact;
import cartago.ArtifactId;
import cartago.LINK;
import cartago.OPERATION;
import cartago.OUTPORT;
import cartago.OpFeedbackParam;

@ARTIFACT_INFO(
		  outports = {
		    @OUTPORT(name="port1")  
		  }
		)

public class ClassifierBagging extends Artifact
{
	private Instances trainData;
	private WrapperBagging model;
	private FastVector attInfo;
	private int models = 0;

	//classifiers is by default J48
	void init(String params, int models, int threads, String classifier, String paramsClassifier)
	{
		params = params.substring(2, params.length()-1); //get ride of b( and the last )
		String atributos = params.substring(params.indexOf(",")+1);		
		//Gets the information of the attributes 
		this.attInfo = new InstanceManager().attsInfo(atributos);		
	    //Creates a new Instances object based on the information of the attributes to Train Data 
	    Instances data = new Instances("DB", this.attInfo,0);			   
	    
	    //Setting class attribute
		data.setClassIndex(data.numAttributes() - 1);
		
		if(threads == 0) //automatically decide
		{
			threads = Runtime.getRuntime().availableProcessors();
		}
		
		if(models == 0) //automatically decide
			models = threads;
		
		this.models = models;
		
		this.trainData = data;
		
		model = new WrapperBagging();
		
		
		if(paramsClassifier.split(" ").length > 0)
		{
			
		}
		
		String[] options;
		String [] elemsParamasClassifier = splitTrim(paramsClassifier, " "); 
		
		if(elemsParamasClassifier.length > 0)
			options = new String[6+elemsParamasClassifier.length + 1]; //hay que considerar el --
		else
			options = new String[6];
		
		if(classifier.trim().equals(""))
			classifier = "weka.classifiers.trees.J48";
		
        options[0] = "-num-slots";
        options[1] = "" + threads;
        options[2] = "-I"; 
        options[3] = "" + models;
        options[4] = "-W";
        options[5] = classifier;
        
        if(elemsParamasClassifier.length > 0) //
        {
        	options[6] = "--";
        	int j = 0;
        	for(int i = 7; i < elemsParamasClassifier.length; i++)
        	{
        		options[i] = elemsParamasClassifier[j];
        		j++;
        	}        	
        }
        
        try 
        {
			model.setOptions(options);
		} 
        catch (Exception e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
        
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
		this.model.setExamplesTrained(this.trainData.numInstances()* this.models); //each models uses same ammount
		this.model.induce(this.trainData);
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
	
	@OPERATION
	void sendBag(ArtifactId aID)
	{
		  try
		  {
			  execLinkedOp(aID, "addBag", this.model);
		  }
		  catch(Exception e)
		  {
			  e.printStackTrace();
		  }
	  }
	  
	  @LINK
	  void receiveModel(WrapperBagging model) 
	  {
		  this.model = model;
		  signal("modelReceived");
	  }
	  
	  @LINK
	  void correctlyClassifies(OpFeedbackParam<Instance> ins, OpFeedbackParam<Boolean> res)
	  {
		res.set(this.model.correctlyClassifies(ins.get()));
	  }
	  
	    private static String[] splitTrim(String s, String pattern)
	    {
		String[] inter = s.split(pattern);
		ArrayList<String> res = new ArrayList<String>();
		for(String e : inter)
		    {
			if(!e.trim().equals(""))
			    res.add(e);
		    }
		return res.toArray(new String[res.size()]);
	    }

}
