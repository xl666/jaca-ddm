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
		makeArtifact("vfdtlinker","artifacts.VFDT",[b(B, a(N, A))], IdVFDT);
		focus(IdVFDT);
		!register_artifact("vfdtlinker");
		!begin.


+modelReceived: true //this is the final model
	<-
		.send(experimenter, tell, finish("vfdtlinker")).





+!begin: true
	<- 
		!consut_type_count("worker", Count);
		+numAgents(Count);
		!waitAllReady;
		!createRoundList;		
		//begin
		?correspondance([c(NAg, To) | T]);
		-+correspondance(T);
		.send(NAg, achieve, trainAndSendModel(To)).
		
+finish[source(Ag)]: correspondance([])
	<-
		println("Finish").

+finish[source(Ag)]: true
	<-
		?correspondance([c(NAg, To) | T]);
		-+correspondance(T);
		.send(NAg, achieve, trainAndSendModel(To)).
				

+ready(Name, VFDT)[source(Ag)]: true
	<-
	+agent(Name,VFDT). //to know the name of the agents	

+!waitAllReady: numAgents(Nags) & .count(ready(Name,VFDT),E) & E == Nags
	<-
	println("All agents are ready").		
		
+!waitAllReady: true
	<- .wait(50); 
	!waitAllReady.
	
+!createRoundList: true
	<-
	+lNames([]);
	+lTos([]);
	for(agent(Name, To))
	{
		?lNames(LNames);
		?lTos(LTos);
		.concat(LNames, [Name], LLNames);
		.concat(LTos, [To], LLTos);
		-+lNames(LLNames);
		-+lTos(LLTos);
	};
	!createRoundList2.
	
+!createRoundList2 : true
	<-	
	?lNames(LNamesX);
	?lTos(LTosX);
	.length(LNamesX, Count);	
	+correspondance([]);
	for (.range(X,0,Count-1))
	{
		.nth(X, LNamesX, NN);
		?correspondance(Corr);
		if(X+1 < Count)
		{			
			.nth(X+1, LTosX, NT);
			.concat(Corr, [c(NN,NT)], NCorr);
			-correspondance(Corr);
			+correspondance(NCorr);
		}
		else
		{
			.concat(Corr, [c(NN,"vfdtlinker")], NCorr);
			-correspondance(Corr);
			+correspondance(NCorr);
		};	
			
	}.
	
	
