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
		?current_wsp(_,WName,_);
		.my_name(Name);
		.concat("base", Name, ArName);
		println("helloWorld",  WName);
		makeArtifact(ArName,"artifacts.InstancesBase",[],IdBase);
		?data(File);
		!artifact_ready(ArName, ID);
		feedExamplesFromFile(File, Atts)[artifact_id(ID)];
		.term2string(Attt, Atts);
		-+Attt;
		?atts(N, A); 
		makeArtifact("vfdt","artifacts.VFDT",[b(B, a(N, A))], IdVFDT);
		focus(IdVFDT);
		linkArtifacts(IdBase,"portExa1", IdVFDT); 
		sendAllExamples;
		.send(experimenter, tell, finish("vfdt")). // finish message, where is the result model

//Wait until the artifact is ready 
+!artifact_ready(ArtName,Id)
	<-	lookupArtifact(ArtName,Id).		
	
-!artifact_ready(ArtName,Id)
	<-	.wait(50);
		!artifact_ready(ArtName,Id).

