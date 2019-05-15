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

+!beginProcess: true
        <-
	!createPendantList;
        -+contradiction(false);
	?pendant([H | T]);
	-+pendant(T);
	.term2string(Ag, H);
	?agent(Ag, ArtifactName);
	!send_modelGPU("coordinatorj48", ArtifactName).
	
		
+!createPendantList: true
         <-
	 -+pendant([]);
         ?numAgents(Count);
	 for (.range(X,1,Count))
	 {
	     .concat("worker",X,NAg);
	     ?pendant(PList);
	     .concat(PList, [NAg], NPen);
	      -+pendant(NPen);
	 }.


+finish(AgName, ContraFound): pendant([])
         <-
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
	    .abolish(finish(_,_));
	    -+round(Round + 1);
        }
	else
	{
	    !freeAll;
	    .send(experimenter, tell, finish("coordinatorj48"));
	}.

//the round controller tells if you must continue
+continue[source(roundController1)]: true
     <-
     -continue[source(roundController1)]; // for the next time
     !beginProcess. //do another iteration

+dontContinue[source(roundController1)]: true
     <-
     !freeAll;
     .send(experimenter, tell, finish("coordinatorj48")). //finish


+finish(AgName, ContraFound): true
        <-
	induceGPU;
	if(ContraFound == true)
	{
	    -+contradiction(true);
	};
	?pendant([H | T]);
        -+pendant(T);
        .term2string(Ag, H);
	?agent(Ag, ArtifactName);
	!send_modelGPU("coordinatorj48", ArtifactName).
				

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
		
