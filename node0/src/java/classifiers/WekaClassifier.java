package classifiers;

/*
 This class represents any weka classifier
 Uses reflection to instantiate a particular classifier
 */

import moa.core.InstancesHeader;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class WekaClassifier implements Classifier, BaseClassifier, java.io.Serializable
{
	private Classifier core;
	private int trainExamples = 0;
	
	//can only work with parameterless constructors
	public WekaClassifier(String wekaClassifierClass) 
	{
		try {
			
			core = (Classifier) Class.forName(wekaClassifierClass).newInstance();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public WekaClassifier() 
	{
		try {
			//use j48 by default
			core = (Classifier) Class.forName("weka.classifiers.trees.J48").newInstance();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void induce(Instances instances) {
		try {
			core.buildClassifier(instances);
			this.trainExamples = instances.numInstances();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void trainOnInstance(Instance instance) {
		// not supported
		
	}

	public boolean correctlyClassifies(Instance instance) {
		try {
			return core.classifyInstance(instance) == instance.classValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
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

	public String getComplexity() {
		// It is not possible to measure in this case
		return null;
	}

	public void buildClassifier(Instances arg0) throws Exception {
		this.core.buildClassifier(arg0);
		
	}

	public double classifyInstance(Instance arg0) 
	{
		try {
			return this.core.classifyInstance(arg0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public double[] distributionForInstance(Instance arg0) throws Exception {
		return this.core.distributionForInstance(arg0);		
	}

	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveModel(String path) 
	{
		try {
			SerializationHelper.write(path, this.core);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
