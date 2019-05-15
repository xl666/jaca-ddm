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
// CArtAgO artifact code for project aprendizajePropV3

package artifacts;


import jason.stdlib.abolish;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.JobAttributes;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.PanelPeer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import cartago.*;
import cartago.tools.GUIArtifact;

public class GUI extends GUIArtifact 
{
	private final String xsdProtocolPath = "config/proto.xsd";
	private File xsdProtocol = new File(xsdProtocolPath);
	private final String xsdConfigPath = "config/sys.xsd";
	private File xsdConfig = new File(xsdConfigPath);
    private MyFrame frame;    
    HashMap<String, ArrayList<String>> mapClientes = new HashMap<String, ArrayList<String>>(); //to save all the relevant info of a client 
    HashMap<String, String> mapTypesAgents = new HashMap<String, String>();
    HashMap<String, String> mapParamValue = new HashMap<String, String>();
    HashMap<String, String> mapParamType = new HashMap<String, String>();
    HashMap<String,HashMap<String, Integer>> mapTypeClient = new HashMap<String, HashMap<String,Integer>>(); //double indexed, the integer is the count
    String typeDataSource = "Single file"; //by default
    String roundFilesBase = "";
    String typeEval = "Single file"; //by default
    ArrayList<Object> evalParams = new ArrayList<Object>(); //to save all the params for the evaluation, the order is important for the given plan
    String testData = ""; //path to test data
    String dataSet = ""; //to know the dataset if the oracle is being used
    String testDataBase = ""; //base name for round file mode
    String protocolName = "";
    boolean evalGPU = false;
    int repetitions = 1; //number of experiment repetitions
    String testPath = "";
    String serverIP = getLocalIP();
    String protocolFile = "";
    
    int numAgents = 0; //to have an easy access to the number of agents
    
    
    
    public void setup()
    {
        frame = new MyFrame();
        
        linkActionEventToOp(frame.bStart, "start");
        
        String[][] val = {{}};
        defineObsProperty("clients", val); 
        defineObsProperty("paramsProto", val); 
        defineObsProperty("agentsDist", val);
        defineObsProperty("typeDataSource", "");
        defineObsProperty("roundFilesBase", "");
        defineObsProperty("evalParams", "");
        defineObsProperty("testData", "");
        defineObsProperty("dataSet", "");
        defineObsProperty("testDataBase", "");
        defineObsProperty("typesAgents", val);        
        defineObsProperty("numAgents", 0);
        defineObsProperty("repetitions", 1);
        defineObsProperty("testPath", "");
        defineObsProperty("serverIP", getLocalIP());
        defineObsProperty("protocolName", protocolName);
        defineObsProperty("evalGPU", evalGPU);
        
        mapClientes = new HashMap<String, ArrayList<String>>();
                       
       
        
        frame.setVisible(true);
    }
   
    
    @INTERNAL_OPERATION
    private void start(ActionEvent ev)
    {
    	if(mapClientes.size() == 0)
    	{
    		JOptionPane.showMessageDialog(null, "You need at least one node");
    		return;
    	}
    	    	
    	
    	if(mapTypesAgents.size() == 0)
    	{
    		JOptionPane.showMessageDialog(null, "You need to load a strategy file");
    		return;
    	}
    	
    	if(mapTypeClient.size() == 0)
    	{
    		JOptionPane.showMessageDialog(null, "You need to configure the agent distribution");
    		return;
    	}
    	    	
    	
    	if(testData.equals("") &&  testDataBase.equals("") && dataSet.equals(""))
    	{
    		JOptionPane.showMessageDialog(null, "You need to specify a path for the test data");
    		return;
    	}
    	
    	
    	//String[][] a = {{"c1","localhost"},{"c2","localhost:8080"}};
    	//getObsProperty("clients").updateValue(a);
    	//defineObsProperty("client", "c2", "192.168.0.1");
    	//remove all the previous observable properties 
    	/*try
    	{
    		while(true) //it is possible to have defined more than one client property, but it only removes one at a time
    			removeObsProperty("client");
    	}
    	catch(Exception e){}*/
    	
    	//add the new observable properties
    	//first clients
    	
    	ArrayList<Object[]> clients = new ArrayList<Object[]>(); 
    	for(String key : mapClientes.keySet())
    	{
    		
    		ArrayList<String> params = (ArrayList<String>) mapClientes.get(key).clone();
    		params.add(0, key);
    		clients.add(params.toArray());
    	}
    	getObsProperty("clients").updateValue(clients.toArray());
    	
    	//remove previous protocol palinkActionEventToOp(frame.bStart, "start");rams
    /*	try
    	{
    		while(true) //it is possible to have defined more than one client property, but it only removes one at a time
    			removeObsProperty("param");
    	}
    	catch(Exception e){}*/
    	
    	/*for(String key : mapParamValue.keySet())
    	{
    		
    		String value = mapParamValue.get(key);
    		if(mapParamType.get(key).equals("int"))
    			defineObsProperty("param", key, Integer.parseInt(value));
    		else if(mapParamType.get(key).equals("boolean"))
    		{
    			defineObsProperty("param", key, Boolean.parseBoolean(value));

    		}
    		else if (mapParamType.get(key).equals("double"))
    			defineObsProperty("param", key, Double.parseDouble(value));
    		else
    			defineObsProperty("param", key, value );
    			
    	}*/
    	
    	//agent types and files
    	ArrayList<Object[]> typesAg = new ArrayList<Object[]>();
    	for(String key : mapTypesAgents.keySet())
    	{    		    		
    		typesAg.add(new Object[]{key, mapTypesAgents.get(key)});	
    	}
    	getObsProperty("typesAgents").updateValue(typesAg.toArray());
    	
    	
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
    	getObsProperty("paramsProto").updateValue(paramsP.toArray());
    	
    	
    	//agent distribution 
    	ArrayList<Object[]> combP = new ArrayList<Object[]>();
    	for(String key : mapTypeClient.keySet())
    	{
    		for(String k2 : mapTypeClient.get(key).keySet())
    		{
    			combP.add(new Object[]{key, k2, mapTypeClient.get(key).get(k2)});
    		}
    			
    	}
    	getObsProperty("agentsDist").updateValue(combP.toArray());
    	
    	//data source parameters
    	getObsProperty("typeDataSource").updateValue(typeDataSource);
    	getObsProperty("roundFilesBase").updateValue(roundFilesBase);
    	
    	//eval params
    	getObsProperty("evalParams").updateValue(evalParams.toArray());
    	getObsProperty("testData").updateValue(testData);
    	getObsProperty("dataSet").updateValue(dataSet);
    	getObsProperty("testDataBase").updateValue(testDataBase);
    	getObsProperty("repetitions").updateValue(repetitions);
    	getObsProperty("testPath").updateValue(testPath);
    	
    	//numAgents
    	getObsProperty("numAgents").updateValue(numAgents);
    	
    	getObsProperty("serverIP").updateValue(serverIP);
    	
    	getObsProperty("protocolName").updateValue(protocolName);
    	getObsProperty("evalGPU").updateValue(evalGPU);
    	
    	//signal the begining
    	signal("begin");
    	
    	//block start button
    	frame.bStart.setEnabled(false);
    	
    }
    
    
    @OPERATION
    void printResult(Object... args){   	    	
    	for (int i = 0; i < args.length; i++) 
    	{
    		frame.putText(args[i].toString());
    	}
    	frame.putText("\n");
    		
    }
    
