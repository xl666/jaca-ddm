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
		
		.concat("vfdt", Name, VFName);
		?data(File);
		feedExamplesFromFile(File, Atts)[artifact_id(IdBase)];
		.term2string(Attt, Atts); //meta info
		-+Attt;
		?atts(N, A); 
		makeArtifact(VFName,"artifacts.VFDT",[b(B, a(N, A))], IdVFDT);
		focus(IdVFDT);
		linkArtifacts(IdBase,"portExa1", IdVFDT);
		!register_artifact(VFName); //so other agents can send their model here
		+vfdtName(VFName);
		.send(contactPerson1, tell, ready(Name, VFName));
		if(Name == worker1)
		{
			.send(contactPerson1, tell, atts(N, A));
		}.
		
		
+!trainAndSendModel(To): true
	<-
		?vfdtName(From);
		sendAllExamples; //train with all
		!send_model(From, To);
		.send(contactPerson1, tell, finish).

