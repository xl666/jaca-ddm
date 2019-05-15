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
	
		!consut_type_count("sender", Count);				 		
		for (.range(X,1,Count))
		{
			.concat("sender",X,NAg);
			.send(NAg, achieve, start);
		}.
					

+atts(N,A)[source(sender1)] : true //receives meta info about dataset
	<-
		!begin.



+!begin: true
	<- 
		!consut_type_count("sender", Count);
		+numAgents(Count);
		!waitAllReady;
		?atts(N,A); 
		?param("Pruning", ParamVal);
		?param("Classifier",  Classifier);
		if(Classifier == "VFDT")
		{
		    makeArtifact("coordinatorj48","artifacts.VFDT",[b(B, a(N, A))], IdJ48);
		}
		else
		{
		    makeArtifact("coordinatorj48","artifacts.ClassifierJ48",[b(B, a(N, A)), ParamVal], IdJ48);
		};
		!register_artifact("coordinatorj48"); 
		//begin

		for (.range(X,1,Count))
		{
			.concat("sender",X,NAg);
			.send(NAg, achieve, sendExamples("coordinatorj48"));
		};
		!waitAllFinish;
		induce;
		.send(experimenter, tell, finish("coordinatorj48")).

						

+ready(Name)[source(Ag)]: true
	<-
	+agent(Name). //to know the name of the agents	

+!waitAllReady: numAgents(Nags) & .count(ready(Name),E) & E == Nags
	<-
	println("All agents are ready").		
		
+!waitAllReady: true
	<- .wait(50); 
	!waitAllReady.

+!waitAllFinish: numAgents(Nags) & .count(finish(Name),E) & E == Nags
	<-
	println("All agents have finished").		
		
+!waitAllFinish: true
	<- .wait(50); 
	!waitAllFinish.
