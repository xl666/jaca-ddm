package classifiers;

import java.util.ArrayList;

import artifacts.Oracle;

import moa.core.InstancesHeader;
import utils.InstanceManager;
import utils.ParallelSearch;
import weka.core.Instance;
import weka.core.Instances;



public class Triplet implements BaseClassifier, java.io.Serializable
	{
	
	private BaseClassifier left;
	private BaseClassifier right;
	private BaseClassifier counter;
	

	//left and right should be already induced, counter not
	public Triplet(BaseClassifier left, BaseClassifier right, BaseClassifier counter) 
	{
		this.left = left;
		this.right = right;		
		this.counter = counter;
	}
	
	public Triplet()
	{
		
	}
	
	//splits data as it is
	private ArrayList<Instances> genDirectChunks(Instances instances, int splits)
	{
		int factor = instances.numInstances()/splits;
		ArrayList<Instances> res = new ArrayList<Instances>(); 
		for(int i = 0; i < splits-1; i++)
		{
			int ini = i * factor;
			int end = ini + factor;
			
			res.add(InstanceManager.slice(instances, ini, end));			
		}
		res.add(InstanceManager.slice(instances, (splits-1)*factor, instances.numInstances()));
		
		return res;
	}
	
	private ArrayList<Instances> genStratifiedChunks(Instances instances, int splits)
	{		
		return Oracle.generatePartitions(instances, splits);		
	}
	
	
	
	public Packet[] buildBase(Instances instances, int threads, String baseClass, String counterClass, boolean stratify)
	{
		//it needs at lest 2 threads
				if(threads < 2)
				{
					this.counter = new WekaClassifier(counterClass);			
					this.induce(instances);
					this.left = this.counter;
					this.right = this.counter;
					Packet[] r1 = new Packet[1];
					r1[0] = new Packet(this.counter, instances, instances);
					return r1;
				}
				
				//create initial models in parallel
				Packet[] r1 = new Packet[threads];
				Thread[] hs = new Thread[threads];
				
				ArrayList<Instances> chunks = null;
				if(stratify)
					chunks = genStratifiedChunks(instances, threads);
				else
					chunks = genDirectChunks(instances, threads);
				
				for(int i = 0; i < threads; i++)
				{										
					Instances data = chunks.get(i);				
					Thread h = new Thread(new WorkSlotIni(i, data, r1, baseClass));
					h.start();
					hs[i] = h;
				}				
				
				//wait all ready
				for(int i = 0; i < hs.length; i++)
				{
					try {
						hs[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return r1;
	}
	
	//threaded version of induce it executes all the process
	public void induce(Instances instances, int threads, String baseClass, String counterClass, boolean stratify)
	{
		//manage round process
		Packet[] res = buildBase(instances, threads, baseClass, counterClass, stratify);
		if(res.length == 1)
			return;
		while(res.length > 1)
		{
			int pairs = res.length/2;
			int extra = res.length % 2; 			
			Packet[] r2 = new Packet[pairs+extra];
			if(extra == 1) //put last element at the beggining of new res
				r2[0] = res[res.length-1];				
			
			Thread[] thSlots = new Thread[pairs];
			for(int i = extra, j = 0, k = 0; i < (pairs+extra); i++, j+=2, k++)
			{
				Thread hx = new Thread(new WorkSlot(i, r2, res[j], res[j+1], counterClass, threads/4));
				hx.start();
				thSlots[k] = hx;
			}
			//wait for the end of all
			for(int i = 0; i < thSlots.length; i++)
			{
				try {
					thSlots[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			res = r2;
		}
		
		//this instance becomes the result
		this.left = ((Triplet)res[0].getModel()).getLeft();
		this.right = ((Triplet)res[0].getModel()).getRight();
		this.counter = ((Triplet)res[0].getModel()).getCounter();
	}

	public BaseClassifier getLeft() {
		return left;
	}

	public BaseClassifier getRight() {
		return right;
	}

	public BaseClassifier getCounter() {
		return counter;
	}

	public void induce(Instances instances) {		
		this.counter.induce(instances);		
		this.counter.setExamplesTrained(instances.numInstances());
	}

	public void trainOnInstance(Instance instance) {
		// TODO Auto-generated method stub
		
	}

	public double classifyInstance(Instance instance) {
		double c1 = this.left.classifyInstance(instance);
		double c2 = this.right.classifyInstance(instance);
		if(c1 == c2)
			return c1;
		return this.counter.classifyInstance(instance);		
	}

	public boolean correctlyClassifies(Instance instance) {
		return this.classifyInstance(instance) == instance.classValue();		
	}

	public void setModelContext(InstancesHeader head) {
		// TODO Auto-generated method stub
		
	}

	public void prepareForUse() {
		// TODO Auto-generated method stub
		
	}

	public int getExamplesTrained() {
		return this.counter.getExamplesTrained() + this.left.getExamplesTrained() + this.right.getExamplesTrained();
	}

	public void setExamplesTrained(int examples) {
		this.counter.setExamplesTrained(examples);
		
	}

	public String getComplexity() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	class Packet
	{
		private BaseClassifier model;
		private Instances train;
		private Instances good;
		
		public Packet(BaseClassifier model, Instances train, Instances good)
		{
			this.model = model;
			this.train = train;
			this.good = good;
		}
		
		public BaseClassifier getModel() {
			return model;
		}
		public Instances getTrain() {
			return train;
		}
		public Instances getGood() {
			return good;
		}
		
	}
	
	//class for tread work ar the beggingin of the process
	class WorkSlotIni implements Runnable
	{
		private int index;
		private Instances data;
		private Packet[] res; //this is a read-only shared structure
		private WekaClassifier model;
		
		public WorkSlotIni(int index, Instances data, Packet[] res, String baseClassifier)
		{
			this.index = index;
			this.data = data;
			this.res = res;
			this.model = new WekaClassifier(baseClassifier);
		}
		

		public void run() 
		{
			model.induce(this.data);
			Packet aux = new Packet(this.model, this.data, this.data);
			res[this.index] = aux;	
		}
		
	}
	
	//class for normal tread work 
		class WorkSlot implements Runnable
		{
			private int index;
			private Packet[] res;
			private Packet left;
			private Packet right;
			private Triplet model;
			int threadsParallelCounter;
			private Instances[] r1;
			private Instances[] r2;
			
			public WorkSlot(int index, Packet[] res, Packet left, Packet right, String classCounter, int threadsParallelCounter)
			{
				WekaClassifier counter = new WekaClassifier(classCounter);
				this.model = new Triplet(left.getModel(), right.getModel(), counter);
				this.index = index;
				this.res = res;
				this.left = left;
				this.right = right;
				this.threadsParallelCounter = threadsParallelCounter;
			}

			public void run() 
			{
				//search counter left-right
				final ParallelSearch ps1 = new ParallelSearch(this.right.getGood(), this.left.getModel(), this.threadsParallelCounter);				
				
				Thread t1 = new Thread() 
				{
				    public void run() 
				    {
				    	//r1 needs to be a property not local var
				        r1 = ps1.searchCounter();
				    }
				};
				t1.start();
				
				//search counter right-left
				final ParallelSearch ps2 = new ParallelSearch(this.left.getGood(), this.right.getModel(), this.threadsParallelCounter);
				Thread t2 = new Thread() 
				{
				    public void run() 
				    {
				        r2 = ps2.searchCounter();
				    }
				};
				t2.start();
												
				
				//wait for both threads
				try 
				{
					t1.join();
					t2.join();
				} catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//join results
				Instances train = InstanceManager.joinInstances(r1[0], r2[0]);
				Instances good = InstanceManager.joinInstances(r1[1], r2[1]);
				
				//induce counter
				this.model.induce(train);
				
				//save res
				res[index] = new Packet(this.model, train, good);
			}
			
		}

		public void saveModel(String path) {
			// TODO Auto-generated method stub
			
		}

}
