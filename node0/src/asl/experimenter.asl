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

// Agent experimenter in project experimenter

/* Initial beliefs and rules */


iteration(1). //always begins at 1 
 //to append in the agent names to make the name unique, it increments

/* Initial goals */


!ini.


/* Plans */



+!ini : mode(experiment)
	<-
	+evalGPU(false); //until evalGPU is added to the configuration file
	makeArtifact("experimenter","artifacts.Experimenter",["results/raw.txt","results/sumary.txt","results/confusion.txt"],Id);
	!start;
	+begin.
	
+!ini : true
	<-
	-+mode(gui);
	makeArtifact("gui","artifacts.GUI",[],Id);
	focus(Id);			
	!start.

+!start: true
	<-			    
		makeArtifact("utils","artifacts.Utils",[],IdUtils);						
		makeArtifact("evaluator","artifacts.Evaluator",[],IdEval);
		focus(IdEval);				
		+evaluatorId(IdEval);
		+evaluatorName("evaluator");
		getNodeId(MyNode);
		+myNode(MyNode).

+begin : true
	<-
		-+accuracys([]); //to save accuracy results per iteration
		-+times([]);
		-+traffics([]);
		-+nExamples([]);
		?serverIP(LIP); //the ip configured
		.concat(LIP,":20100",LIP2); //by default that is the port
		-+ipServer(LIP2);
		makeArtifact("directory","artifacts.Directory",[],IdDic); //this is the only artifact that needs to be restarted after a complete experiment				
		!normalizeClients;
		!normalizeTypes;	
		!linkAllNodes;
		?evalParams(ListParams); //check if hold out or cross validation is being used
		.length(ListParams, Tam);
		makeArtifact("oracle","artifacts.Oracle",[],IdOr);
		if(Tam \== 0)
		{			
			.nth(0, ListParams, X);
			?testPath(Testpath);
			
			//go to each site and create a fileManager artifact, return the id and save it in a list
			!createFileManagers;
			?fmIds(FMList);
			?dataSet(Data);
			?repetitions(Reps);
			if(X == "Hold-Out")
			{
				.nth(1, ListParams, TPer);
				holdOut(Data, TPer, FMList, Reps, Testpath, RFName); //distribute all the data i one shot
				-+roundFilesBase(RFName); //to have a proper name
				.concat(Testpath, "/", TPInter);
				.concat(TPInter, RFName, TPFinal);
				-+testDataBase(TPFinal);
			}
			else
			{
				if(X == "Cross-Validation")
				{
					.nth(1, ListParams, TFolds);
					crossValidation(Data, TFolds, FMList, Reps, Testpath, RFName); //distribute all the data i one shot
					-+roundFilesBase(RFName); //to have a proper name
					.concat(Testpath, "/", TPInter);
					.concat(TPInter, RFName, TPFinal);
					-+testDataBase(TPFinal);
				};
			};
		};
		!newIteration.
	
+!createFileManagers: true
	<-
	-+fmIds([]); //to save the ids
	for(nodeID(Client, ID) ) //go to each node 
	{
		?fmIds(FMIDs);
		?client(Client, _, _, _, Path);
		getClientIp(Client, IPC);
		joinRemoteWorkspace("default",IPC,_);		
		makeArtifact("fileManager","artifacts.FileManager",[Path],IdFM);
		.concat(FMIDs, [IdFM], NList);
		-+fmIds(NList);
		quitWorkspace; //return to original workspace
	}.				
		
+!newIteration: true
	<-		
		?evaluatorId(IdEval);
		registerArtifactId("evaluator", IdEval); //register on directory
		?typeDataSource(TS);				
		if(TS == "Round files") //change test
		{
			?iteration(It);
			?testDataBase(RB);			
			attachNumberToFile(RB, It, NF);
			-+testData(NF);
		};
		!createAndDistributeAgents.
		//continues in +ready


+!createTrafficMonitors: true
	<-
	?clients(C);
	selectOneClientPerIp(C, Res);	
	for ( .member(X,Res) ) 
	{
		?typeDataSource(TDS);
		?client(_, X, _, _, File);
		if(TDS == "Single file")
		{
			getDirectory(File, Dir);
		}
		else
		{
			?client(_, X, _, _, Dir);
		};
		
		
		stripPort(X, IP);
        joinRemoteWorkspace("default",X,_);   
        makeArtifact("monitor","artifacts.TrafficMonitor",[],_);   
        ?iteration(Iter); 
        beginMonitoring(IP, Dir, Iter);        
        quitWorkspace;
    }.

