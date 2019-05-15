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

import java.util.HashMap;

import cartago.*;


public class Directory extends Artifact 
{
	private HashMap<String, String> mapClients = new HashMap<String, String>();
	private HashMap<String, String> mapAgentsClients = new HashMap<String, String>();
	private HashMap<String, ArtifactId> mapArtifactsId = new HashMap<String, ArtifactId>();
	private HashMap<String, Integer> agentsPerType = new HashMap<String, Integer>();
	private HashMap<String, String> typesFiles = new HashMap<String, String>();
	
	void init() 
	{
		
	}
	
	@OPERATION
	void registerTypeFile(String type, String file)
	{
		typesFiles.put(type, file);
	}
	
	@OPERATION
	void getTypeFile(String type, OpFeedbackParam<String> file)
	{
		if(!typesFiles.containsKey(type))
			file.set("");
		else
			file.set(typesFiles.get(type));
	}
	
	@OPERATION
	void incrementTypeCount(String type)
	{
		if(!agentsPerType.containsKey(type))
			agentsPerType.put(type, 1);
		else
		{
			int val = agentsPerType.get(type);
			agentsPerType.put(type, val+1);
		}
	}
	
	@OPERATION void getTypeCount(String type, OpFeedbackParam<Integer> count)
	{
		if(!agentsPerType.containsKey(type))
			count.set(0);
		else
		{
			int val = agentsPerType.get(type);
			count.set(val);
		}
	}
	
	@OPERATION
	void clearAgentasAndArtifacts()
	{
		this.mapAgentsClients = new HashMap<String, String>();
		this.mapArtifactsId = new HashMap<String, ArtifactId>();
		this.agentsPerType = new HashMap<String, Integer>();
	}
	
	@OPERATION
	void registerClient(String name, String ip)
	{
		mapClients.put(name, ip);
	} 
	
	@OPERATION
	void registerAgentClient(String agent, String client)
	{
			mapAgentsClients.put(agent, client);
	}
	
	@OPERATION
	void registerArtifactId(String artName, ArtifactId id)
	{		
		mapArtifactsId.put(artName, id);	
	}
	
	@OPERATION
	void getClientIp(String client, OpFeedbackParam<String> ip)
	{
		ip.set(mapClients.get(client));
	}
	
	@OPERATION
	void getAgentClient(String agent, OpFeedbackParam<String> client)
	{
		client.set(mapAgentsClients.get(agent));
	}
	@OPERATION
	void getArtifactId(String artName, OpFeedbackParam<ArtifactId> aid)
	{
		aid.set(mapArtifactsId.get(artName));			
	}
	
}