    @OPERATION
    void activateStart()
    {
        frame.bStart.setEnabled(true);
    }
    
    /**
     * @author xl666
     *
     */
    class MyFrame extends JFrame 
    {
    	private final JPanel displayArea;
    	private JButton bClients;
    	private JButton bLoadProtocol;
    	private JTextField tfProtoFileName;
    	
    	private JButton bDataDist;
    	private JButton bAgentDist;
    	
    	private JButton bStart = new JButton("Start");;
    	
    	JTextArea taResults = null; //to have a reference to the results
    	
    	JPanel panConfig =  panelConfig();
        JPanel panServer =  panelConfigServer();
        JPanel panClientes = panelConfigClientes();
        JPanel panProtocolo = panelConfigProtocolo();         
        JPanel panAgentsDist = panelAgentDist();
        JPanel panEval = panelEvaluation();
        JPanel panDispatch = panelDispatch();
    	
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Configuration");
        DefaultMutableTreeNode serverConfNode = new DefaultMutableTreeNode("Node0 Configuration");
        DefaultMutableTreeNode clientsConfNode = new DefaultMutableTreeNode("Nodes Configuration");
        DefaultMutableTreeNode protocolConfNode = new DefaultMutableTreeNode("Strategy Configuration");
        DefaultMutableTreeNode agentsDistConfNode = new DefaultMutableTreeNode("Agents Distribution");
        DefaultMutableTreeNode evalConfNode = new DefaultMutableTreeNode("Evaluation Configuration");
        DefaultMutableTreeNode dispatchConfNode = new DefaultMutableTreeNode("Dispatch Experiment");
        
        final HashMap<DefaultMutableTreeNode,JPanel> mapItemPanel = new HashMap<DefaultMutableTreeNode, JPanel>();
        
        public MyFrame() 
        {        	
            setTitle("Propositional Learning");
            setSize(900, 700); 
                                                
            
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(null);
            renderer.setClosedIcon(new ImageIcon("resources/rightArrow.jpg"));
            renderer.setOpenIcon(new ImageIcon("resources/downArrow.jpg"));
            
            JPanel controls = new JPanel();            
            displayArea = new JPanel();
            displayArea.setLayout(new BorderLayout());
            
            //JScrollPane scrolled = new JScrollPane(displayArea);
            controls.setBackground(Color.WHITE);
                        
            
          //create the root node
            
            mapItemPanel.put(root, panConfig);
            displayArea.add(panConfig);
            //create the child nodes
            
            recreatePanels();
            
            
            //add the child nodes to the root node
            root.add(serverConfNode);
            root.add(clientsConfNode);
            root.add(protocolConfNode);           
            root.add(agentsDistConfNode);         
            root.add(evalConfNode);
            root.add(dispatchConfNode);
             
            //create the tree by passing in the root node
            final JTree tree = new JTree(root);
            tree.setCellRenderer(renderer);
            final Font currentFont = tree.getFont();
            final Font bigFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + 3);
            tree.setFont(bigFont);
            tree.setRowHeight(50);
            
            tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
                