//links all the nodes for comunication bwtween 		
+!linkAllNodes: true
	<-
	
	for(nodeID(Client, ID)) //go to each node and link it with the rest
	{
		getClientIp(Client, IPC);
		joinRemoteWorkspace("default",IPC,_);
		//link with the server			
		?myNode(ServerNode);
		?ipServer(LIP);
		enableLinkingWithNode(ServerNode, "default", LIP);
		for(nodeID(Client2, ID2))
		{			
			if (Client \== Client2) //dont link the same node 
			{
				?client(Client2,IPC2, _, _, _);
				enableLinkingWithNode(ID2, "default", IPC2);
     		};
		};
		quitWorkspace; //return to original workspace
	}.		

//creates a believe for client to have easy access to client info
+!normalizeClients : true
	<-
	?clients(List);
	!normalizeRec(List).
	
+!normalizeRec([]). //end of recursion

+!normalizeRec([[Name, Ip, User, Pass, File] | T]) : true
	<-
		registerClient(Name, Ip); //register in directory
		joinRemoteWorkspace("default",Ip,_); //go an register the node id
		getNodeId(NodeId);
		+nodeID(Name, NodeId);
		quitWorkspace; //return
		+client(Name,Ip, User, Pass, File);
		!normalizeRec(T).

+!normalizeTypes : true
 <-
 	?typesAgents(List);
 	!normalizeTypeRec(List).
 	
 +!normalizeTypeRec([]). //end of recursion

+!normalizeTypeRec([[Type, File] | T]) : true
	<-
		+typeCount(Type, 1); //how many of this type there are
		+typeAgent(Type, File);
		registerTypeFile(Type, File); //so the strategy can create new agents
		!normalizeTypeRec(T).

//crates the agents, asign them to a work space and fills the directory artifact

+!createAndDistributeAgents : true
	<-
	?agentsDist(List);
	for (.member(X, List) ) //for each combination
	{
        !processOneComb(X);
    }.
	
	
	
+!processOneComb([Type, Client, Num]) : true
	<-
	?typeAgent(Type, AgFile);
	?client(Client, IP, Name, Pass, File);
	
	for (.range(X,1,Num))
	{				
		incrementTypeCount(Type);
		?typeCount(Type, Count);
		.concat(Type,Count,NAg);
		!createOneAg(NAg, AgFile, Client, IP, File);		
		-typeCount(Type, Count);
		+typeCount(Type, Count+1);
	}.
	


+!createOneAg(Name, AgFile, Client, IP, File): true
	<-	
	.create_agent(Name, AgFile, [agentArchClass("c4jason.CAgentArch")]);
	registerAgentClient(Name, Client);
	+agent(Name); //save the name of the agent
	.plan_label(PJR, joinRemote);
	.send(Name, tellHow, PJR);
	
	.plan_label(PSM, sendModel); //every agent knows how to send a model
	.send(Name, tellHow, PSM);
	
	.plan_label(PSMGPU, sendModelGPU); //every agent knows how to send a gpu model
	.send(Name, tellHow, PSMGPU);
	
	.plan_label(PRA, registerArtifact); //every agent knows how to register an artifact in the directory
	.send(Name, tellHow, PRA);
	
	.plan_label(CTC, consultTypeCount);
	.send(Name, tellHow, CTC);
	
	.plan_label(CTF, consultTypeFile); 
	.send(Name, tellHow, CTF);
	
	.send(Name, achieve, join_remote(IP));	
	.send(Name, tell, site(Client)); //tell the name of its site	
	?ipServer(LIP);
	.send(Name, tell, ipServer(LIP)); //tell the location of the server
	
	//tell the parameters of the protocol configured
	?paramsProto(Params);
	for(.member(XParam,Params))
	{
		.nth(0, XParam, ParamName); //there's always 2 fields
		.nth(1, XParam, ParamVal);
		.send(Name, tell, param(ParamName, ParamVal));
	};
	
	//tell the name of the data file for this iteration
	?typeDataSource(TS);
	if(TS == "Single file") //don't append a number
	{
		.send(Name, tell, data(File))		
	}
	else
	{				
		if(TS == "Round files")
		{
			?iteration(It);
			?roundFilesBase(RB);
			attachNumberToFile(File, RB, It, NF);
			.send(Name, tell, data(NF))
		};
	}.

+ready(_):  numAgents(Nags) & .count(ready(_),E) & E == Nags
	<- 
	println("All agents are ready");			
	!createTrafficMonitors; 		
	startTimer; //begin the iteration time count		
	.send(contactPerson1, tell, begin_process). //tell the contactPerson to begin its process

