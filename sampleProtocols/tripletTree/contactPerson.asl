
+begin_process: true
		<-
                !consut_type_count("worker", Count);
                +numAgents(Count);
		+stack([]);
		!rounds(Count, Rounds); //how many rounds
		+remainingRounds(Rounds);
		for (.range(X,1,Count))
		{
			.concat("worker",X,NAg);
			.send(NAg, achieve, load);
		};
		!newIteration.
		

+!newIteration: remainingRounds(Rounds) & Rounds > 0
	<-
        !waitAllReady;
	?remainingRounds(RR);
        -+remainingRounds(RR-1);
	-+numAgents(0);
	!fillStack;
	.abolish(ready(_, _)[source(_)]); //cleaning
	!fixPairs;
	!newIteration.

+!newIteration: remainingRounds(Rounds) & Rounds == 0
        <-
        !waitAllReady;
        ?ready(Ag, ArModel); //there should be only one
	?site(MySite);
        .send(Ag, askOne, site(X), site(Site));
	 if(not(Site == MySite)) //one element can not be processed in this round
	{	    
	   .send(experimenter, tell, finish(Ag, ArModel));
	}
	else
	{
            .send(experimenter, tell, finish(ArModel));
	 }.

+!fixPairs: true
        <-				       
	?stack([HQ | TQ]);
	-+newStack([]);
	.length([HQ | TQ], Len);
	Res = Len mod 2;
	if(Res == 1) //one element can not be processed in this round
	{
	    -+newStack([HQ]);
	    -+stack(TQ);
	};
	?stack(Q);
	-+previous("");
	-+cont(1);
	for (.member(X,Q))
	{
	    ?cont(C);
	    RRR = C mod 2;
	    if(RRR == 0)
	    {
		?previous(Pr);
		?numAgents(NA);
		-+numAgents(NA+1);
		.send(Pr, askOne, site(SH), site(Client));
		//create new agent
		.send(experimenter, askOne, spawnNewAgent("worker", Client, ZZZ), spawnNewAgent(Dummy, Client, NName));
		.send(X, tell, pair(Pr, NName));
		.send(Pr, tell, pair(X, NName));
		.send(NName, tell, pairTriplet(Pr, X));
	    };
	    -+cont(C+1);
	    -+previous(X);
	};
	?newStack(NS);
	-+stack(NS).
				       
+!fillStack: true
        <-
	for (ready(X, _))
	{
	    ?stack(Q);
	    .concat([X], Q, NQ); 
	    -+stack(NQ);
	}.			       
				       

+!waitAllReady: numAgents(Nags) & .count(ready(Name, ArModel),E) & E == Nags
	<-
	println("All workers are ready for a new round").	
		
+!waitAllReady: true
		<-
        .wait(50); 
	!waitAllReady.
		 
//manual log2 + ceiling
+!rounds(Count, Res): true 
	<-
	!rounds(Count, 0, Res).
+!rounds(1, Ac, Ac): true.
+!rounds(Count, Ac, Res): true
	<-
	Num1 = Count/2;
	Num2 = Num1 mod 1;
	Num = Num1 - Num2; //manual floor
	Den = Count mod 2;
	Ac2 = Ac + 1;
	New = Num + Den;	
	!rounds(New, Ac2, Res).

+!waitWSPChange(CW): current_wsp(WId,_,_) & CW == WId
	<-
        .wait(50); 
         !waitWSPChange(CW).

+!waitWSPChange(CW): true
	<-
        println("Joined to result site").
