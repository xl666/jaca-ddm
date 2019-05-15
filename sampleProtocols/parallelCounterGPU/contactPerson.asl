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

+begin_process: true
		<-
	
		!consut_type_count("worker", Count);				 		
		for (.range(X,1,Count))
		{
			.concat("worker",X,NAg);
			.send(NAg, achieve, start);
		}.
					

+atts(N,A)[source(worker1)] : true
	<-
	        ?param("Pruning", ParamVal);
		
		 makeArtifact("coordinatorj48","artifacts.ClassifierJ48",[b(B, a(N, A)), ParamVal, true], IdJ48);
		
		!register_artifact("coordinatorj48"); 
		
		!begin.


+!begin: true
	<- 
		!consut_type_count("worker", Count);
		+numAgents(Count);
		!waitAllReady;
		.send(roundController1, tell, begin);

		 !waitRoundController;

		for (.range(X,1,Count))
		{
		    .concat("worker",X,NAg);
		    .send(NAg, tell, j48Model("coordinatorj48"));
		    if(X > 1)  //worker 1 can not load data to the gpu yet
		    {
			.send(NAg, achieve, initGPU);
		    };
		};


		//begin
		//create base model
		.send(worker1, achieve, sendPercentage);

		-+round(1). //how many rounds are there

+initFinish: true
	<-
	.send(worker1, achieve, initGPU); //now it can load it
	induceGPU;
	!waitAllGPUsReady;
	!beginProcess.


+!saveTrain : true
	      <-
	    //save train file
	    ?param("Filtered data directory", Path);
	    //add iteration info
	      ?data(File);
	    makeArtifact("utilsExtra","artifacts.Utils",[],IdUtilsE);				getFileName(File, NameF);		
	    .concat(Path,"/", NormalizedP);
	    .concat(NormalizedP, NameF, FinalPath);
	     dumpInstances2File(FinalPath); //save final window
	     //tell workers to save left overs
	     ?numAgents(Count);
	     for (.range(X,1,Count))
	     {
		.concat("worker",X,NAg);
		.send(NAg, achieve, saveLefts);
	     };
	     !waitSaveWorkers;
	     .abolish(saved(_)).

//send the model to all the agents
+!beginProcess: true
        <-
	?numAgents(Count);
        -+contradiction(false);
        for (.range(X,1,Count))
	{
	    .concat("worker",X,NAg);
	    .term2string(Ag, NAg);
	    ?agent(Ag, ArtifactName);
	    !send_modelGPU("coordinatorj48", ArtifactName);
	};
	!waitAllFinish;
	induceGPU;
	 if(ContraFound == true)
	{
	    -+contradiction(true);
	};
        ?contradiction(Cont);
	?param("Max Rounds", MaxRounds);
	?round(Round);
	if(Cont == true & Round < MaxRounds) //another round is necessary
        {
	    !send_modelGPU("coordinatorj48", "evaluatorController"); //send the current model for evaluation
	    -+round(Round + 1);
        }
	else
	{
	    !freeAll;
	    !saveTrain;
	    .send(experimenter, tell, finish("coordinatorj48"));
	}.
	

+!waitAllFinish : numAgents(Nags) & .count(finish(Name, CF),E) & E == Nags
	<-								  
	println("all the examples gathered");
	.abolish(finish(_,_)).

+!waitAllFinish : true
	<- .wait(50); 
	!waitAllFinish.
	 

//the round controller tells if you must continue
+continue[source(roundController1)]: true
     <-
     -continue[source(roundController1)]; // for the next time
     !beginProcess. //do another iteration

+dontContinue[source(roundController1)]: true
     <-
     !freeAll;
     !saveTrain;
     .send(experimenter, tell, finish("coordinatorj48")). //finish


+finish(AgName, ContraFound)[source(Ag)]: true
        <-
	if(ContraFound == true)
	{
	    -+contradiction(true);
	}.

+ready(Name, Artifact)[source(Ag)]: true
	<-
	+agent(Name, Artifact). //to know the name of the agent and their artifact to send the model

+!waitAllReady: numAgents(Nags) & .count(ready(Name, Artifact),E) & E == Nags
	<-
	println("All workers are ready").

		
+!waitAllReady: true
	<- .wait(50); 
	!waitAllReady.

+!waitAllGPUsReady: numAgents(Nags) & .count(gpuready(Name),E) & E == Nags
	<-
	println("GPUs ready").		

+!waitAllGPUsReady: true
	<- .wait(50); 
	!waitAllGPUsReady.

+!waitRoundController : ready[source(roundController1)]
       <-
	println("Round controller ready").		

+!waitRoundController : true
	<- .wait(50); 
	!waitRoundController.
	
	
+!freeAll : true
	<-
        .send(roundController1, achieve, freeGPU);
	!consut_type_count("worker", Count);				 		
        for (.range(X,1,Count))
	{
	    .concat("worker",X,NAg);
	    .send(NAg, achieve, freeGPU);
	}.		
		

+!waitSaveWorkers: numAgents(Nags) & .count(saved(Name),E) & E == Nags
	<-
	println("All workers saved leftovers").		
		
+!waitSaveWorkers: true
	<- .wait(50); 
	!waitSaveWorkers.
