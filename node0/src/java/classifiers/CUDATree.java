package classifiers;

import static jcuda.driver.JCudaDriver.cuMemAlloc;
import static jcuda.driver.JCudaDriver.cuMemcpyHtoD;

import java.io.IOException;

import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdeviceptr;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import static jcuda.driver.JCudaDriver.cuMemFree;

public class CUDATree implements java.io.Serializable
{
	private static final long serialVersionUID = 7526472295622776147L;
	
	//Node
	private int NUMNODES ;
	public int MAX_NUM_ARCS;
	public int attributes [];
	public int isLeaf [];
	public int numbersOfArcs [];
	//public int arcIndices [];
	
	//Arc
	public int narcs;
	public int evalTypes [];
	public float vals [];
	public int nodeIndices [];
	
	
	//cuda
	public CUdeviceptr deviceAttributes;
	public CUdeviceptr deviceIsLeaf;
	public CUdeviceptr deviceNumberOfArcs;
	public CUdeviceptr deviceEvalTypes;
	public CUdeviceptr deviceVals;
	public CUdeviceptr deviceNodeIndices;
	
	//misc
	public int nLeaf = 0; //to know the number of leaves;
	public int examplesTrained = 0; //to know how many examples where used to train this model

	
	public CUDATree(int n, int arcos) 
	{
		NUMNODES = n;
		MAX_NUM_ARCS = arcos;
		attributes = new int [n];
		isLeaf =  new int [n];
		numbersOfArcs = new int[n]; 
		//arcIndices =  new int [n * MAX_NUM_ARCS];
		
		narcs = NUMNODES * MAX_NUM_ARCS;
		evalTypes = new int [narcs];
		vals = new float [narcs];
		nodeIndices = new int [narcs];
	}
	
	public static  float[] shrink(double[] ar)
	{
		float[] res = new float[ar.length];
		for(int i = 0;i <  ar.length; i++)
		{
			res[i] = (float)ar[i];
		}		
		return res;
	}
	
	public int classify(float[] ins) throws Exception
	{
		int actual = 0;
		while(this.isLeaf[actual] != 1)
		{			
			int auxx = actual;
			int att = this.attributes[actual];			
			float insVal = ins[att]; //the actual value of the attribute for this instance
			//System.out.println("Atributoo: " + att + "vaal :" + insVal);
			for(int i = 0; i < this.numbersOfArcs[actual]; i++) //traverse each arc
			{
				int arcIndi = (actual * this.MAX_NUM_ARCS) + i; //the actual index of the arc
				float arcVal = this.vals[arcIndi];				
				int evType = this.evalTypes[arcIndi];
				//System.out.println("aaaarc :" + arcVal + "evvtype" + evType);
				if(evType == 0)
				{
					if(insVal <=  arcVal) //<=
					{			
						actual = this.nodeIndices[arcIndi];
						break;
					}
						
					else
					{
						continue;
					}
				}
				else if(evType == 1)
				{
					if(insVal >  arcVal) // >
					{
						actual = this.nodeIndices[arcIndi];
						break;
					}
					else
						continue;
				}
				else if(evType == 2)
				{
					if(insVal ==  arcVal) // =
					{						
						actual = this.nodeIndices[arcIndi];
						break;
					}
					else
						continue;
				}									
			}
			if(auxx == actual) //there wasn't an appropiate path, None value found 
			{
				System.out.println("Aquí se ciclaría :" + insVal);
				return 0;
			}
			
		}
		
		return this.attributes[actual];
	}
	