+!waitAllReady: numAgents(Nags) & .count(ready(_),E) & E == Nags
	<-	println("All agents are ready").		
		
+!waitAllReady: true
	<- .wait(50); 
	println("waiting...");
	!waitAllReady.

+!prepareToFinish(EN): true
	<-
	stopTimer; //do not consider the time required to evaluate the model that is not part of the protocol
	
		?evaluatorName(EN);
		
		//stop traffic monitoring and save values
		?clients(C);
		selectOneClientPerIp(C, Res);	
		-+traffic(0);
	 	for ( .member(X,Res) ) 
	 	{
	 	
	 		?typeDataSource(TDS);
			?client(_, X, _, _, File);
			if(TDS == "Single file")
			{
				getDirectory(File, Dir);
			}
			else
			{
				?client(_, X, _, _, Dir);
			};
	 		
        	joinRemoteWorkspace("default",X,_);
        	.wait(1000); //to have a more accurate reading
        	?iteration(Iter);
        	getTraffic(Dir, Traffic, Iter);
        	?traffic(Val);
        	-+traffic(Val+Traffic);
        	quitWorkspace;
     	}.

//to end the iteration 
+finish(From)[source(contactPerson1)] : true
	<-	    
		!prepareToFinish(EN);		
		.send(contactPerson1, achieve, send_model(From, EN)).
		
//to end the iteration and retrieve the model from other agent
+finish(Ag, From)[source(contactPerson1)] : true
	<-	    
		!prepareToFinish(EN);		
		.send(Ag, achieve, send_model(From, EN)).
		
//when the model is loaded in the evaluator this signal is received 
+readyToEvaluate: true
	<-
		?testData(TDF);
		?traffic(Traffic);
		feedTestDataFromFile(TDF);
		?evalGPU(EGPU);
		if(EGPU == true) //this may cause an error if the model is not supported
		{
			initializeGPU;
			transformModel2GPU;
			println("GPU Evaluation");
			evaluateGPU(PcCorrect, NC, NI, TEx);
			freeGPU;
		}
		else
		{
			//evaluate(PcCorrect, NC, NI, TEx);
			evaluateWithConfusion(PcCorrect, NC, NI, TEx, Confusion);
		}
		println(PcCorrect);
		println(NC);
		println(NI);
       
		timeElapsed(TElapsed);
		?iteration(Iter);
		printResult("Iteration: ", Iter);
		printResult("*********************************************");
		printResult("Classification accuracy: ", PcCorrect, "%");
		printResult("Correctly classified: ", NC);
		printResult("Incorrectly classified: ", NI);
		printResult("Training examples used: ", TEx);
		printResult("Time elapsed (in seconds): ", TElapsed);
		printResult("Total traffic (in megabytes) ", Traffic);	
		printResult(Confusion);			
		printResult("*********************************************\n");
		//save the current results
		?accuracys(Acs);
		?times(Times);
		?traffics(Traffics);
		?nExamples(NExamples);
		.concat(Acs, [PcCorrect], NAcs);
		.concat(Times, [TElapsed], NTimes);
		.concat(Traffics, [Traffic], NTraffics);
		.concat(NExamples, [TEx], NNExamples);
		-+accuracys(NAcs);
		-+times(NTimes);
		-+traffics(NTraffics);
		-+nExamples(NNExamples);
				
		!endIteration.
		
+!endIteration: true
	<-
		-finish(_)[source(contactPerson1)];
		-finish(_, _)[source(contactPerson1)];
		.abolish(typeCount(_,_));
		.abolish(ready(_)[source(_)]);
		for(typeAgent(Type, _)) //restart with one
		{
			+typeCount(Type, 1);
		};
		/*Go to each client and delete all the artifacts*/
		for(client(_, IP, _, _, _))
		{
			joinRemoteWorkspace("default",IP,WspID2);
			!deleteArtifacts;
			quitWorkspace;
		};
		!deleteAgents;
		?repetitions(Reps);
		?iteration(Iter);
		if(Iter < Reps)
		{
			-+iteration(Iter+1);
			clearAgentasAndArtifacts; //clear the things that where put  by the protocol during this iteration  in the directory
			println("Begining new iteration");
			!newIteration;
		}
		else //end of experiment
		{
			!finishExperiment;
		}.
		

