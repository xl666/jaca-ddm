package utils;

import classifiers.BaseClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

//class for doing parallel searchs of counter examples
public class ParallelSearch {

	private Instances data;
	private int threads;
	private BaseClassifier model;
	
	public ParallelSearch(Instances data, BaseClassifier model, int threads) 
	{
		if(threads < 1)
			threads = 1;
		this.data = data;
		this.threads = threads;
		this.model = model;
	}
	
	public Instances[] searchCounter()
	{
		
		Instances[][] todo = new Instances[this.threads][2];
		Thread[] hilos = new Thread[this.threads];
		
		int factor = this.data.numInstances()/this.threads;
		
		//the last thread works with what was left
		for(int i = 0; i < this.threads-1; i++)
		{
			int ini = i * factor;
			int fin = ini + factor;
			

			Thread h = new Thread(new Slot(i, ini, fin, data, model, todo));
			h.start();
			hilos[i] = h;
		}
		Thread h = new Thread(new Slot(threads-1, (threads-1)*factor, data.numInstances(), data, model, todo));
		h.start();
		hilos[threads-1] = h;
		
		for(Thread aux : hilos)
		{
			try {
				aux.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return reduceResults(todo);
	}
	
	
	private  static Instances[] reduceResults(Instances[][] todos)
	{
		Instances[] res = new Instances[2];
		res[0] = todos[0][0];
		res[1] = todos[0][1];
		
		for(int i = 1; i < todos.length; i++)
		{
			InstanceManager.joinInstances(res[0], todos[i][0]);
			InstanceManager.joinInstances(res[1], todos[i][1]);
		}
		
		return res;
	}
	
	

	class Slot implements Runnable
	{
		private int index;
		private int ini;
		private int end;
		private Instances data;
		private BaseClassifier model;
		private Instances[][] res;
		
		public Slot(int index, int ini, int end, Instances data, BaseClassifier model, Instances[][] res)
		{
			this.index = index;
			this.ini = ini;
			this.end = end;
			this.data = data;
			this.model = model;
			this.res = res;
		}
		
		public void run() 
		{			
			this.res[index] = searchCounter(ini, end);
		}
		
		public  Instances[] searchCounter(int ini, int end)
	    {
	    	Instances good = new Instances(this.data, 0);
			Instances res = new Instances(this.data, 0);
			
			for(int i = ini; i < end; i++)
			{
				Instance e = this.data.instance(i);
				if(e.classValue() != this.model.classifyInstance(e))
					res.add(e);
				else
					good.add(e);
			}
			Instances[] k ={res, good};
			return k;
	    }
		
	}
	
}
