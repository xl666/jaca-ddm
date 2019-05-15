/*******************************************************************************
 *  ----------------------------------------------------------------------------------------------------------
 *  Copyright (C) 2015 Xavier Limón, Alejandro Guerra-Hernández, Nicandro Cruz-Ramírez, Francisco Grimaldo-Moreno
 * Departmento de Inteligencia Artificial. Universidad Veracruzana. 
 * Departament d’Informàtica. Universitat de València
 *
 *  This file is part of JaCa-DDM.
 *
 *     JaCa-DDM is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     JaCa-DDM is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JaCa-DDM.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  To contact the authors:
 *    xavier120@hotmail.com, aguerra@uv.mx, francisco.grimaldo@uv.es
 *    http://www.uv.mx/aguerra/
 *   http://www.uv.es/grimo/
 * -----------------------------------------------------------------------------------------------------------
 *******************************************************************************/
// CArtAgO artifact code for project experimenter

package artifacts;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;

import cartago.*;

public class Utils extends Artifact 
{
	private long startTime = 0;
	private long endTime = 0;
	
	void init(int initialValue) 
	{
		
	}
	
	//operation used to asign a valid name when round files mode is in use
	@OPERATION
	void attachNumberToFile(String path, String baseName, int num, OpFeedbackParam<String> nName)
	{
		String[] parts = baseName.split("\\."); //at leas has an extension at the end		
		String res = "";
		for(int i = 0; i < parts.length -1; i++)
		{
			if(i < parts.length -2)
				res += parts[i] + ".";
			else
				res += parts[i];
		}
		res += "" + num +".";
		res += parts[parts.length -1];
		if(path.endsWith("/") || path.endsWith("\\"))
			res = path + res;
		else
			res = path + "/" + res;
		
		nName.set(res);
	}
	
	//this version has the path in the same basena
	@OPERATION
	void attachNumberToFile(String baseName, int num, OpFeedbackParam<String> nName)
	{
		
		String[] parts = baseName.split("\\."); //at leas has an extension at the end		
		String res = "";
		for(int i = 0; i < parts.length -1; i++)
		{
			if(i < parts.length -2)
				res += parts[i] + ".";
			else
				res += parts[i];
		}
		res += "" + num +".";
		res += parts[parts.length -1];
		
		
		nName.set(res);
	}
	
	@OPERATION
	//it is difficult to know exactly what IP to return, it returns the first local ip, in reality it is better if the
	// ip address is set in the configuration
	void getLocalIP(OpFeedbackParam<String> ip)
	{
		try
		{

	    Enumeration e = NetworkInterface.getNetworkInterfaces();
	    while(e.hasMoreElements())
	    {
	    	
	        NetworkInterface n = (NetworkInterface) e.nextElement();	       
	        Enumeration ee = n.getInetAddresses();
	        while (ee.hasMoreElements())
	        {
	            InetAddress i = (InetAddress) ee.nextElement();
	            if(i.isSiteLocalAddress())
	            {
	            	//System.out.println(i.getHostAddress());
	            	ip.set(i.getHostAddress());
	            	return;
	            }
	        }
	    }
		}
		catch(Exception e){e.printStackTrace();}
		ip.set("localhost");
	}
	
	
	@OPERATION
	/*
	 * In reality clients is a matrix
	 */
	void selectOneClientPerIp(Object[] clients, OpFeedbackParam<String[]> res)
	{
		System.out.println(((Object[])clients[0]).length);
		HashMap<String, String> map = new HashMap<String, String>();
		for(Object cli : clients)
		{
			Object[] elems = (Object[]) cli;
			String ipPort = (String)elems[1];
			String ip = ipPort.split(":")[0];
			if(!map.containsKey(ip))
			{
				map.put(ip, ipPort);
			}
		}
		String [] rr = new String[map.size()];
		int i = 0;
		for(String key : map.keySet())
		{
			rr[i] = map.get(key);
			i++;
		}
		res.set(rr);
	}
	

	@OPERATION
	void stripPort(String ipPort, OpFeedbackParam<String> res)
	{
		res.set(ipPort.split(":")[0]);
	}
	
	@OPERATION
	void getDirectory(String file, OpFeedbackParam<String> res)
	{
		res.set(file.substring(0, file.lastIndexOf("/")));
	}
	
	@OPERATION
	void getFileName(String file, OpFeedbackParam<String> res)
	{
		if(!file.contains("/"))
		{
			res.set(file);
			return;
		}
		res.set(file.substring(file.lastIndexOf("/")+1));
	}
	
	
	
	 @OPERATION
	    void startTimer()
	 {
			 startTime= System.nanoTime();		 
	 }
		 
		 // Operator to calculate the total time
	@OPERATION
	void stopTimer()
	{
		endTime = System.nanoTime();
				 
	}
	
	@OPERATION
	void timeElapsed(OpFeedbackParam<Double> totalTime)
	{
		double aux = ((double)(endTime - startTime)/1000000000); //time in seconds
		totalTime.set(Math.floor(aux * 10000) / 10000); //take 4 decimals rounding
	}

	
}

