package classifiers;

import java.io.IOException;
import java.util.ArrayList;

import moa.core.InstancesHeader;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

public class BaggingEnsemble implements BaseClassifier, java.io.Serializable
{
	private static final long serialVersionUID = 6526472295622776149L;
	private ArrayList<WrapperBagging> bags;

	public BaggingEnsemble() 
	{
		bags = new ArrayList<WrapperBagging>();
	}

	public void induce(Instances instances) 
	{
		//not used, each bag is already induced		
					
	}

	public void trainOnInstance(Instance instance) {
		// TODO Auto-generated method stub		
	}

	public double classifyInstance(Instance instance) {
		// Returns mayority class from all models
		int[] distri =new int[instance.numClasses()];
		for(WrapperBagging ba : this.bags)
		{
			for(Classifier cla : ba.getClassifiers())
			{
				try {
					int index = (int)cla.classifyInstance(instance);
					distri[index]++;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
		return maxIndex(distri);
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

	public int getExamplesTrained() 
	{
		//sum of all models
		int res = 0;
		for(WrapperBagging ba : this.bags)
		{
			res += ba.getExamplesTrained();
		}
		return res;
	}

	public void setExamplesTrained(int examples) {
		// TODO Auto-generated method stub
		
	}

	public String getComplexity() {
		if(bags.size() > 0)
		{
			Classifier aux = this.bags.get(0).getClassifiers()[0];
			if(aux instanceof J48)
			{
				
				double treeSize = 0;
				double leaves = 0;
				int i = 0;
			for(WrapperBagging wb : this.bags)
			{
				for(Classifier c : wb.getClassifiers())
				{
					J48 a2 = (J48) c;
					treeSize += a2.measureTreeSize();
					leaves += a2.measureNumLeaves();
					i++;
				}				
			}
			return "Number of leaves: " + (leaves/i) + "\nSize of the tree: " + (treeSize/i);
			}
			
		}
		
		return "Not available";
	}
	
	public void addBag(WrapperBagging ba)
	{
		this.bags.add(ba);
	}
	
	private int maxIndex(int[] arr)
	{
		int res = 0;
		int max = arr[0];
		for (int i = 1; i < arr.length; i++)
		{
			if(arr[i]> max)
			{
				max = arr[i];
				res = i;
			}
		}
		return res;
	}
	
	//serialization
	
	private synchronized void writeObject(java.io.ObjectOutputStream stream) throws IOException 
	{
		stream.writeInt(this.bags.size()); // how many bags
		for(WrapperBagging ba : this.bags)
		{
			stream.writeObject(ba); //use default serialization
		}
		
	}
	
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		this.bags = new ArrayList<WrapperBagging>();
		int ns = stream.readInt();
		for(int i = 0; i < ns; i++)
		{
			this.bags.add((WrapperBagging)stream.readObject());
		}
	}

	public void saveModel(String path) {
		// TODO Auto-generated method stub
		
	}

}
