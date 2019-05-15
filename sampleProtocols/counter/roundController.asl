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

//can only be one agent of this type, it is really recomended to have this agent in the same site as the contactPerson to optimize data comunication

+begin : true
   <-
   makeArtifact("evaluatorController","artifacts.Evaluator",[],IdEval);
   focus(IdEval);
   !register_artifact("evaluatorController");
   +pastAccuracy(0); //to have a reference with the past evaluation
   ?site(MySite);
   .broadcast(tell, tellMeInstanceBase(MySite)). //the agent needs to know where are the examples for training in order to  use a part for internal testing


+myIB(IB): true
   <-
   ?param("Test Percentaje For Rounds", TPFR);
   .my_name(Name);
   .concat("base", Name, ArName);
    lookupArtifact("evaluatorController", IdEval);
    lookupArtifact(IB, IDIB);
    sendHeader(IdEval)[artifact_id(IDIB)]; //so the new InstancesBase can have the metadata
    sendPercentageOfExamples(IdEval, TPFR)[artifact_id(IDIB)]; //for testing 
    .send(contactPerson1, tell, ready).								     



+readyToEvaluate: true //this signal trigers when a new model is loaded for evaluation
	<-
	evaluate(PcCorrect, NC, NI, TEx);
        ?pastAccuracy(PAcc);
        ?param("Change step", CS);
	println(PcCorrect);
	Rest = (PcCorrect - PAcc);
	R2 = (PAcc - PcCorrect); //lower bound
	println("MENSAAAAAAAJEEEEEEEEEEEEEEEE!!!!!!!!!!!!!");
	println(Rest);
	if((Rest > CS) |  (R2 > CS))   //if current result keeps getting better or if the current stimate is much worse (it may happen), continue 
	{
	    -+pastAccuracy(PcCorrect);
	    .send(contactPerson1, tell, continue);
	}
	else
	{
	    .send(contactPerson1, tell, dontContinue);
	}.
	
