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

import cartago.*;

/*
 * Artifact used to measure network traffic, it requires tcpdump and capinfos to work
 * */
public class TrafficMonitor extends Artifact 
{
	private Process process;
	
	void init() 
	{
		
	}
	
	String ip;
	@OPERATION
	void beginMonitoring(String ip, String dir, int rep)
	{
		this.ip = ip;
		String cmd = "tcpdump -ni any -s0 -U src host " + ip + " and not tcp portrange 0-1024 -w " + dir + "/traffic.cap";
		//String cmd = "/usr/sbin/tcpdump -ni eth0 -s0 -U src host " + ip + " and not tcp portrange 0-1024 -w /tmp/" + rep + "ip" + ip + "traffic.cap";
		//String cmd = "/usr/sbin/tcpdump -ni eth0 -s0 -U src host " + ip + " and not tcp portrange 0-1024  and not udp and not port mysql and not port amqp and not arp and not icmp and not port condor and not port 5666 -w /tmp/" + rep + "ip" + ip + "traffic.cap";
		//System.out.println("Este es el comandoooo!!!!: "+ cmd);
		try
		{
			this.process = Runtime.getRuntime().exec(cmd);
		}
		catch(Exception e)
		{			
			//this.process.destroy();
			e.printStackTrace();
		}		
	}
	
	@OPERATION
	/*
	 * Ends the monitoring and retrieves the traffic in bytes
	 */
	void getTraffic(String dir, OpFeedbackParam<Double> res, int rep)
	{
		
		try {
			//Runtime.getRuntime().exec("killall tcpdump");
			this.process.destroy(); 
			//Thread.sleep(5000); //give time to the buffer to dump
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String cmd = "capinfos " + dir + "/traffic.cap -T -d";
		//String cmd = "/home/fgrimaldo/wireshark/bin/capinfos /tmp/"+ rep + "ip"+ ip + "traffic.cap -T -d";
		
		//String salida = execCmd(cmd);
		//System.out.println(salida);
		try
		{
			java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream());
			String val = "";
			while(s.hasNext())
			{
				val = s.next();
			}
			double numba = (Integer.parseInt(val)/1024.0)/1024.0;
			res.set(numba); //the number of bytes is the last element
		}
		catch(Exception e)
		{
			e.printStackTrace();
			res.set(0.0);
		}
		
		
		
	}
	
	
}

