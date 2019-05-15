package artifacts;

import java.util.ArrayList;

import weka.core.Instance;
import classifiers.BaggingEnsemble;
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

public class ClassifierBaggingEnsemble extends Artifact
{
	private BaggingEnsemble model;
	
	public ClassifierBaggingEnsemble() 
	{
		// TODO Auto-generated constructor stub
	}
	
	void init()
	{
		model = new BaggingEnsemble();
	}
	
	
	@LINK
	void addBag(WrapperBagging ba) 
	{
		this.model.addBag(ba);
		signal("bagReceived");
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
	  void receiveModel(BaggingEnsemble model) 
	  {
		  this.model = model;
		  signal("modelReceived");
	  }
	  
	  @LINK
	  void correctlyClassifies(OpFeedbackParam<Instance> ins, OpFeedbackParam<Boolean> res)
	  {
		res.set(this.model.correctlyClassifies(ins.get()));
	  }

}