                public void valueChanged(TreeSelectionEvent e) {
                	
                	//displayArea.add(panClientes);
                	displayArea.removeAll();
                	try
                	{
                		displayArea.add(mapItemPanel.get((DefaultMutableTreeNode) tree.getLastSelectedPathComponent()));
                	}
                	catch(Exception ex){}
                	
                	displayArea.repaint();
                	//DefaultMutableTreeNode aux =(DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                	//JOptionPane.showMessageDialog(null,aux);
                    // selectedLabel.setText(e.getPath().toString());
                }
            });
            
            controls.add(tree);
            
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controls, displayArea);
            splitPane.setDividerLocation(200);
           
            this.getContentPane().add(splitPane);
                        
        }
          
        private void recreatePanels()
        {
        	panConfig =  panelConfig();
            panServer =  panelConfigServer();
            panClientes = panelConfigClientes();
            panProtocolo = panelConfigProtocolo();         
            panAgentsDist = panelAgentDist();
            panEval = panelEvaluation();
            panDispatch = panelDispatch();
            
            mapItemPanel.put(serverConfNode, panServer);
            mapItemPanel.put(clientsConfNode, panClientes);
            mapItemPanel.put(protocolConfNode, panProtocolo);
            mapItemPanel.put(agentsDistConfNode, panAgentsDist);
            mapItemPanel.put(evalConfNode, panEval);
            mapItemPanel.put(dispatchConfNode, panDispatch);
            
        }
        
        private File openFile()
        {
            final JFileChooser fc = new JFileChooser();
            
            fc.setCurrentDirectory(new File(".."));
            fc.showOpenDialog(this);
            File res = fc.getSelectedFile();
            return res;
        }

        private JPanel panelConfig()
        {
        	JPanel res = new JPanel();
        	
        	res.setLayout(new GridLayout(3,3));
        	
        	JPanel pan = new JPanel();
        	pan.setLayout(new GridLayout(9,1));
        	final JTextField tfLoad = new JTextField();        	
        	tfLoad.setEditable(false);
        	
        	final JTextField tfSave = new JTextField();
        	
        	
        	JButton bLoad = new JButton("Load"); 
        	bLoad.addActionListener(new ActionListener()
        	{				
				public void actionPerformed(ActionEvent arg0) 
				{
					File ar = openFile();
					if(ar != null)
					{
						if(!utils.XMLUtils.validateAgainstXSD(ar, xsdConfig))
                        {
                        	JOptionPane.showMessageDialog(null, "Your xml file doesn't compply to the schema");
                        	return;
                        }
						
						
						Object[] allParams =  new Object[15]; 
						allParams = utils.XMLUtils.loadConfigXML(ar);
						
						//server
						serverIP = (String) allParams[0];
						
						
						//clients
						mapClientes = (HashMap<String, ArrayList<String>>)allParams[1];						
						
						//protocol
						protocolFile = ((String) allParams[2]).trim();
						mapParamValue = (HashMap<String, String>) allParams[3];
						mapParamType =	(HashMap<String, String>) allParams[4];
						
						//distribution
						mapTypeClient = (HashMap<String,HashMap<String, Integer>>) allParams[5];
						
						//evaluation
						typeDataSource = (String)allParams[6];
						typeEval = (String)allParams[7];
						testData = (String)allParams[8];
						evalParams = (ArrayList<Object>)allParams[9];
						testDataBase = (String)allParams[10];
						roundFilesBase = (String)allParams[11];
						repetitions = (Integer)allParams[12];
						dataSet = (String)allParams[13];
						testPath = (String)allParams[14];
						
						recreatePanels(); //load the new params in the GUI
						try {
							tfLoad.setText(ar.getCanonicalPath());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						JOptionPane.showMessageDialog(null, "Loading success");
					}
					
				}
			});
        	
        	JButton bSave = new JButton("Save");
        	bSave.addActionListener(new ActionListener() 
        	{
				
				public void actionPerformed(ActionEvent arg0) {
					if(tfSave.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The save path can't be empty");
						return;
					}
					try
					{
						utils.XMLUtils.createConfigXML(serverIP, mapClientes, protocolFile, mapParamValue, mapParamType, mapTypeClient, typeDataSource, typeEval, testData, evalParams, testDataBase, roundFilesBase, repetitions, dataSet, testPath, tfSave.getText().trim());
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(null, "An error ocurred, the file wasn't saved");
						return;
					}
					JOptionPane.showMessageDialog(null, "File saved!");
				}
			});
        	
        	pan.add(new JLabel(""));
        	pan.add(new JLabel("Load config file"));
        	pan.add(tfLoad);
        	//pan.add(new JLabel(""));
        	pan.add(bLoad);
        	pan.add(new JLabel(""));
        	pan.add(new JLabel(""));
        	pan.add(new JLabel("Save current config to file"));
        	pan.add(tfSave);
        	pan.add(bSave);
        	
        	
        	
        	
        	
        	res.add(new JLabel(""));
        	res.add(pan);        	
        	res.add(new JLabel(""));
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	
        	
        	
        	return res;
        }
        
        
        private JPanel panelConfigServer()
        {
        	JPanel res = new JPanel();
        	res.setLayout(new GridLayout(7,3));
        	
        	JPanel pan = new JPanel();
        	pan.setLayout(new GridLayout(4,1));
        	final JTextField tfIP = new JTextField();
        	tfIP.setText(serverIP);
        	
        	
        	JButton bSave = new JButton("Save");
        	bSave.addActionListener(new ActionListener() 
        	{
				
				public void actionPerformed(ActionEvent arg0) {
					if(tfIP.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The Node0 IP can't be empty");
						return;
					}
					serverIP = tfIP.getText().trim(); 
				}
			});
        	
        	pan.add(new JLabel("Node0 IP:"));
        	pan.add(tfIP);
        	pan.add(new JLabel(""));
        	pan.add(bSave);
        	
        
        	
        	
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	
        	res.add(new JLabel(""));
        	res.add(pan);        	
        	res.add(new JLabel(""));
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	res.add(new JLabel(""));
        	
        	
        	return res;
        }
        
       
        private JPanel panelConfigClientes()
        {
        	final String NOT_SELECTABLE_OPTION = " - Select a Node - ";
        	
        	final JPanel res = new JPanel();
        	final JComboBox<String> comboBox = new JComboBox<String>();
        	
        	JPanel panDerecha = new JPanel();        	
        	final JPanel panIzquierda = new JPanel();
        	panelIzqClientes(panIzquierda, comboBox);
        	
        	//right panel
        	panDerecha.setLayout(new GridLayout(26, 2));  
        	

            comboBox.setModel(new DefaultComboBoxModel<String>() {
              private static final long serialVersionUID = 1L;
              boolean selectionAllowed = true;

              @Override
              public void setSelectedItem(Object anObject) {
                if (!NOT_SELECTABLE_OPTION.equals(anObject)) {
                  super.setSelectedItem(anObject);
                } else if (selectionAllowed) {
                  // Allow this just once
                  selectionAllowed = false;
                  super.setSelectedItem(anObject);
                }
              }
            });

            comboBox.addItem(NOT_SELECTABLE_OPTION);
            
            
            
            
            panDerecha.add(new JLabel(""));
            panDerecha.add(new JLabel(""));
            
            panDerecha.add(new JLabel("Node:"));
            panDerecha.add(comboBox);
            
            JButton bAdd = new JButton("New");
           
            bAdd.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					panelIzqClientes(panIzquierda, comboBox);
					displayArea.repaint();
					res.repaint();
					
				}
			});;
            
            
            JButton bDelete = new JButton("Delete");
            bDelete.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					mapClientes.remove(comboBox.getSelectedItem());
					DefaultComboBoxModel model = ( DefaultComboBoxModel) comboBox.getModel();	
					model.removeElementAt(comboBox.getSelectedIndex());
					model.setSelectedItem(model.getElementAt(0));					
					comboBox.repaint();
				}
			});
            
            JPanel panBut = new JPanel();
            panBut.add(bAdd);
            panBut.add(bDelete);
            panDerecha.add(new JLabel(""));
            panDerecha.add(new JLabel(""));
        	panDerecha.add(panBut);
        	
        	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panDerecha, panIzquierda);
            splitPane.setDividerLocation(200);
            //res.add(splitPane);
            res.setLayout(new BorderLayout());
            res.add(panDerecha, BorderLayout.WEST);
            res.add(panIzquierda, BorderLayout.CENTER);
            
            
            //load elements
            for(String key: mapClientes.keySet())
            {
            	comboBox.addItem(key);
            }
            
            
            
        	return res;
        }
        
        private void fillProtocolInfo(File ar, JTextArea taResume, JPanel panelParams)
        {
        	Document doc = utils.XMLUtils.loadXML(ar);
            if(doc == null)
            {
            	JOptionPane.showMessageDialog(null, "Invalid xml strategy file");
            	
            	return;
            }
            
            if(!utils.XMLUtils.validateAgainstXSD(ar, xsdProtocol))
            {
            	JOptionPane.showMessageDialog(null, "Your xml file for the strategy doesn't compply to the schema");
            	return;
            }
            
            
            //extract types and params
            mapTypesAgents = extractTypesProtocol(doc);
            if(!mapTypesAgents.containsKey("contactPerson"))
            {
            	mapTypesAgents = null;
            	JOptionPane.showMessageDialog(null, "You must define a contactPerson agent in your strategy");
            	return;
            }                      
            
            mapParamType = extractParamsProtocol(doc);            
            protocolName = extractProtocolName(doc);
            
            taResume.setText("Strategy: " + protocolName + "\n");
            taResume.setText(taResume.getText() + "Agent programs:\n");
            for(String type : mapTypesAgents.keySet())
            {
            	taResume.setText(taResume.getText()+ " *" + type + "\n");
            }
            
            //add components to the params panel
            addParams(panelParams);
        }
        
        private JPanel panelConfigProtocolo()
        {
        	final JPanel res2 = new JPanel();
        	res2.setLayout(new GridLayout(3,1));
        	
        	final JPanel panelParams = new JPanel();
        	JScrollPane scrolled = new JScrollPane(panelParams);
        	
        	final JPanel res = new JPanel();
        	res.setLayout(new GridLayout(10,3));
        	
        	final JTextArea taResume = new JTextArea();
        	final JTextField tfPath = new JTextField();
        	tfPath.setText(protocolFile);
        	
        	tfPath.setColumns(35);
        	//tfPath.setEditable(false);
        	JButton bLoad = new JButton("Load file");
        	bLoad.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					mapTypesAgents = new HashMap<String, String>();
					mapParamType = new HashMap<String, String>();
					mapParamValue = new HashMap<String, String>();
					addParams(panelParams);
					panelParams.validate();
					panelParams.repaint();
					taResume.setText("");
					File ar = openFile();
                    if(ar != null)
                    {
                    	protocolFile = ar.getPath();                    	
                        tfPath.setText(ar.getAbsolutePath());
                        fillProtocolInfo(ar, taResume, panelParams);                                                                        
                        
                        
                    }
                    else
                    	protocolFile = tfPath.getText().trim();
					
				}
			});
        	JPanel panAux = new JPanel();
        	panAux.setLayout(new GridLayout(0,4));
        	
        	panAux.add(new JLabel(""));        	
        	panAux.add(tfPath);
        	panAux.add(bLoad);
        	
        	res.add(new JLabel(""));
        	res.add(new JLabel("                                          Load XML strategy configuration file:"));
        	res.add(new JLabel(""));
        	res.add(panAux);
        	res.add(new JLabel(""));        	
        	
        	
        	taResume.setEditable(false);
        	//taResume.setRows(10);
        	JPanel panRes = new JPanel();
        	panRes.setLayout(new GridLayout(0,3));
        	panRes.add(new JLabel(""));
        	panRes.add(new JLabel("Resume:"));
        	panRes.add(new JLabel(""));
        	panRes.add(new JLabel(""));

        	panRes.add(new JScrollPane(taResume));
        	panRes.add(new JLabel(""));
        	//res.add(taResume);
        	
        	
        	
        	
        	res2.add(res);
        	res2.add(panRes);
        	res2.add(scrolled);
        	
        	if(!protocolFile.equals(""))
        	{
        		File faux = new File(protocolFile);
        		fillProtocolInfo(faux, taResume, panelParams);
        	}
        	
        	return res2;
        }
        
        
        
        private void executeDistributionRefresh( DefaultListModel listModelTypes, DefaultListModel listModelClients)
        {
        	listModelTypes.removeAllElements();
			for(String key : mapTypesAgents.keySet())
			{
				listModelTypes.addElement(key);
			}
			
			listModelClients.removeAllElements();
			for(String key : mapClientes.keySet())
			{
				listModelClients.addElement(key);
			}
        }
        
        private JPanel panelAgentDist()
        {
        	JPanel res = new JPanel();
        	res.setLayout(new GridLayout(2,1));
        	
        	final String[] selectedOptions = {null, null, null};
        	
        	final JSpinner spCount = new JSpinner();
        	
        	
        	final DefaultListModel listModelComb = new DefaultListModel();
        	final JList<String> lComb = new JList<String>(listModelComb);
        	lComb.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	lComb.setLayoutOrientation(JList.VERTICAL);
        	lComb.setVisibleRowCount(-1);
        	
        
        	
        	lComb.addListSelectionListener(new ListSelectionListener() {
				
				public void valueChanged(ListSelectionEvent arg0) {					
					selectedOptions[2] = lComb.getSelectedValue();
				}
			});
        	
        	
        	JScrollPane scComb = new JScrollPane(lComb);
        	scComb.setPreferredSize(new Dimension(140, 80));
        	
        	final DefaultListModel listModelTypes = new DefaultListModel();
        	final JList<String> lTypes = new JList<String>(listModelTypes);
        	lTypes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	lTypes.setLayoutOrientation(JList.VERTICAL);
        	lTypes.setVisibleRowCount(-1);
        	
        
        	
        	lTypes.addListSelectionListener(new ListSelectionListener() {
				
				public void valueChanged(ListSelectionEvent arg0) {					
					selectedOptions[0] = lTypes.getSelectedValue();
				}
			});
        	
        	
        	JScrollPane scLT = new JScrollPane(lTypes);
        	scLT.setPreferredSize(new Dimension(100, 80));
        	
        	
        	
        	final DefaultListModel listModelClients = new DefaultListModel();
        	final JList<String> lClients = new JList<String>(listModelClients);
        	lClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	lClients.setLayoutOrientation(JList.VERTICAL);
        	lClients.setVisibleRowCount(-1);
        	
        
        	
        	lClients.addListSelectionListener(new ListSelectionListener() {
				
				public void valueChanged(ListSelectionEvent arg0) {					
					selectedOptions[1] = lClients.getSelectedValue();
				}
			});
        	
        	
        	JScrollPane scLC = new JScrollPane(lClients);
        	scLC.setPreferredSize(new Dimension(100, 80));
        	
        	executeDistributionRefresh(listModelTypes, listModelClients);
        	JButton bRefresh = new JButton("Refresh");
        	
        	bRefresh.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) 
				{
					executeDistributionRefresh(listModelTypes, listModelClients);
					
				}
			});
        	
        	
        	JButton bSave = new JButton("Save");
        	bSave.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) 
				{
					if(selectedOptions[0] == null || selectedOptions[1] == null)
					{
						JOptionPane.showMessageDialog(null, "You must select an agent program and a node");
						return;
					}
					if(((Integer)spCount.getValue()) <= 0)
					{
						JOptionPane.showMessageDialog(null, "The agent count must be grater than 0");
						return;
					}
					if(mapTypeClient.containsKey(selectedOptions[0]))
					{
						if(mapTypeClient.get(selectedOptions[0]).containsKey(selectedOptions[1]))
						{
							JOptionPane.showMessageDialog(null, "You have already defined this combination, if you want to made changes fist delete this combination and define it again");
							return;
						}
						
					}
					
					if(selectedOptions[0].equals("contactPerson") && (Integer) spCount.getValue() > 1)
					{
						JOptionPane.showMessageDialog(null, "It can only be one contactPerson agent");
						return;
					}
					
					HashMap<String, Integer> mAux = new HashMap<String, Integer>();
					mAux.put(selectedOptions[1], (Integer)spCount.getValue());
					if(mapTypeClient.containsKey(selectedOptions[0])) //don't overwrite the inner map
					{
						mapTypeClient.get(selectedOptions[0]).put(selectedOptions[1], (Integer)spCount.getValue());
					}
					else
						mapTypeClient.put(selectedOptions[0], mAux);
					//refresh list of combinations
					listModelComb.addElement(selectedOptions[0] + "->" + selectedOptions[1] + "->" +spCount.getValue());
					
					int nA = 0;
					for(String k1 : mapTypeClient.keySet())
					{
						for(String k2 : mapTypeClient.get(k1).keySet())
						{
							nA += mapTypeClient.get(k1).get(k2);
						}
					}
					numAgents = nA;
					
				}
			});
        	
        	JButton bDelete = new JButton("Delete");
        	bDelete.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					if(selectedOptions[2] == null)
					{
						JOptionPane.showMessageDialog(null, "You have to select a combination to delete");
						return;
					}
					String[] vals = selectedOptions[2].split("->");
					mapTypeClient.get(vals[0]).remove(vals[1]);
					if(mapTypeClient.get(vals[0]).size() == 0)
						mapTypeClient.remove(vals[0]);
					
					listModelComb.removeElement(selectedOptions[2]);
					
					int nA = 0;
					for(String k1 : mapTypeClient.keySet())
					{
						for(String k2 : mapTypeClient.get(k1).keySet())
						{
							nA += mapTypeClient.get(k1).get(k2);
						}
					}
					numAgents = nA;
				}
			});
        	
        	JPanel p1 = new JPanel();
        	p1.setLayout(new GridLayout(3,3));
        	
        	JPanel p11 = new JPanel();
        	p11.setLayout(new GridLayout(3,1));
        	      
        	
        	
        	p1.add(new JLabel(""));
        	
        	
        	JPanel pR = new JPanel();
        	pR.setLayout(new GridLayout(3,1));
        	pR.add(new JLabel(""));
        	pR.add(new JLabel(""));
        	pR.add(bRefresh);
        	p1.add(pR);
        	
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel("Agent Programs:"));
        	p1.add(new JLabel("Nodes:"));
        	p1.add(new JLabel("Number of Agents:"));
        	
        	 	
        	p1.add(scLT);
        	p1.add(scLC);
        	p11.add(spCount);
        	p11.add(new JLabel(""));       
        	p11.add(bSave);
        	
        	JPanel p12 = new JPanel();
        	//p12
        	
        	p1.add(p11);
        	/*p1.add(new JLabel(""));       
        	p1.add(new JLabel(""));
        	p1.add(bSave);*/
        	
        	
        	JPanel p2 = new JPanel();
        	p2.setLayout(new GridLayout(1,3));
        	
        	JPanel p21 = new JPanel();
        	p21.setLayout(new GridLayout(3,1));
        	
        	p21.add(new JLabel("Combinations:"));
        	p21.add(scComb);
        	
        	JPanel pD = new JPanel();
        	pD.setLayout(new GridLayout(3,1));
        	pD.add(new JLabel(""));        	
        	pD.add(bDelete);
        	pD.add(new JLabel(""));
        	
        	p21.add(pD);
        	
        	p2.add(new JLabel(""));
        	p2.add(p21);
        	p2.add(new JLabel(""));
        	
        	res.add(p1);
        	res.add(p2);
        	
        	//res.add(bSave);
        	//res.add(scComb);
        	//res.add(bDelete);
        	                	
        	//init from XML file
        	for(String k1 : mapTypeClient.keySet())
        	{
        		for(String k2: mapTypeClient.get(k1).keySet())
        		{
        			listModelComb.addElement(k1 + "->" + k2 + "->" + mapTypeClient.get(k1).get(k2));
        		}
        	}
			
			int nA = 0;
			for(String k1 : mapTypeClient.keySet())
			{
				for(String k2 : mapTypeClient.get(k1).keySet())
				{
					nA += mapTypeClient.get(k1).get(k2);
				}
			}
			numAgents = nA;
        	
        	return res;
        }
        
        
       
        
        private JPanel panelEvaluation()
        {
        	final JPanel res = new JPanel();
        	
        	/*if(typeDataSource.equals("Single file"))
        		panelEvalTestSingle();
        	else
        		panelEvalTestRound();*/
        	
        	res.setLayout(new BorderLayout());
        	
        	final JPanel panParams = new JPanel();
        	
        	final JComboBox<String> comboTypesEval = new JComboBox<String>();
        	comboTypesEval.addItem("Single file");
        	comboTypesEval.addItem("Round files");
        	comboTypesEval.addItem("Hold-Out");
        	comboTypesEval.addItem("Cross-Validation");
        	
        	
        	
        	fillParamsStatic(panParams);        	
        	
        	comboTypesEval.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() !=2)
					{
						typeEval = (String) comboTypesEval.getSelectedItem();
						if(typeEval.equals("Hold-Out"))
						{
							fillParamsHoldOut(panParams);
							typeDataSource = "Round files"; //the behavior is the same
							res.validate();			
							//evalParams = new ArrayList<Object>();
						}
						else if(typeEval.equals("Single file"))
						{
							fillParamsStatic(panParams);
							typeDataSource = "Single file";
							repetitions = 1; //to avoid problems
							res.validate();
							evalParams = new ArrayList<Object>(); //reset options
						}
						else if(typeEval.equals("Round files"))
						{
							fillParamsStaticRound(panParams);
							typeDataSource = "Round files";
							res.validate();
							evalParams = new ArrayList<Object>(); //reset options
						}
						else if(typeEval.equals("Cross-Validation"))
						{
							fillParamsCrossValidation(panParams);
							typeDataSource = "Round files";
							res.validate();
							//evalParams = new ArrayList<Object>(); //reset options
						}
					}
					
				}
			});
        	        	        	   	
        	
        	JPanel panUpper = new JPanel();
        	panUpper.setLayout(new GridLayout(4,3));
        	
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel("Select a type of evaluation:"));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(comboTypesEval);
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        	panUpper.add(new JLabel(""));
        
        	JPanel panGPU = new JPanel();
        	JLabel lGPU = new JLabel("Enable GPU evaluation (only for J48)");
        	final JCheckBox cbGPU = new JCheckBox();
        	
        	cbGPU.addItemListener(new ItemListener() {
        	      public void itemStateChanged(ItemEvent e) {
        	       evalGPU = cbGPU.isSelected(); 
        	      }
        	    });
        	
        	panGPU.add(lGPU);
        	panGPU.add(cbGPU);
        	
        	
        	res.add(panUpper, BorderLayout.NORTH);
        	res.add(panParams);
        	res.add(panGPU, BorderLayout.SOUTH);
        	res.validate();
        	
        	if(typeEval.equals("Round files"))
        	{        		
        		comboTypesEval.setSelectedIndex(1);
        		fillParamsStaticRound(panParams);
        	}
        	else if(typeEval.equals("Hold-Out"))
        	{
            	comboTypesEval.setSelectedIndex(2);
            	fillParamsHoldOut(panParams);
        	}
        	else if(typeEval.equals("Cross-Validation"))
        	{
            	comboTypesEval.setSelectedIndex(3);
            	fillParamsCrossValidation(panParams);
        	}
        	res.validate();
        	
        	
        	return res;
        }
        
        private void fillParamsStatic(JPanel panel)
        {
        	        	
        	panel.removeAll();
        	
        	panel.validate();
        	
        	panel.setLayout(new GridLayout(1,3));
        	
        	JPanel res = new JPanel();
        	
        	res.setLayout(new GridLayout(20,1));
        	        
        	
        	final JTextField tfFile = new JTextField();
        	tfFile.setEditable(false);
        	tfFile.setText(testData);
        	JButton bEx = new JButton("Load");
        	bEx.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					File ar = openFile();
					if(ar != null)
					{
						tfFile.setText(ar.getPath());
						testData = ar.getPath();
					}
					
				}
			});
        	
        	
        	
        	
        	res.add(new JLabel("Select the test file:"));
        	res.add(bEx);
        	res.add(tfFile);
        	res.validate();
        	res.repaint();
        	
        	
        	panel.add(new JLabel(""));
        	panel.add(res);
        	panel.add(new JLabel(""));
        	panel.validate();
        	
        	
        }
        
        private void fillParamsStaticRound(JPanel panel)
        {
        	        	
        	panel.removeAll();
        	
        	panel.validate();
        	
        	panel.setLayout(new GridLayout(1,3));
        	
        	JPanel res = new JPanel();
        	
        	
        	res.setLayout(new GridLayout(15,1));
        	
        	SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, 100, 1);
        	
        	final JSpinner spinnerReps = new JSpinner(sModel);
        	spinnerReps.setValue(repetitions);
        	
        	final JTextField tfFile = new JTextField();   
        	tfFile.setText(testDataBase);
        	JButton bEx = new JButton("Save");
        	final JTextField tfBN = new JTextField();
			tfBN.setColumns(20);
			tfBN.setText(roundFilesBase);
        	bEx.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) 
				{
					
					if(tfFile.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The  base test name and path can't be empty");
						return;
					}
					
					if(tfBN.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The base train name can't be empty");
						return;
					}
					
					
					testDataBase = tfFile.getText().trim();
					roundFilesBase = tfBN.getText().trim();
					repetitions = (Integer)spinnerReps.getValue();
				}
			});
        	res.add(new JLabel("Base Train name:"));        	
        	res.add(tfBN);
        	res.add(new JLabel("Test file base name with path:"));        	
        	res.add(tfFile);
        	res.add(new JLabel("Number of Round files:"));
        	res.add(spinnerReps);
        	res.add(bEx);
        	                	        	        	        	        	        	        	        
        	panel.add(new JLabel(""));
        	panel.add(res);
        	panel.add(new JLabel(""));        	
        	panel.validate();
        	
        	
        }
        
        private void fillParamsHoldOut(JPanel panel)
        {
        	panel.removeAll();
        	
        	
        	
        	final JSpinner spinnerPer;
        	SpinnerModel sModelPer; 
        	        	
        	sModelPer = new SpinnerNumberModel(1, 1, 100, 1);
        	
        	spinnerPer = new JSpinner(sModelPer);
        	if(evalParams.size() > 0)      
        	{
        		try
        		{
        			spinnerPer.setValue((int)((Double)evalParams.get(1) *100));
        		}
        		catch(Exception e){}
        	}
        	
        	final JSpinner spinnerReps;
        	SpinnerModel sModel; 
        	        	
        	sModel = new SpinnerNumberModel(1, 1, 100, 1);
        	
        	spinnerReps = new JSpinner(sModel);
        	spinnerReps.setValue(repetitions);
        	
        	final JTextField tfFile = new JTextField();
        	tfFile.setText(dataSet);
        	tfFile.setEditable(false);
        	
        	final JTextField tfTestPath = new JTextField();        	
        	tfTestPath.setText(testPath);
        	
        	JButton bEx = new JButton("Load");
        	bEx.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					File ar = openFile();
					if(ar != null)
					{
						tfFile.setText(ar.getPath());
						dataSet = ar.getPath();
					}
					
				}
			});        	       	
        	
        	JButton bSave = new JButton("Save");
        	
        	bSave.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {					
					
					if(tfTestPath.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The test path can't be empty");
						return;
					}
					
					if((Integer) spinnerReps.getValue() <= 0 || (Integer) spinnerReps.getValue() > 100)
					{
						JOptionPane.showMessageDialog(null, "The train percent has to be in te range (0, 100]");
						return;
					}
					
					evalParams = new ArrayList<Object>(); //delete previous params
					evalParams.add("Hold-Out");
					evalParams.add(((Integer) spinnerPer.getValue())*.01);	//to have a value between 0 and 1
					testPath = tfTestPath.getText().trim();
					repetitions = (Integer)spinnerReps.getValue();
				}
			});
        	
        	
        	panel.setLayout(new GridLayout(2,1));
        	
        	JPanel p1 = new JPanel();
        	p1.setLayout(new GridLayout(12,3));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Dataset File:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(tfFile);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(bEx);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Test Data path:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(tfTestPath);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Train percentage:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(spinnerPer);
        	p1.add(new JLabel(""));      
        	
        	p1.add(new JLabel(""));        	        	
        	p1.add(new JLabel("Number of repetitions:"));
        	p1.add(new JLabel(""));
        	        	  	
        	
        	p1.add(new JLabel(""));
        	p1.add(spinnerReps);
        	p1.add(new JLabel(""));
        	        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(bSave);
        	p1.add(new JLabel(""));
        	
        	panel.add(p1);
        	panel.validate();
        	
        }
        
        private void fillParamsCrossValidation(JPanel panel)
        {
        	panel.removeAll();
        	
        	
        	
        	final JSpinner spinnerPer;
        	SpinnerModel sModelPer; 
        	        	
        	sModelPer = new SpinnerNumberModel(1, 1, 100, 1);
        	
        	spinnerPer = new JSpinner(sModelPer);
        	
        	
        	final JSpinner spinnerReps;
        	SpinnerModel sModel; 
        	        	
        	sModel = new SpinnerNumberModel(1, 1, 100, 1);
        	
        	spinnerReps = new JSpinner(sModel);
        	
        	if(evalParams.size() > 0)
        	{
        		try
        		{
        		int folds = (Integer)evalParams.get(1);
        		spinnerPer.setValue(folds);
        		spinnerReps.setValue(repetitions/folds);
        		}
        		catch(Exception e){}
        	}
        	
        	final JTextField tfFile = new JTextField();
        	//tfFile.setEditable(false);
        	tfFile.setText(dataSet);
        	
        	final JTextField tfTestPath = new JTextField();        	
        	tfTestPath.setText(testPath);
        	
        	JButton bEx = new JButton("Load");
        	bEx.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					File ar = openFile();
					if(ar != null)
					{
						tfFile.setText(ar.getPath());
						dataSet = ar.getPath();
					}
					else
						dataSet = tfFile.getText().trim();
					
				}
			});        	       	
        	
        	JButton bSave = new JButton("Save");
        	
        	bSave.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {					
					
					if(tfTestPath.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The test path can't be empty");
						return;
					}
					
					if((Integer) spinnerReps.getValue() <= 0 || (Integer) spinnerReps.getValue() > 100)
					{
						JOptionPane.showMessageDialog(null, "The train percent has to be in te range (0, 100]");
						return;
					}
					
					evalParams = new ArrayList<Object>(); //delete previous params
					evalParams.add("Cross-Validation");
					evalParams.add(((Integer) spinnerPer.getValue()));	
					testPath = tfTestPath.getText().trim();
					repetitions = ((Integer)spinnerReps.getValue()) * ((Integer) spinnerPer.getValue()); //folds * repetitions
				}
			});
        	
        	
        	panel.setLayout(new GridLayout(2,1));
        	
        	JPanel p1 = new JPanel();
        	p1.setLayout(new GridLayout(12,3));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Dataset File:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(tfFile);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(bEx);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Test Data path:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(tfTestPath);
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel("Folds:"));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(spinnerPer);
        	p1.add(new JLabel(""));      
        	
        	p1.add(new JLabel(""));        	        	
        	p1.add(new JLabel("Number of repetitions:"));
        	p1.add(new JLabel(""));
        	        	  	
        	
        	p1.add(new JLabel(""));
        	p1.add(spinnerReps);
        	p1.add(new JLabel(""));
        	        	
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	p1.add(new JLabel(""));
        	
        	p1.add(new JLabel(""));
        	p1.add(bSave);
        	p1.add(new JLabel(""));
        	
        	panel.add(p1);
        	panel.validate();
        	
        }
        
        private JPanel panelDispatch()
        {
        	JPanel res = new JPanel();
        	
        	final JTextArea taResume = new JTextArea();
        	taResume.setRows(5);
        	//taResume.setColumns(15);
        	taResume.setEditable(false);
        	
        	JButton bRefresh = new JButton("Refresh");
        	
        	bRefresh.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					
					
					
					taResume.setText("");
					String nCli = "N. Nodes: " + mapClientes.size();										
					
					String nAgs = "N. Agents: " + numAgents;
					
					String tDataS = "Type of data source: " + typeDataSource;
					String tEval = "Type of evaluation: " + typeEval;
					
					taResume.setText("Strategy: " +protocolName + "\n" + nCli + "\n" + nAgs + "\n" + tDataS + "\n" + tEval);
				}
			});
        	        	
        	/*bStart.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					start();
					
				}
			});*/
        	
        	taResults = new JTextArea();
        	taResults.setEditable(false);
        	taResults.setRows(20);
        	taResults.setColumns(10);
        	
        	res.setLayout(new BorderLayout());
        	
        	JPanel pNor = new JPanel();
        	pNor.setLayout(new GridLayout(5,3));
        	
        	JPanel pN1 = new JPanel();
        	pN1.setLayout(new GridLayout(3,1));
        	
        	pNor.add(new JLabel(""));
        	pNor.add(new JLabel(""));
        	pNor.add(new JLabel(""));
        	
        	pN1.add(new JLabel("Experiment resume:"));        	        	
        	pN1.add(bRefresh);
        	
        	pNor.add(new JLabel(""));
        	pNor.add(pN1);
        	pNor.add(new JLabel(""));
        	
        	pNor.add(new JLabel(""));
        	pNor.add(new JScrollPane(taResume));
        	pNor.add(new JLabel(""));
        	
        	
        	JPanel pN2 = new JPanel();
        	pN2.setLayout(new GridLayout(3,1));
        	pN2.add(new JLabel(""));
        	pN2.add(bStart);
        	
        	pNor.add(new JLabel(""));
        	pNor.add(pN2);
        	pNor.add(new JLabel(""));
        	
        	pNor.add(new JLabel(""));
        	pNor.add(new JLabel("Results:"));
        	pNor.add(new JLabel(""));
        	
        	res.add(pNor, BorderLayout.NORTH);
        	
        	
        	//res.add(bStart);
        	
        	res.add(new JScrollPane(taResults), BorderLayout.CENTER);
        	
        	return res;
        }
        
        
        
        
        private void panelIzqClientes(JPanel res, final  JComboBox<String> comItems)
        {
        	res.removeAll();
        	
        	
        	
        	
        	JPanel pAux = new JPanel();
        
        	pAux.setLayout(new GridLayout(0,2));
        	
        	final JTextField tfIP =  new JTextField();
        	tfIP.setColumns(20);
        	
        	final JTextField tfName =  new JTextField();
        	tfName.setColumns(20);
        	
        	final JTextField tfUser =  new JTextField();
        	tfUser.setColumns(20);
        	
        	final JTextField tfPass=  new JTextField();
        	tfPass.setColumns(20);
        	
        	final JTextField tfPath =  new JTextField();
        	tfPath.setColumns(20);
        	
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel("Define a new Node"));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	
        	pAux.add(new JLabel("Name:"));
        	pAux.add(tfName);
        	
        	pAux.add(new JLabel("IP:"));
        	tfIP.setText("localhost:8080");
        	pAux.add(tfIP);
        	
        	
        	pAux.add(new JLabel("Data file path:"));
        	tfPath.setText("");
        	pAux.add(tfPath);
        	
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	pAux.add(new JLabel(""));
        	JButton bSave = new JButton("Save");
        	
        	bSave.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					if(tfName.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The Name field can't be empty");
						return;
					}
					if(tfIP.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The IP field can't be empty");
						return;
					}
					if(tfPath.getText().trim().equals(""))
					{
						JOptionPane.showMessageDialog(null, "The File Path field can't be empty");
						return;
					}
					if(mapClientes.containsKey(tfName.getText().trim()))
					{
						JOptionPane.showMessageDialog(null, "You are editing an existing node");
						//return;
					}
					for(String key : mapClientes.keySet())
					{
						if(key.equals(tfName.getText().trim())) 
							continue;
						if(mapClientes.get(key).contains(tfIP.getText().trim()))
						{
							JOptionPane.showMessageDialog(null, "You can't use the same IP and port for two different nodes, try to change the port");
							return;
						}
					}
					
					ArrayList<String> params = new ArrayList<String>();
					params.add(tfIP.getText().trim());
					params.add(tfUser.getText().trim());
					params.add(tfPass.getText().trim());
					params.add(tfPath.getText().trim());
					
					if(!mapClientes.containsKey(tfName.getText().trim()))
					{
						comItems.addItem(tfName.getText().trim());
						comItems.setSelectedIndex(comItems.getItemCount()-1);
					}
					
					mapClientes.put(tfName.getText().trim(), params);
					
				}
			});
        	
        	pAux.add(bSave);
        	
        	
        	comItems.addItemListener(new ItemListener() {
				
        		public void itemStateChanged(ItemEvent arg0) {
        			if(arg0.getStateChange() == 2) return;
        			
        			try
        			{
        			if(arg0.getItem() != null  && mapClientes.containsKey(arg0.getItem()))
        			{
        				ArrayList<String> pars = mapClientes.get(arg0.getItem());
        				tfName.setText(arg0.getItem().toString());
        				tfIP.setText(pars.get(0));
        				tfUser.setText(pars.get(1));
        				tfPass.setText(pars.get(2));
        				tfPath.setText(pars.get(3));
        			}
        			}catch(Exception e){}
					
				}
			});
        	
        	
        	res.add(pAux);
        	
        	res.validate();
        	
        	
        	
        }
        
        public void putText(String text)
        {	
        	taResults.append(text);
        	taResults.setCaretPosition(taResults.getDocument().getLength());
        }
            
    }
    
    private   HashMap<String, String> extractTypesProtocol(Document doc)
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
	
	private  HashMap<String, String> extractParamsProtocol (Document doc)
	{
		HashMap<String, String> res = new HashMap<String, String>();
		
		NodeList nList = doc.getElementsByTagName("param");
		
		for (int temp = 0; temp < nList.getLength(); temp++) 
		{
	 
			Node nNode = nList.item(temp);	 				 
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
				Element eElement = (Element) nNode;
				String key = eElement.getElementsByTagName("name").item(0).getTextContent();				
				String val = eElement.getElementsByTagName("type").item(0).getTextContent();
				res.put(key, val);
			}
		}
		
		return res;
	}
	
	private   String extractProtocolName (Document doc)
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
    
	
	
	//adds the components form the special params of a protocol
	public void addParams(JPanel res)
	{
		final HashMap<String, Component> mapParamComp = new HashMap<String, Component>(); //to have a relation between param and the actual compponent that has its value
		res.removeAll();
		
		res.setLayout(new GridLayout(0,1));
		
		JPanel pAux = new JPanel();
		pAux.setLayout(new GridLayout(0,2));
		
		if(mapParamType.isEmpty())
		{
			res.validate();
			return; //empty panel
		}
		
		
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel("Define Parameters"));
		pAux.add(new JLabel(""));
		for(String key : mapParamType.keySet())
		{
			pAux.add(new JLabel(key + ":"));
			String type = mapParamType.get(key);
			if(type.equals("int"))
			{
				JSpinner comp = new JSpinner();				
				mapParamComp.put(key, comp);				
				pAux.add(comp);				
				if(mapParamValue.containsKey(key) && !mapParamValue.get(key).equals(""))
					comp.setValue(new Integer(mapParamValue.get(key)));
			}
			else if(type.equals("string"))
			{
				JTextField comp = new JTextField();
				mapParamComp.put(key, comp);
				pAux.add(comp);
				if(mapParamValue.containsKey(key) && !mapParamValue.get(key).equals(""))
					comp.setText(mapParamValue.get(key));
			}
			else if(type.equals("boolean"))
			{
				JCheckBox comp = new JCheckBox();
				mapParamComp.put(key, comp);
				pAux.add(comp);
				if(mapParamValue.containsKey(key) && !mapParamValue.get(key).equals(""))
					comp.setSelected(new Boolean(mapParamValue.get(key)));
			}
			else if(type.equals("double"))
			{
				DoubleSpinner comp = new DoubleSpinner();
				mapParamComp.put(key, comp);
				pAux.add(comp);
				if(mapParamValue.containsKey(key) && !mapParamValue.get(key).equals(""))
					comp.setValue(new Double(mapParamValue.get(key)));
			}
		}
		
		JButton bSave = new JButton("Save");
		
		bSave.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				for(String kk : mapParamComp.keySet())
				{
					Component comp = mapParamComp.get(kk);
					if(comp.getClass() == JSpinner.class ||  comp.getClass() == DoubleSpinner.class)
					{
						JSpinner comp2 = (JSpinner) comp;						
						mapParamValue.put(kk, comp2.getValue().toString());
					}
					else if(comp.getClass() == JTextField.class)
					{
						JTextField comp2 = (JTextField) comp;						
						mapParamValue.put(kk, comp2.getText());
					}
					else if(comp.getClass() == JCheckBox.class)
					{						
						JCheckBox comp2 = (JCheckBox) comp;						
						mapParamValue.put(kk, ((Boolean)comp2.isSelected()).toString());
					}
				}
				
			}
		});
		
		
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));		
		pAux.add(bSave);
		pAux.add(new JLabel(""));
		pAux.add(new JLabel(""));
		
		
		res.add(pAux);
		res.validate();
		
	}
	
	 public class DoubleSpinner extends JSpinner
	 {

	        private static final long serialVersionUID = 1L;
	        private static final double STEP_RATIO = 0.1;

	        private SpinnerNumberModel model;

	        public DoubleSpinner() {
	            super();
	            // Model setup
	            model = new SpinnerNumberModel(0.0, -1000.0, 1000.0, 0.1);
	            this.setModel(model);

	            // Step recalculation
	            this.addChangeListener(new ChangeListener() {
	                
	                public void stateChanged(ChangeEvent e) {
	                    Double value = getDouble();
	                    // Steps are sensitive to the current magnitude of the value
	                    long magnitude = Math.round(Math.log10(value));
	                    double stepSize = STEP_RATIO * Math.pow(10, magnitude);
	                    model.setStepSize(stepSize);
	                }
	            });
	        }

	        /**
	         * Returns the current value as a Double
	         */
	        public Double getDouble() {
	            return (Double)getValue();
	        }
	        
	        
	        

	    }
	 
	 String getLocalIP()
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
		            	return (i.getHostAddress());		            
		            }
		        }
		    }
			}
			catch(Exception e){e.printStackTrace();}
			return("localhost");
		}
	 

}

