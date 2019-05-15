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


+!start: true
	<-
		.my_name(Name);
		.concat("base", Name, ArName);
		makeArtifact(ArName,"artifacts.InstancesBase",[],IdBase);
		focus(IdBase);
		!register_artifact(ArName); 
		+myInstancesBase(ArName);
		+instanceBaseID(IdBase);
		?data(File);
		feedExamplesFromFile(File, Atts)[artifact_id(IdBase)];		
		.term2string(Attt, Atts); //meta info
		-+Attt;
		?atts(N, A); 
		
		if(Name == worker1) //ther is always at least one worker
		{
			.send(contactPerson1, tell, atts(N, A));
		};
		.send(contactPerson1, tell, ready(Name, ArName)).
		

+!initGPU: true //initialize until round controlled has extracter its share
	  <-
	  ?instanceBaseID(IdBase);
          initializeGPU[artifact_id(IdBase)]; //initialize GPU
           .my_name(Name);
	  .send(contactPerson1, tell, gpuready(Name)).

+modelReceived: true //each time it receives a model
        <-
	?j48Model(J48);
        ?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getArtifactId(J48, J48ID); //use directory	
	quitWorkspace;
	?instanceBaseID(IdBase);
	//dumpInstances2File("/tmp/uglyComplete.arff")[artifact_id(IdBase)];
        searchSendContradictionsGPU(J48ID, ContsFound)[artifact_id(IdBase)];
	//nullOperation; //to crasshh
	.my_name(Name);
	.send(contactPerson1, tell, finish(Name, ContsFound)).
	
+!sendPercentage: true
      <-
      ?j48Model(J48);
        ?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	getArtifactId(J48, J48ID); //use directory	
	quitWorkspace;
	?param("Init Percentage", Per);
	?instanceBaseID(IdBase);
	sendPercentageOfExamples(J48ID, Per)[artifact_id(IdBase)];
	.send(contactPerson1, tell, initFinish).
      
+tellMeInstanceBase(SiteSource)[source(Ag)]: true
     <-
     ?site(MySite);
     if(MySite == SiteSource)
     {
	 ?myInstancesBase(MIB);
	 .send(Ag, tell, myIB(MIB));
     }.
   
+!freeGPU: true
	<-
	?instanceBaseID(IdBase);
	freeGPU[artifact_id(IdBase)].