	public void loadToDevice()
	{
		
		deviceAttributes = new CUdeviceptr();
		cuMemAlloc(deviceAttributes, this.attributes.length * Sizeof.INT);
        cuMemcpyHtoD(deviceAttributes, Pointer.to(this.attributes),
            this.attributes.length * Sizeof.FLOAT);
        
        deviceIsLeaf = new CUdeviceptr();
		cuMemAlloc(deviceIsLeaf, this.isLeaf.length * Sizeof.INT);
        cuMemcpyHtoD(deviceIsLeaf, Pointer.to(this.isLeaf),
            this.isLeaf.length * Sizeof.INT);
        
        deviceNumberOfArcs = new CUdeviceptr();
		cuMemAlloc(deviceNumberOfArcs, this.numbersOfArcs.length * Sizeof.INT);
        cuMemcpyHtoD(deviceNumberOfArcs, Pointer.to(this.numbersOfArcs),
            this.numbersOfArcs.length * Sizeof.INT);
        
        deviceEvalTypes = new CUdeviceptr();
		cuMemAlloc(deviceEvalTypes, this.evalTypes.length * Sizeof.INT);
        cuMemcpyHtoD(deviceEvalTypes, Pointer.to(this.evalTypes),
            this.evalTypes.length * Sizeof.INT);
        
        deviceVals = new CUdeviceptr();
		cuMemAlloc(deviceVals, this.vals.length * Sizeof.FLOAT);
        cuMemcpyHtoD(deviceVals, Pointer.to(this.vals),
            this.vals.length * Sizeof.FLOAT);
        
        deviceNodeIndices = new CUdeviceptr();
		cuMemAlloc(deviceNodeIndices, this.nodeIndices.length * Sizeof.INT);
        cuMemcpyHtoD(deviceNodeIndices, Pointer.to(this.nodeIndices),
            this.nodeIndices.length * Sizeof.INT);
        
	}
	
	public void freeDevice()
	{
		 cuMemFree(deviceAttributes);
	     cuMemFree(deviceIsLeaf);
	     cuMemFree(deviceNumberOfArcs);
	     cuMemFree(deviceEvalTypes);
	     cuMemFree(deviceVals);
	     cuMemFree(deviceNodeIndices);
	}
	
	//how to serialize the object
	//Node
	
	private synchronized void writeObject(java.io.ObjectOutputStream stream) throws IOException 
	{
		stream.writeInt(this.NUMNODES);
		stream.writeInt(this.MAX_NUM_ARCS);
		stream.writeInt(this.attributes.length);		
		for (int i=0; i<attributes.length; i++)
		{
			stream.writeInt(this.attributes[i]);
		}
		
		stream.writeInt(this.isLeaf.length);		
		for (int i=0; i<isLeaf.length; i++)
		{
			stream.writeInt(this.isLeaf[i]);
		}
		
		stream.writeInt(this.numbersOfArcs.length);		
		for (int i=0; i<numbersOfArcs.length; i++)
		{
			stream.writeInt(this.numbersOfArcs[i]);
		}
		
		stream.writeInt(this.narcs);
		
		stream.writeInt(this.evalTypes.length);		
		for (int i=0; i<evalTypes.length; i++)
		{
			stream.writeInt(this.evalTypes[i]);
		}
		
	
		stream.writeInt(this.vals.length);		
		for (int i=0; i<evalTypes.length; i++)
		{
			stream.writeFloat(this.vals[i]);
		}
		
		stream.writeInt(this.nodeIndices.length);		
		for (int i=0; i<evalTypes.length; i++)
		{
			stream.writeInt(this.nodeIndices[i]);
		}
		
		stream.writeInt(this.examplesTrained);
		
		stream.writeInt(this.nLeaf);
		
	}
	
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		this.NUMNODES = stream.readInt();
		this.MAX_NUM_ARCS = stream.readInt();
		
		this.attributes = new int[stream.readInt()];
		for(int i = 0; i < attributes.length; i++)
		{
			this.attributes[i] = stream.readInt();
		}
		
		this.isLeaf = new int[stream.readInt()];
		for(int i = 0; i < isLeaf.length; i++)
		{
			this.isLeaf[i] = stream.readInt();
		}
		
		this.numbersOfArcs = new int[stream.readInt()];
		for(int i = 0; i < numbersOfArcs.length; i++)
		{
			this.numbersOfArcs[i] = stream.readInt();
		}
		
		this.narcs = stream.readInt();
		
		this.evalTypes = new int[stream.readInt()];
		for(int i = 0; i < evalTypes.length; i++)
		{
			this.evalTypes[i] = stream.readInt();
		}
		
		this.vals = new float[stream.readInt()];
		for(int i = 0; i < vals.length; i++)
		{
			this.vals[i] = stream.readFloat();
		}
		
		this.nodeIndices = new int[stream.readInt()];
		for(int i = 0; i < nodeIndices.length; i++)
		{
			this.nodeIndices[i] = stream.readInt();
		}
		
		this.examplesTrained = stream.readInt();
		
		this.nLeaf = stream.readInt();
	}
	
}
