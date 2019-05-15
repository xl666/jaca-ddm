

+!load : true
	 <-
         ?param("Base Classifier", BN);
         ?param("Threads per node", TrNo);
         ?param("Counter Classifier", CoCa);
	 ?param("Stratify", Stratify);
         .my_name(Name);
	 .concat("insNormal", Name, ArName);
	  makeArtifact(ArName,"artifacts.InstancesBase",[],IdNormal);
	  +arNormal(ArName);
	  ?data(File);
	  feedExamplesFromFile(File, Atts)[artifact_id(IdNormal)];
	  .term2string(Attt, Atts); //meta info
	  -+Attt;
	  ?atts(N, A);
	 .concat("base", Name, BaseName);
	  makeArtifact(BaseName,"artifacts.ClassifierTriplet",[b(B, a(N, A)), CoCa], IdBase);
	  +baseMod(BaseName);
	  !register_artifact(BaseName); //to be able to send it
	  sendAllExamples(IdBase)[artifact_name(ArName)];
	 .concat("insMate", Name, ArMate);
	  ?atts(N, A); //header
	  makeArtifact(ArMate,"artifacts.InstancesBase",[b(B, a(N, A))],IdMate);	  
	  !register_artifact(ArMate); //to transfer instances
	  +arMate(ArMate);
	  induceBase(TrNo, BN, CoCa, Stratify)[artifact_name(BaseName)];
	  .send(contactPerson1, tell, ready(Name, BaseName)).

//send all examples to mate so it can search for counters
+pair(Mate, Third): true
	<-
        +third(Third);
        .send(Mate, askOne, arMate(XX), arMate(ArM));
	 ?arNormal(ToSend);
	 //retrieve artifactID of ArM
	 ?ipServer(IPSer); //all agents have this believe
	 joinRemoteWorkspace("default",IPSer,_); //go to the server
	 getArtifactId(ArM, ArMId); //use directory	
	 quitWorkspace; //return to previous workspace
	 sendAllExamples(ArMId)[artifact_name(ToSend)];
	 .send(Mate, tell, sendCompleted).

//search for counters
+sendCompleted: true
	<-
        ?arMate(ArMate);
        !wait4Third; 
        ?thirdInfo(ArC, ArN, ArModel);
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	//getArtifactId(ArC, IdC); //use directory
	getArtifactId(ArN, IdN); //use directory
	getArtifactId(ArModel, IdModel); //use directory
	quitWorkspace; //return to previous workspace
	?baseMod(BaseMod);
	!send_model(BaseMod, ArMate); //to search counters
	?param("Threads per node", TrNo);
	parallelSearchSendContradictions(IdModel, TrNo, ContsFound)[artifact_name(ArMate)];
	sendAllExamples(IdN)[artifact_name(ArMate)]; //consistent
	sendModel(IdModel)[artifact_name(BaseMod)];
	?third(Third);
	.my_name(Name);
	.send(Third, tell, done(Name)).

 //create triplet
+pairTriplet(Left, Rigth): true
	<-
        .send(Left, askOne, atts(XX1, XX2), atts(N, A));
	-+atts(N, A); 
	?param("Counter Classifier", CC);
        .my_name(Name);
	.concat("CounterMod", Name, ArCounter);
	 makeArtifact(ArCounter,"artifacts.ClassifierTriplet",[b(B, a(N, A)), CC], IdCounter);
	 +baseMod(ArCounter);
	//!register_artifact(ArCounter);
	.concat("insNormal", Name, ArNormal);
	.concat("insMate", Name, ArMate);
	.concat("insCounter", Name, ArC);
	 makeArtifact(ArNormal,"artifacts.InstancesBase",[b(B, a(N, A))],IdNormal);
	 //!register_artifact(ArNormal);	 
	 makeArtifact(ArMate,"artifacts.InstancesBase",[b(B, a(N, A))],IdMate);
	 makeArtifact(ArC,"artifacts.InstancesBase",[b(B, a(N, A))],IdC);
	 +arNormal(ArNormal);
	 +arMate(ArMate);
	 +arCounter(ArC);
	 //!register_artifact(ArMate);
	?ipServer(IPSer); //all agents have this believe
	joinRemoteWorkspace("default",IPSer,_); //go to the server
	registerArtifactId(ArCounter, IdCounter);
	registerArtifactId(ArNormal, IdNormal);
	registerArtifactId(ArMate, IdMate);
	registerArtifactId(ArC, IdC);
	quitWorkspace;
	.send(Left, tell, thirdInfo(ArC, ArNormal, ArCounter));
	.send(Rigth, tell, thirdInfo(ArC, ArNormal, ArCounter));
	!makeCounter.
	 
+!makeCounter : true
	<-
        !wait4Pairs;
        ?baseMod(BaseName);
        induce[artifact_name(BaseName)];
	println("Counter model ready");
	.my_name(Name);
	.send(contactPerson1, tell, ready(Name, BaseName)).


+!wait4Pairs: .count(done(_),E) & E == 2
	<-
	println("Inducing counter").	
		
+!wait4Pairs: true
		<-
        .wait(50); 
	!wait4Pairs.


+!wait4Third: .count(thirdInfo(_, _ ,_),E) & E == 1
	<-
	println("Third ready").	
		
+!wait4Third: true
		<-
        .wait(50); 
	!wait4Third.
