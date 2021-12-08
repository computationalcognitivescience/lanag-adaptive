# Documentation Datafiles

The code produces three different output files per run: the config.csv, results_rounds.csv, and results_turns.csv

## config.csv

The config produces the configuration settings of the simulation in accessible format after running. It has the following columns: 

- **agentPairs** *(int)* The number of agent pairs that the simulation has run for.
- **maxTurns** *(int)* The maximum number of turns played per round.
- **roundsPlayed** *(int)* The number of interaction rounds played for each agent pair.
- **beta** *(float)* The beta (softmax) parameter.
- **entropyThreshold** *(float)* The entropy threshold set, this determines when the agents are certain enough to commit to a decision on a referent.
- **order** *(int)* The order of reasoning that the agents do.
- **costs** *(float)* The cost attributed to signals.
- **initiatorDistribution** *(float)* The value used to set the binomial distribution for the initator.
- **responderDistribution** *(float)* The value used to set the binomial distribution for the responder.


## results_rounds.csv

The results_rounds file contains an overview of the simulation: it contains, per agent pair, per round they interacted, the number of turns they needed to reach a conclusion, and whether that conclusion was a successful interaction or not. It has the following columns:

- **pair** *(int)* The index indicating which pair these results are for.
- **round** *(int)* The index indicating which round of interaction for this pair the results are for.
- **nrTurns** *(int)* The number of turns these agents took to reach a conclusion (or 7 if they gave up).
- **success** *(boolean: TRUE or FALSE)* Denotes the factual understanding these agents achieved.



## results_turns.csv
 
The results_turns file shows a more detailed view of the happenings in the simulation. It has the following columns:

- **pair** *(int)* The index indicating which pair these results are for.
- **round** *(int)* The index indicating which round of interaction for this pair the results are for.
- **turn** *(int)* The index indicating which turn of this round of interaction for this pair the results are for.
- **initiatorIntention** *(string)* The intention/referent the initiator means to communicate about.
- **initiatorSignal** *(string)* The signal the initator uses to communicate the intention. "I understand" is produced if the initiator believes understanding has been reached.
- **responderInference** *(string)* The referent that the responder infers from the signal received.
- **responderSignal** *(string)* The signal the responder uses to communicate back. "I understand" if the entropy threshold is reached.
- **entropyInitiatorListen** *(double)* The entropy value for the initiator after they have interpreted the signal sent before. "NA" if there was no previous signal (i.e. at the beginning of a round).
- **entropyResponderListen** *(double)* The entropy value for the responder after they have interpreted the signal sent before. "NA" if there was no previous signal (i.e. if the initiator ends the round).
- **entropyInitiatorLexicon** *(double)* The entropy over the initiator's lexical likelihood after their turn.
- **entropyResponderLexicon** *(double)* The entropy over the responder's lexical likelihood after their turn.
- **KLDivItoR** *(double)* The KL-divergence between initiator lexicon distribution and responder lexicon distribution for this turn.
- **KLDivRtoI** *(double)* The KL-divergence between responder lexicon distribution and initiator lexicon distribution for this turn.
- **posteriorInitiator** *( {(string -> double)} )* The posterior distribution over referents for the initiator, gives a value between 0 and 1 for each referent available. "NA" if there is no posterior computed (i.e. at the beginning of a round.)
- **posteriorResponder** *( {(string -> double)} )* The posterior distribution over referents for the responder, gives a value between 0 and 1 for each referent available. "NA" if there is no posterior computed (i.e. if the initiator ends the round.)

