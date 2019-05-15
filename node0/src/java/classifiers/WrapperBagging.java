package classifiers;


import moa.core.InstancesHeader;
import weka.classifiers.Classifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WrapperBagging extends Bagging implements BaseClassifier
{
	
	private int trainExamples = 0;

	public WrapperBagging() 
	{
		super();
	}

	public void induce(Instances instances) 
	{
		try
		{
			
			super.buildClassifier(instances);			
			
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void trainOnInstance(Instance instance) {
		// TODO Auto-generated method stub
		
	}

	public boolean correctlyClassifies(Instance instance) {
		double predicted = this.classifyInstance(instance);
		return predicted == instance.classValue();
	}

	public void setModelContext(InstancesHeader head) {
		// TODO Auto-generated method stub
		
	}

	public void prepareForUse() {
		// TODO Auto-generated method stub
		
	}

	public int getExamplesTrained() {
		return this.trainExamples;
	}

	public void setExamplesTrained(int examples) {
		this.trainExamples = examples;
		
	}

	public String getComplexity() 
	{
		if(this.m_Classifiers.length > 0)
		{
			Classifier aux = this.getClassifiers()[0];
			if(aux instanceof J48)
			{
				
				double treeSize = 0;
				double leaves = 0;
				for(Classifier c : this.getClassifiers())
				{
					J48 a2 = (J48) c;
					treeSize += a2.measureTreeSize();
					leaves += a2.measureNumLeaves();
				}
				return "Number of leaves: " + (leaves/this.getClassifiers().length) + "\nSize of the tree: " + (treeSize/this.getClassifiers().length);
			}
		}
		
		return "Not available";
	}
	
	public double classifyInstance(Instance instance) 
	{
		try
		{			
			return super.classifyInstance(instance);
		}
		catch(Exception e){e.printStackTrace();}
		return -1;
	}
	
	public Classifier[] getClassifiers()
	{
		return this.m_Classifiers;
	}

	public void saveModel(String path) {
		try {
			SerializationHelper.write(path, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
