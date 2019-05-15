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
	
		!consut_type_count("splitter", Count);

		for (.range(X,1,Count))
		{
			.concat("splitter",X,NAg);
			.send(NAg, achieve, start);
		};

		!begin.
					


+!begin: true
	<- 
		!consut_type_count("splitter", Count);
		+numAgents(Count);
		!waitAllReady;
		makeArtifact("central","artifacts.ClassifierSplitter",[], IdCentral);
		!register_artifact("central"); 
		//begin

		for (.range(X,1,Count))
		{
			.concat("splitter",X,NAg);
			.send(NAg, achieve, build("central"));
		};
		!waitAllFinish;
		.send(experimenter, tell, finish("central")).

						

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
