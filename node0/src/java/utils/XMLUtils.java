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
package utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;



import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import weka.core.pmml.jaxbbindings.MapValues;



public class XMLUtils {

	public XMLUtils() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static boolean validateAgainstXSD(File xmlF, File xsdF)
	{
		InputStream xml = null;
		InputStream xsd = null;
		try
		{
			xml = new FileInputStream(xmlF);
			xsd = new FileInputStream(xsdF);
		}
		catch(Exception e){}
		
	    try
	    {
	        SchemaFactory factory = 
	            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	        Schema schema = factory.newSchema(new StreamSource(xsd));
	        Validator validator = schema.newValidator();
	        validator.validate(new StreamSource(xml));
	        return true;
	    }
	    catch(Exception ex)
	    {
	        return false;
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
	
	/*
	 * creates an xml file from configuration objects
	 */
	public static void createConfigXML(String serverIP, HashMap<String, ArrayList<String>> mapClientes, String protocolFile, HashMap<String, String> mapParamValue, HashMap<String, String> mapParamType, HashMap<String,HashMap<String, Integer>> mapTypeClient, String typeDataSource, String typeEval, String testData, ArrayList<Object> evalParams, String testDataBase, String roundFilesBase, int repetitions, String dataSet, String testPath, String filename) throws Exception
	{
		
		
			 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("config");
			doc.appendChild(rootElement);
			Attr attrRE1 = doc.createAttribute("xmlns");
			attrRE1.setValue("http://uv.mx/hlimon");
			rootElement.setAttributeNode(attrRE1);
			
			Attr attrRE2 = doc.createAttribute("xmlns:xsi");
			attrRE2.setValue("http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttributeNode(attrRE2);
			
			Attr attrRE3 = doc.createAttribute("xsi:schemaLocation");
			attrRE3.setValue("http://uv.mx/hlimon sys.xsd");
			rootElement.setAttributeNode(attrRE3);
	 
			// server elements
			Element server = doc.createElement("node0");
			rootElement.appendChild(server);	 				 						
			Element eServerIP = doc.createElement("ip");
			eServerIP.appendChild(doc.createTextNode(serverIP));
			server.appendChild(eServerIP);
	 
			//clients elements
			Element clients = doc.createElement("nodes");
			rootElement.appendChild(clients);
			for(String key : mapClientes.keySet())
			{
				Element eClient = doc.createElement("node");
				
				Element eName = doc.createElement("name");
				eName.appendChild(doc.createTextNode(key));
				eClient.appendChild(eName);
				
				Element eIP = doc.createElement("ip");
				eIP.appendChild(doc.createTextNode(mapClientes.get(key).get(0)));
				eClient.appendChild(eIP);
				
				if(!mapClientes.get(key).get(1).trim().equals(""))
				{
					Element eUser = doc.createElement("user");
					eUser.appendChild(doc.createTextNode(mapClientes.get(key).get(1)));
					eClient.appendChild(eUser);
				}
				
				if(!mapClientes.get(key).get(2).trim().equals(""))
				{
					Element ePass = doc.createElement("pass");
					ePass.appendChild(doc.createTextNode(mapClientes.get(key).get(2)));
					eClient.appendChild(ePass);
				}
				
				Element eFile = doc.createElement("file");
				eFile.appendChild(doc.createTextNode(mapClientes.get(key).get(3)));
				eClient.appendChild(eFile);
				
				clients.appendChild(eClient);
			}
			
			//protocol
			Element protocol = doc.createElement("strategy");
			rootElement.appendChild(protocol);
			Element epFile = doc.createElement("file");
			epFile.appendChild(doc.createTextNode(protocolFile));
			protocol.appendChild(epFile);
			
			if(mapParamType.size() > 0)
			{
				Element epParams = doc.createElement("params");
				protocol.appendChild(epParams);
				for(String key : mapParamValue.keySet())
				{
					Element eppParam = doc.createElement("param");
					
					Element eppName = doc.createElement("name");
					eppName.appendChild(doc.createTextNode(key));
					eppParam.appendChild(eppName);
					
					Element eppValue = doc.createElement("value");
					eppValue.appendChild(doc.createTextNode(mapParamValue.get(key)));
					eppParam.appendChild(eppValue);
					
					Element eppType = doc.createElement("type");
					eppType.appendChild(doc.createTextNode(mapParamType.get(key)));
					eppParam.appendChild(eppType);
					
					epParams.appendChild(eppParam);
				}
			}
			
			//agent distribution
			Element distribution = doc.createElement("distribution");
			rootElement.appendChild(distribution);
			for(String key : mapTypeClient.keySet())
			{
				for(String k2 : mapTypeClient.get(key).keySet())
				{
					Element eagents = doc.createElement("agents");
					distribution.appendChild(eagents);
					
					
					
					Element etype = doc.createElement("program");
					etype.appendChild(doc.createTextNode(key));
					eagents.appendChild(etype);
					
					Element elocation = doc.createElement("node");
					elocation.appendChild(doc.createTextNode(k2));
					eagents.appendChild(elocation);
					
					Element enumber = doc.createElement("number");
					enumber.appendChild(doc.createTextNode(mapTypeClient.get(key).get(k2).toString()));
					eagents.appendChild(enumber);
				}								
			}
			
			//eval configuration
			Element eval = doc.createElement("evaluation");
			rootElement.appendChild(eval);
			
			Element ename = doc.createElement("name");
			ename.appendChild(doc.createTextNode(typeEval));
			eval.appendChild(ename);
			
			Element etdatasource = doc.createElement("typeDataSource");
			etdatasource.appendChild(doc.createTextNode(typeDataSource));
			eval.appendChild(etdatasource);
			
			Element epParams = doc.createElement("params");
			eval.appendChild(epParams);
			
			if(typeEval.equals("Single file") && evalParams.size() == 0) 
			{
				Element eppParam = doc.createElement("param");
				
				Element eppName = doc.createElement("name");
				eppName.appendChild(doc.createTextNode("testData"));
				eppParam.appendChild(eppName);
				
				Element eppValue = doc.createElement("value");
				eppValue.appendChild(doc.createTextNode(testData));
				eppParam.appendChild(eppValue);
				
				Element eppType = doc.createElement("type");
				eppType.appendChild(doc.createTextNode("string"));
				eppParam.appendChild(eppType);						
				
				epParams.appendChild(eppParam);
			}
			
			else if (typeEval.equals("Round files") && evalParams.size() == 0) 
			{
				Element eppParam = doc.createElement("param");
				
				Element eppName = doc.createElement("name");
				eppName.appendChild(doc.createTextNode("testDataBase"));
				eppParam.appendChild(eppName);
				
				Element eppValue = doc.createElement("value");
				eppValue.appendChild(doc.createTextNode(testDataBase));
				eppParam.appendChild(eppValue);
				
				Element eppType = doc.createElement("type");
				eppType.appendChild(doc.createTextNode("string"));
				eppParam.appendChild(eppType);	
				
				Element epp2Param = doc.createElement("param");
				
				Element epp2Name = doc.createElement("name");
				epp2Name.appendChild(doc.createTextNode("roundFilesBase"));
				epp2Param.appendChild(epp2Name);
				
				Element epp2Value = doc.createElement("value");
				epp2Value.appendChild(doc.createTextNode(roundFilesBase));
				epp2Param.appendChild(epp2Value);
				
				Element epp2Type = doc.createElement("type");
				epp2Type.appendChild(doc.createTextNode("string"));
				epp2Param.appendChild(epp2Type);	
				
				Element epp3Param = doc.createElement("param");
				
				Element epp3Name = doc.createElement("name");
				epp3Name.appendChild(doc.createTextNode("repetitions"));
				epp3Param.appendChild(epp3Name);
				
				Element epp3Value = doc.createElement("value");
				epp3Value.appendChild(doc.createTextNode(new Integer(repetitions).toString()));
				epp3Param.appendChild(epp3Value);
				
				Element epp3Type = doc.createElement("type");
				epp3Type.appendChild(doc.createTextNode("int"));
				epp3Param.appendChild(epp3Type);
				
				epParams.appendChild(eppParam);
				epParams.appendChild(epp2Param);
				epParams.appendChild(epp3Param);
			}
			
			else if (typeEval.equals("Hold-Out") && evalParams.size() != 0)
			{
				Element eppParam = doc.createElement("param");				
				Element eppName = doc.createElement("name");
				eppName.appendChild(doc.createTextNode("dataSet"));
				eppParam.appendChild(eppName);				
				Element eppValue = doc.createElement("value");
				eppValue.appendChild(doc.createTextNode(dataSet));
				eppParam.appendChild(eppValue);				
				Element eppType = doc.createElement("type");
				eppType.appendChild(doc.createTextNode("string"));
				eppParam.appendChild(eppType);	
				epParams.appendChild(eppParam);
				
				Element epp2Param = doc.createElement("param");				
				Element epp2Name = doc.createElement("name");
				epp2Name.appendChild(doc.createTextNode("trainPer"));
				epp2Param.appendChild(epp2Name);				
				Element epp2Value = doc.createElement("value");
				epp2Value.appendChild(doc.createTextNode(evalParams.get(1).toString()));
				epp2Param.appendChild(epp2Value);				
				Element epp2Type = doc.createElement("type");
				epp2Type.appendChild(doc.createTextNode("double"));
				epp2Param.appendChild(epp2Type);
				epParams.appendChild(epp2Param);
				
				Element epp3Param = doc.createElement("param");				
				Element epp3Name = doc.createElement("name");
				epp3Name.appendChild(doc.createTextNode("repetitions"));
				epp3Param.appendChild(epp3Name);				
				Element epp3Value = doc.createElement("value");
				epp3Value.appendChild(doc.createTextNode(new Integer(repetitions).toString()));
				epp3Param.appendChild(epp3Value);			
				Element epp3Type = doc.createElement("type");
				epp3Type.appendChild(doc.createTextNode("int"));
				epp3Param.appendChild(epp3Type);
				epParams.appendChild(epp3Param);
				
				Element epp4Param = doc.createElement("param");				
				Element epp4Name = doc.createElement("name");
				epp4Name.appendChild(doc.createTextNode("testPath"));
				epp4Param.appendChild(epp4Name);				
				Element epp4Value = doc.createElement("value");
				epp4Value.appendChild(doc.createTextNode(testPath));
				epp4Param.appendChild(epp4Value);			
				Element epp4Type = doc.createElement("type");
				epp4Type.appendChild(doc.createTextNode("string"));
				epp4Param.appendChild(epp4Type);
				epParams.appendChild(epp4Param);
			}
			
			else if (typeEval.equals("Cross-Validation") && evalParams.size() != 0)
			{
				Element eppParam = doc.createElement("param");				
				Element eppName = doc.createElement("name");
				eppName.appendChild(doc.createTextNode("dataSet"));
				eppParam.appendChild(eppName);				
				Element eppValue = doc.createElement("value");
				eppValue.appendChild(doc.createTextNode(dataSet));
				eppParam.appendChild(eppValue);				
				Element eppType = doc.createElement("type");
				eppType.appendChild(doc.createTextNode("string"));
				eppParam.appendChild(eppType);	
				epParams.appendChild(eppParam);
				
				Element epp2Param = doc.createElement("param");				
				Element epp2Name = doc.createElement("name");
				epp2Name.appendChild(doc.createTextNode("folds"));
				epp2Param.appendChild(epp2Name);				
				Element epp2Value = doc.createElement("value");
				epp2Value.appendChild(doc.createTextNode(evalParams.get(1).toString()));
				epp2Param.appendChild(epp2Value);				
				Element epp2Type = doc.createElement("type");
				epp2Type.appendChild(doc.createTextNode("int"));
				epp2Param.appendChild(epp2Type);
				epParams.appendChild(epp2Param);
				
				Element epp3Param = doc.createElement("param");				
				Element epp3Name = doc.createElement("name");
				epp3Name.appendChild(doc.createTextNode("repetitions"));
				epp3Param.appendChild(epp3Name);				
				Element epp3Value = doc.createElement("value");
				epp3Value.appendChild(doc.createTextNode(new Integer(repetitions).toString()));
				epp3Param.appendChild(epp3Value);			
				Element epp3Type = doc.createElement("type");
				epp3Type.appendChild(doc.createTextNode("int"));
				epp3Param.appendChild(epp3Type);
				epParams.appendChild(epp3Param);
				
				Element epp4Param = doc.createElement("param");				
				Element epp4Name = doc.createElement("name");
				epp4Name.appendChild(doc.createTextNode("testPath"));
				epp4Param.appendChild(epp4Name);				
				Element epp4Value = doc.createElement("value");
				epp4Value.appendChild(doc.createTextNode(testPath));
				epp4Param.appendChild(epp4Value);			
				Element epp4Type = doc.createElement("type");
				epp4Type.appendChild(doc.createTextNode("string"));
				epp4Param.appendChild(epp4Type);
				epParams.appendChild(epp4Param);
			}
			
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			//System.out.println("File saved!");
	 
		  
			
	}
	
	public static Object[] loadConfigXML(File xfile)
	{
		Object[] res = new Object[15];
		/*
		 * String serverIP
		 * HashMap<String, ArrayList<String>> mapClientes
		 * String protocolFile
		 * HashMap<String, String> mapParamValue
		 * HashMap<String, String> mapParamType
		 * HashMap<String,HashMap<String, Integer>> mapTypeClient
		 * String typeDataSource
		 * String typeEval
		 * String testData
		 * ArrayList<Object> evalParams
		 * String testDataBase
		 * String roundFilesBase
		 * int repetitions
		 * String dataSet
		 * String testPath
		 */
		
		
		
		Document doc = loadXML(xfile);
		
		//server
		NodeList nList = doc.getElementsByTagName("node0");					 
		Node nNode = nList.item(0);	 				 						
		Element eElement = (Element) nNode;
		res[0] = eElement.getElementsByTagName("ip").item(0).getTextContent(); //server ip							
			
		//clients
		HashMap<String, ArrayList<String>> mapClientes = new HashMap<String, ArrayList<String>>();
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
		res[1] = mapClientes;
		
		//protocol
		Element eProtocol = (Element)doc.getElementsByTagName("strategy").item(0);					 		
		res[2] = eProtocol.getElementsByTagName("file").item(0).getTextContent(); //protocol file
		HashMap<String, String> mapParamValue = new HashMap<String, String>();
		HashMap<String, String> mapParamType = new HashMap<String, String>();
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
		res[3] = mapParamValue;
		res[4] = mapParamType;
		
		
		//distribution
		HashMap<String,HashMap<String, Integer>> mapTypeClient = new HashMap<String, HashMap<String,Integer>>();
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
		res[5] = mapTypeClient;

		//evaluation
		
		res[8] = ""; //testData
		
		res[10] = ""; //testDataBase
		res[11] = ""; // roundFilesBase
		res[12] = 1; //repetitions
		res[13] = ""; //dataset
		res[14] = ""; //testPath

		
		Element eEval = (Element)doc.getElementsByTagName("evaluation").item(0);
		String typeDataSource = eEval.getElementsByTagName("typeDataSource").item(0).getTextContent();
		String typeEval = eEval.getElementsByTagName("name").item(0).getTextContent();
		Element eeParams = (Element)eEval.getElementsByTagName("params").item(0);
		NodeList epParams = eeParams.getElementsByTagName("param");
		ArrayList<Object> evalParams = new ArrayList<Object>(); //evalParams
		evalParams.add(typeEval);
		
		
		for(int i = 0; i < epParams.getLength(); i++)
		{
			Element pppParam = (Element) epParams.item(i);
			String name = pppParam.getElementsByTagName("name").item(0).getTextContent();
			String val = pppParam.getElementsByTagName("value").item(0).getTextContent();
			String type = pppParam.getElementsByTagName("type").item(0).getTextContent();
			
			if(name.equals("testData"))
				res[8] = val;
			else if(name.equals("testDataBase"))
				res[10] = val;
			else if(name.equals("roundFilesBase"))
				res[11] = val;
			else if(name.equals("repetitions"))
				res[12] = Integer.parseInt(val);
			else if(name.equals("dataSet"))
				res[13] = val;
			else if(name.equals("testPath"))
				res[14] = val;
			else if(name.equals("folds"))
				evalParams.add(Integer.parseInt(val));  
			else if(name.equals("trainPer"))
				evalParams.add(Double.parseDouble(val));
		}
		
		res[9] = evalParams;
		res[6] = typeDataSource;
		res[7] = typeEval;
		
		return res;
	}

}
