import os
import numpy as np
import pandas as pd



# config.csv:
# agentPairs;maxTurns;roundsPlayed;beta;entropyThreshold;
# order;costs;initiatorDistribution;responderDistribution
def read_config(location, config_title):
	filename = location + "config" + config_title + ".csv"
	with open(filename) as f:
		config = pd.read_csv(filename, sep=";")

	return config


# results_rounds.csv: 
# pair;round;nrTurns;success
def read_results_rounds(location, config_title):
	filename = location + "results_rounds" + config_title + ".csv"
	with open(filename) as f:
		results_rounds = pd.read_csv(filename, sep=";")
	
	return results_rounds


# results_turns.csv: 
# pair;round;turn;initiatorIntention;initiatorSignal;
# responderInference;responderSignal;entropyInitiatorListen;entropyResponderListen;
# entropyInitiatorLexicon;entropyResponderLexicon;KLDivItoR;KLDivRtoI
def read_results_turns(location, config_title):
	filename = location + "results_turns" + config_title + ".csv"
	with open(filename) as f:
		results_turns = pd.read_csv(filename, sep=";")

	return results_turns


