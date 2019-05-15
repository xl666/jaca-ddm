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
package mas2jgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main 

{

	public Main() 
	{
		
	}
	
	public static void generateMas2j(String xfilePath, String outPath)
	{
		//extract all the necessary info
		
		  String serverIP;
		  HashMap<String, ArrayList<String>> mapClientes;
		  String protocolFile;
		  HashMap<String, String> mapParamValue;
		  HashMap<String, String> mapParamType;
		  HashMap<String,HashMap<String, Integer>> mapTypeClient;
		  String typeDataSource;
		  String typeEval;
		  String testData;
		  ArrayList<Object> evalParams;
		  String testDataBase;
		  String roundFilesBase;
		  int repetitions;
		  String dataSet;
		  String testPath;
		  String protocolName;
		 				
		File xfile = new File(xfilePath);
		Document doc = loadXML(xfile);
		
						
		
		//server
		NodeList nList = doc.getElementsByTagName("node0");					 
		Node nNode = nList.item(0);	 				 						
		Element eElement = (Element) nNode;
		serverIP = eElement.getElementsByTagName("ip").item(0).getTextContent(); //server ip							
			
		//clients
		mapClientes = new HashMap<String, ArrayList<String>>();
		Element clients  = (Element) doc.getElementsByTagName("nodes").item(0);
		NodeList nClients = clients.getElementsByTagName("node");
		for(int i = 0;  i < nClients.getLength(); i++)
		{
			Element eClient = (Element) nClients.item(i);
			ArrayList<String> internos = new ArrayList<String>();
														
			internos.add(eClient.getElementsByTagName("ip").item(0).getTextContent());
			
			
			if(eClient.getElementsByTagName("user").getLength() > 0)
			{
				internos.add(eClient.getElementsByTagName("user").item(0).getTextContent());
			}
			else
				internos.add("");
			if(eClient.getElementsByTagName("pass").getLength() > 0)
			{
				internos.add(eClient.getElementsByTagName("pass").item(0).getTextContent());
			}
			else
				internos.add("");
			
			internos.add(eClient.getElementsByTagName("file").item(0).getTextContent());
			
			mapClientes.put(eClient.getElementsByTagName("name").item(0).getTextContent(), internos);			
		}
		
		
		//strategy
		Element eProtocol = (Element)doc.getElementsByTagName("strategy").item(0);					 		
		protocolFile = eProtocol.getElementsByTagName("file").item(0).getTextContent(); //protocol file
		mapParamValue = new HashMap<String, String>();
		mapParamType = new HashMap<String, String>();
		//it is possible that threre are no params
		if(eProtocol.getElementsByTagName("params").getLength() > 0)
		{
		Element epParams = (Element)eProtocol.getElementsByTagName("params").item(0);
		NodeList ppParams = epParams.getElementsByTagName("param");
		
		for(int i = 0; i < ppParams.getLength(); i++)
		{
			Element pppParam = (Element) ppParams.item(i);
			String key = pppParam.getElementsByTagName("name").item(0).getTextContent();
			String val = pppParam.getElementsByTagName("value").item(0).getTextContent();
			String type = pppParam.getElementsByTagName("type").item(0).getTextContent();
			mapParamValue.put(key, val);
			mapParamType.put(key, type);
		}
		}
		
		
		//distribution
		mapTypeClient = new HashMap<String, HashMap<String,Integer>>();
		Element eDist = (Element)doc.getElementsByTagName("distribution").item(0);
		NodeList dAgents = eDist.getElementsByTagName("agents");
		for(int i = 0; i < dAgents.getLength(); i++)
		{
			Element eags = (Element) dAgents.item(i);
			HashMap<String, Integer> interno = null;			
			String key = eags.getElementsByTagName("program").item(0).getTextContent();
			String client = eags.getElementsByTagName("node").item(0).getTextContent();
			int number = Integer.parseInt(eags.getElementsByTagName("number").item(0).getTextContent());
			
			if(mapTypeClient.containsKey(key))
				interno = mapTypeClient.get(key);
			else
				interno = new HashMap<String, Integer>();
			
			interno.put(client, number);
			mapTypeClient.put(key, interno);
		}
		

		//evaluation
		
		testData = ""; //testData
		
		testDataBase = ""; //testDataBase
		roundFilesBase = ""; // roundFilesBase
		repetitions = 1; //repetitions
		dataSet = ""; //dataset
		testPath = ""; //testPath

		
		Element eEval = (Element)doc.getElementsByTagName("evaluation").item(0);
		typeDataSource = eEval.getElementsByTagName("typeDataSource").item(0).getTextContent();
		typeEval = eEval.getElementsByTagName("name").item(0).getTextContent();
		Element eeParams = (Element)eEval.getElementsByTagName("params").item(0);
		NodeList epParams = eeParams.getElementsByTagName("param");
		evalParams = new ArrayList<Object>(); //evalParams
		evalParams.add(typeEval);
		
		
		for(int i = 0; i < epParams.getLength(); i++)
		{
			Element pppParam = (Element) epParams.item(i);
			String name = pppParam.getElementsByTagName("name").item(0).getTextContent();
			String val = pppParam.getElementsByTagName("value").item(0).getTextContent();
			String type = pppParam.getElementsByTagName("type").item(0).getTextContent();
			
			if(name.equals("testData"))
				testData = val;
			else if(name.equals("testDataBase"))
				testDataBase = val;
			else if(name.equals("roundFilesBase"))
				roundFilesBase = val;
			else if(name.equals("repetitions"))
				repetitions = Integer.parseInt(val);
			else if(name.equals("dataSet"))
				dataSet = val;
			else if(name.equals("testPath"))
				testPath = val;
			else if(name.equals("folds"))
				evalParams.add(Integer.parseInt(val));  
			else if(name.equals("trainPer"))
				evalParams.add(Double.parseDouble(val));
		}
		
		
		typeDataSource = typeDataSource;
		typeEval = typeEval;
		
		
		//begin the generation of the mas2j file
		
		String base = "MAS experimenter { \n"
			+ "   infrastructure: Centralised\n"
			+ "   environment: c4jason.CartagoEnvironment(\"infrastructure\")\n"
			+ "   agents:\n"
			+ "      experimenter [beliefs= \"mode(experiment), clients(%s), paramsProto(%s), agentsDist(%s), typeDataSource(%s), roundFilesBase(%s), evalParams(%s), testData(%s), dataSet(%s), testDataBase(%s), typesAgents(%s), numAgents(%s), repetitions(%s), testPath(%s), serverIP(%s), protocolName(%s)\"] agentArchClass c4jason.CAgentArch;\n"
			+ "   aslSourcePath:\n"
			+ "      \"src/asl\";\n"
		    + "}\n";
		
		//clients
		ArrayList<Object[]> oClients = new ArrayList<Object[]>(); 
    	for(String key : mapClientes.keySet())
    	{
    		
    		ArrayList<String> params = (ArrayList<String>) mapClientes.get(key).clone();
    		params.add(0, key);
    		oClients.add(params.toArray());
    	}
    	String bCLients = mat2String(oClients);
    	
    	//paramsProto
    	ArrayList<Object[]> paramsP = new ArrayList<Object[]>();
    	for(String key : mapParamValue.keySet())
    	{
    		
    		String value = mapParamValue.get(key);
    		if(mapParamType.get(key).equals("int"))
    		{
    			paramsP.add(new Object[]{key, Integer.parseInt(value)});        			
    		}
    		else if(mapParamType.get(key).equals("boolean"))
    		{
    			paramsP.add(new Object[]{key, Boolean.parseBoolean(value)});         			

    		}
    		else if (mapParamType.get(key).equals("double"))
    			paramsP.add(new Object[]{key, Double.parseDouble(value)});
    		
    		else
    			paramsP.add(new Object[]{key, value});
    			
    	}
    	String bParamsProto = mat2String(paramsP);
    	
    	//agentsDist
    	ArrayList<Object[]> combP = new ArrayList<Object[]>();
    	for(String key : mapTypeClient.keySet())
    	{
    		for(String k2 : mapTypeClient.get(key).keySet())
    		{
    			combP.add(new Object[]{key, k2, mapTypeClient.get(key).get(k2)});
    		}
    			
    	}
    	String bAgentsDist = mat2String(combP);
    	
    	//evalParams
    	String bEvalParams = array2String(evalParams);
    	
		//typeAgents
    	File faux = new File(protocolFile);
    	Document dProtocol = loadXML(faux);
    	HashMap<String, String> mapTypesAgents = extractTypesProtocol(dProtocol);
    	protocolName =extractProtocolName(dProtocol); //extract the name of the protocol
    	ArrayList<Object[]> typesAg = new ArrayList<Object[]>();
    	for(String key : mapTypesAgents.keySet())
    	{    		    		
    		typesAg.add(new Object[]{key, mapTypesAgents.get(key)});	
    	}
    	String bTypesAgents = mat2String(typesAg);
		
    	//numAgents
    	int nA = 0;
		for(String k1 : mapTypeClient.keySet())
		{
			for(String k2 : mapTypeClient.get(k1).keySet())
			{
				nA += mapTypeClient.get(k1).get(k2);
			}
		}
    	
    	
		String res = String.format(base, bCLients, bParamsProto, bAgentsDist, "\\\"" + typeDataSource + "\\\"", "\\\"" + roundFilesBase + "\\\"", bEvalParams, "\\\"" + testData + "\\\"", "\\\"" + dataSet + "\\\"", "\\\"" + testDataBase + "\\\"", bTypesAgents, nA, repetitions, "\\\"" + testPath + "\\\"", "\\\"" + serverIP + "\\\"", "\\\"" + protocolName + "\\\"");		
		File newTextFile = new File(outPath);
		FileWriter fileWriter;
		try 
		{
			fileWriter = new FileWriter(newTextFile);
			fileWriter.write(res);
	        fileWriter.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
        
		
	}
	
	public static Document loadXML(File fXmlFile)
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			return doc;
		}
		catch(Exception e)
		{
			return null; //invalid xml
		}		
	}
	
	
	
	public static String mat2String(ArrayList<Object[]> mat)
	{
		String res = "[%s]";
		String interno = "";
		for(Object[] ao : mat)
		{
			String sub = "[%s]";
			String aux = "";
			for(Object oo : ao)
			{
				if(oo instanceof String)
					aux += "\\\"" + oo.toString() + "\\\",";
				else
					aux += oo.toString() + ",";
			}
			aux = aux.substring(0, aux.length()-1);
			sub = String.format(sub, aux);
			interno += sub + ",";
		}
		if(interno.length() > 0)
			interno = interno.substring(0, interno.length()-1);
		res = String.format(res, interno);
		
		return res;
	}
	
	public static String array2String(ArrayList<Object> mat)
	{
		String res = "[%s]";
		String interno = "";
		for(Object ao : mat)
		{						
			if(ao instanceof String)
				interno += "\\\"" + ao.toString() + "\\\",";
			else
				interno += ao.toString() + ",";
		}
		interno = interno.substring(0, interno.length()-1);
		res = String.format(res, interno);
		
		return res;
	}
	
	
	 private static  HashMap<String, String> extractTypesProtocol(Document doc)
		{
			HashMap<String, String> res = new HashMap<String, String>();
			
			NodeList nList = doc.getElementsByTagName("agent");
			
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
		 
				Node nNode = nList.item(temp);	 				 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;
					String key = eElement.getElementsByTagName("program").item(0).getTextContent();				
					String val = eElement.getElementsByTagName("file").item(0).getTextContent();
					res.put(key, val);
				}
			}
			
			return res;
		}
	 
	 private  static String extractProtocolName (Document doc)
		{
			String res = "";
			
			NodeList nList = doc.getElementsByTagName("config");
			
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
		 
				Node nNode = nList.item(temp);	 				 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;
					res = eElement.getElementsByTagName("name").item(0).getTextContent();								
				}
			}
			
			return res;
		}
	 
	
	public static void main(String[] args)
	{
		Object f = " $\\pm$ ";
		StringBuilder sout = new StringBuilder();
        for (int i = 0; i < 1; i++) {
            sout.append(f.toString());
        }	
		System.out.println(sout);
		generateMas2j(args[0], args[1]);
	}
	
}