+!finishExperiment: true
	<-
		lookupArtifact("directory", IDD);
		disposeArtifact(IDD); //dispose data of directory
		
		lookupArtifact("oracle", IDOr);
		disposeArtifact(IDOr); //dispose data of directory
		
		//measure means and standard deviations
		?accuracys(Acs); //to save accuracy results per iteration
		?times(Times);
		?traffics(Traffics);
		?nExamples(NExamples);
		mean(Acs, MAcs);
		mean(Times, MTimes);
		mean(Traffics, MTraffics);
		mean(NExamples, MNExamples);
		stdDev(Acs, SAcs);
		stdDev(Times, STimes);
		stdDev(Traffics, STraffics);
		stdDev(NExamples, SNExamples); 
		getOverallConfusionMatrix(Confusion);
		?protocolName(ProName);
		?mode(Mode);		
		?roundFilesBase(RoundBase);
		if(Mode == experiment)
		{
			printConfusion(Confusion);	
			printSummary(RoundBase, " & ", ProName, " & ", MAcs, " $\\pm$ ", SAcs, " & ", MNExamples, " $\\pm$ ", SNExamples, " & ", MTimes, " $\\pm$ ", STimes, " & ", MTraffics, " $\\pm$ ", STraffics);
		}
		else
		{
			printResult("Global Results: ", ProName);
			printResult("**************************************");
			printResult("Mean classificaction accuracy: ", MAcs, " +/- ", SAcs);
			printResult("Mean training exaples used: ", MNExamples, " +/- ", SNExamples);
			printResult("Mean time (seconds): ", MTimes, " +/- ", STimes);
			printResult("Mean traffic (megabytes): ", MTraffics, " +/- ", STraffics);
			printResult(Confusion);		
			printResult("**************************************\n");
		};
		println("End of experiment");
		-+iteration(1);
		//clear some believes 
		.abolish(client(_,_,_,_,_));
		.abolish(nodeID(_,_));
		.abolish(typeAgent(_,_));		
		.abolish(typeCount(_,_));
		
		
		if(mode(experiment)) //terminate process
		{
			exit;
		}
		else
		{
			activateStart; //GUI			
		}.
		
+!deleteAgents: true
	<-
		for(agent(Ag))
		{
			.kill_agent(Ag);
			-agent(Ag);
		}.	

//doesn't delete default artifacts
+!deleteArtifacts: true
	<-
	getCurrentArtifacts(List);
	for ( .member(X,List) ) 
	{
		if(X \== "console" & X \== "node" & X \== "workspace" & X \== "blackboard" & X \== "manrepo")
		{
			lookupArtifact(X, ID);
			disposeArtifact(ID);        	
        };
     }.
	
@joinRemote
+!join_remote(IP) 
	<-  
	joinRemoteWorkspace("default",IP,WspID2); 	
	.my_name(N) ; 
	.send(experimenter, tell, ready(N)).

	
@sendModel
//From and To are artifact names, IPDest is the ip where the artifact To is located
+!send_model(From, To): true
	<-	
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getArtifactId(To, PlaceHolder); //use directory	
	quitWorkspace; //return to previous workspace
	sendModel(PlaceHolder)[artifact_name(From)].
	
@sendModelGPU
//From and To are artifact names, IPDest is the ip where the artifact To is located
+!send_modelGPU(From, To): true
	<-	
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getArtifactId(To, PlaceHolder); //use directory	
	quitWorkspace; //return to previous workspace
	sendModelGPU(PlaceHolder)[artifact_name(From)].
	
	
//to save an artifact id in the directory
@registerArtifact
+!register_artifact(ArtName): true
	<-
	lookupArtifact(ArtName, ID);
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	registerArtifactId(ArtName, ID);
	quitWorkspace.
	
@consultTypeCount
+!consut_type_count(Type, Count): true
	<-	
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getTypeCount(Type, Count);
	quitWorkspace.
	
@consultTypeFile
+!consut_type_file(Type, File): true
	<-	
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getTypeFile(Type, Count);
	quitWorkspace.
		
//services start

//this is a service given by the platform, so agents can be created dynamically
@spawn[atomic]
+?spawnNewAgent(Type, Client, OutName)
    <-	
    ?typeCount(Type, Count);
    incrementTypeCount(Type);
    -+typeCount(Type, Count+1);
    .concat(Type,Count,OutName);
    ?client(Client, IP, Name, Pass, File);
    getTypeFile(Type, SF);
    !createOneAg(OutName, SF, Client, IP, File);
    .term2string(TN, OutName);
    !wait4ParticularReady(TN).
    
//complement to previous service
+!wait4ParticularReady(Ag): ready(Ag)
	<-
		println("New agent ready").	
		
+!wait4ParticularReady(Ag): true
		<-		
        .wait(50); 
	!wait4ParticularReady(Ag).
		