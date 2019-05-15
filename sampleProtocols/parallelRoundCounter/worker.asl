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
		-+processInit(true); //this is a flag
		.my_name(Name);
		.concat("base", Name, ArName);
		makeArtifact(ArName,"artifacts.InstancesBase",[],IdBase);
		focus(IdBase);
		!register_artifact(ArName); 
		+myInstancesBase(ArName);
		.concat("baseContras", Name, ArName2);
		makeArtifact(ArName2,"artifacts.InstancesBase",[],IdBaseContras); //to save the contrdictions
		+idBase(IdBase);
		+idBaseContras(IdBaseContras);
		.concat("vfdt", Name, VFName);
		?data(File);
		feedExamplesFromFile(File, Atts)[artifact_id(IdBase)];
		.term2string(Attt, Atts); //meta info
		-+Attt;
		?atts(N, A); 
		makeArtifact(VFName,"artifacts.VFDT",[b(B, a(N, A))], IdVFDT);
		focus(IdVFDT);
		linkArtifacts(IdBase,"portExa2", IdBaseContras); 
		linkArtifacts(IdBase,"portExa1", IdVFDT);
		linkArtifacts(IdBaseContras,"portExa1", IdVFDT); //yea this two are also linked
		sendHeader(IdBaseContras)[artifact_id(IdBase)]; //this way the contra base knows the header and creates its instances object
		!register_artifact(VFName); //so other agents can send their model here
		+vfdtName(VFName);
		.send(contactPerson1, tell, ready(Name, VFName));
		if(Name == worker1)
		{
			.send(contactPerson1, tell, atts(N, A));
		}.
				

+!sendPercentage: true
      <-
      ?param("Init Percentage", Per);
      ?idBase(IdBase);
      sendPercentageOfExamples(Per)[artifact_id(IdBase)];
      .send(contactPerson1, tell, initFinish).


+modelReceived: processInit(true) //when the init model is received
     <-
     -+processInit(false);
     //begin to search contras
     ?idBase(IdBase);
     searchSendContradictionsMultiple(["portExa1", "portExa2"],ContraF)[artifact_id(IdBase)]; // the counter examples are saved
     .my_name(Name); 
     .send(contactPerson1, tell, searchFinished(Name, ContraF)).								    

+modelReceived: true //triggers when the round phase is on
	<-
	-+processInit(true).
	
//to send the init model
+!sendModelToList(ListArtifacts): true
     <-
     -+processInit(false); //to have the same behavior as if a model was received
     ?vfdtName(VFName);
     for ( .member(X, ListArtifacts) ) 
     {
	 !send_model(VFName, X);	
     }; 
     //this agent also has to search contradictions
     ?idBase(IdBase);
     searchSendContradictionsMultiple(["portExa1", "portExa2"],ContraF)[artifact_id(IdBase)]; // the counter examples are saved 
     .my_name(Name);
     .send(contactPerson1, tell, searchFinished(Name, ContraF)).								    

+!trainAndSendModel(To): true
	<-
		?vfdtName(From);
        ?idBaseContras(IdBaseContras);
		//sendAllExamples[artifact_id(IdBaseContras)]; //train with the counter examples
		searchSendContradictions(_)[artifact_id(IdBaseContras)];
		clearExamples[artifact_id(IdBaseContras)];
		!send_model(From, To);		
		.send(contactPerson1, tell, finish).

+!justSendModel(To): true
	<-
		?vfdtName(From);                
		!send_model(From, To);		
		.send(contactPerson1, tell, finish).

+tellMeInstanceBase(SiteSource)[source(Ag)]: true
     <-
     ?site(MySite);
     if(MySite == SiteSource)
     {
	 ?myInstancesBase(MIB);
	 .send(Ag, tell, myIB(MIB));
     }.
