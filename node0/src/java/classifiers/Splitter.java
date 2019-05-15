package classifiers;

import java.util.ArrayList;

import classifiers.Triplet.Packet;
import classifiers.Triplet.WorkSlotIni;
import moa.core.InstancesHeader;
import utils.InstanceManager;
import weka.core.Instance;
import weka.core.Instances;


public class Splitter implements BaseClassifier, java.io.Serializable
{

	private ArrayList<BaseClassifier> models;
	private String baseClass;
	private int trained = 0;
	
	public Splitter(String baseClass) 
	{
		this.baseClass = baseClass;
	}
	
	public Splitter() 
	{
		this.baseClass = "weka.classifiers.trees.J48";
		this.models = new ArrayList<BaseClassifier>();
	}

	public void induce(Instances instances, int threads, boolean stratify)
	{
		//reuse triplet implementation
        Triplet dummy = new Triplet();
		Packet[] r1 = dummy.buildBase(instances, threads, this.baseClass, this.baseClass, stratify);
		this.models = new ArrayList<BaseClassifier>();
		for(int i = 0; i < threads; i++)
		{
			models.add(r1[i].getModel());
		}						
	}
	
	public void addBaseModel(BaseClassifier mo)
	{
		this.models.add(mo);
	}
	
	public ArrayList<BaseClassifier> getModels()
	{
		return this.models;
	}
	
	public void induce(Instances instances) {
		
		int cores = Runtime.getRuntime().availableProcessors();
		//induce on the maximum number of parallel processors
		induce(instances, cores, false);
		
	}

	public void trainOnInstance(Instance instance) {
		// TODO Auto-generated method stub
		
	}
	
	private int max(int[] ar)
	{
		int m = ar[0];
		int res = 0;
		for(int i = 1; i < ar.length; i++)
		{
			if(ar[i] > m)
			{
				m = ar[i];
				res = i;
			}
		}
		return res;
	}

	//classifies by mayority vote
	public double classifyInstance(Instance instance) {
		int[] classCount = new int[instance.numClasses()];
		//ini at 0
		for(int i = 0; i < classCount.length; i++)
		{
			classCount[i] = 0;
		}
		for(BaseClassifier mo : this.models)
		{
			classCount[(int)mo.classifyInstance(instance)]++;
		}
		
		return max(classCount);
	}

	public boolean correctlyClassifies(Instance instance) {
		
		return classifyInstance(instance) == instance.classValue();
	}

	public void setModelContext(InstancesHeader head) {
		// TODO Auto-generated method stub
		
	}

	public void prepareForUse() {
		// TODO Auto-generated method stub
		
	}

	public int getExamplesTrained() {
		return this.trained;
	}

	public void setExamplesTrained(int examples) {
		this.trained = examples;
		
	}

	public String getComplexity() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveModel(String path) {
		// TODO Auto-generated method stub
		
	}

}
