import csv
from readResults import *
from matplotlib import pyplot as plt
import numpy as np
import pandas as pd
import scipy.stats as st

# not used because it causes chaotic data
def plot_succes_over_rounds(results_rounds, config, filename):
	success_in_turn = split_over_rounds(results_rounds['nrTurns'], config)
	# fig, ax = plt.subplots()
	for i in range(0, config['agentPairs']):
		ax.plot(success_in_turn[i])
	plt.xlabel("rounds")
	plt.ylabel("turns")
	plt.title("Success over rounds")
	plt.show()
	plt.savefig(savelocation + "succes_over_rounds" + filename + ".png")
	plt.close()

def total_success(location, savelocation, explicit, betas, distributions, filestarter):
	colours = ['r', 'g', 'b', 'c', 'm', 'y']
	for beta in betas:
		fig, ax = plt.subplots()
		n=1
		for dist in distributions:
			colour = colours[n]
			filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
			results_rounds = read_results_rounds(location, filename)
			# x = [0,1,2,3,4,5]
			yes = results_rounds.groupby('success').get_group(True)
			no = results_rounds.groupby('success').get_group(False)
			yes_grouped = yes.groupby('round')
			no_grouped = no.groupby('round')
			success_yes = yes_grouped['success'].count()
			success_no = no_grouped['success'].count()

			labels = ['0', '1', '2', '3', '4', '5']

			x = np.arange(len(labels))  # the label locations
			width = 0.2  # the width of the bars

			ax.bar(x + n*width, success_yes, width, label=str(dist) + ", Success")
			# ax.bar(x + n*(width), success_no, width, label=str(dist) + ", No Success")

			# ax.bar(x, yes_grouped.count(), color = colour)

			n += 1
		plt.xlabel("rounds")
		plt.ylabel("mean success rate")
		plt.ylim([0,500])
		plt.title("Successful conversations (beta = " + str(beta) + ")")
		plt.legend()
		savename = "_a500_b" + str(beta)
		plt.savefig(savelocation + "successful_conv_" + savename + ".png")
		plt.close()

# needs new splitting function
def plot_entropy_lexicons_over_rounds(results_turns, config, filename, beta, dist, savelocation):
	entropy_lexicons = results_turns.loc[:,['round', 'entropyInitiatorLexicon', 'entropyResponderLexicon']]
	grouped_entropy_lexicons = entropy_lexicons.groupby('round')

	average_initiator_lexicon = grouped_entropy_lexicons['entropyInitiatorLexicon'].agg(np.nanmean)
	std_initiator_lexicon = grouped_entropy_lexicons['entropyInitiatorLexicon'].agg(np.nanstd)

	x = [0,1,2,3,4,5]

	average_responder_lexicon = grouped_entropy_lexicons['entropyResponderLexicon'].agg(np.nanmean)
	std_responder_lexicon = grouped_entropy_lexicons['entropyResponderLexicon'].agg(np.nanstd)

	# fig, ax = plt.subplots()
	# init_lexicon = results_turns.loc[:,'round','entropyInitiatorLexicon']
	# init_lexicon_group = init_lexicon.groupby('round')
	# plt.boxplot(init_lexicon, label = 'initiator')
	plt.errorbar(x, average_initiator_lexicon, std_initiator_lexicon, label = 'initiator')
	plt.errorbar(x, average_responder_lexicon, std_responder_lexicon, label = 'responder')
	# plt.plot(average_initiator_lexicon, label = 'initiator')
	# plt.plot(average_responder_lexicon, label = 'responder')
	plt.xlabel("rounds")
	plt.ylabel("entropy")
	plt.title("Entropy lexicon over rounds(beta = " + str(beta) + " and dist of " + str(dist) + ")")
	# ax.axis([0, config['roundsPlayed'], 0.5,1.5])
	plt.legend()
	# plt.show()
	plt.savefig(savelocation + "plot_entropy_lexicons_over_rounds" + filename + ".png")
	plt.close()

def plot_average_succes_over_rounds(results_rounds, config, filename, beta, dist, savelocation):
	sub_rounds = results_rounds.loc[:,['round', 'nrTurns']]
	grouped = sub_rounds.groupby('round')

	average_success_over_turns = grouped['nrTurns'].agg(np.nanmean)
	std_succes_over_turns = grouped['nrTurns'].agg(np.nanstd)
	x = [0,1,2,3,4,5]
	# fig, ax = plt.subplots()
	plt.errorbar(x, average_success_over_turns, std_succes_over_turns)
	# plt.plot(average_success_over_turns)
	plt.xlabel("rounds")
	plt.ylabel("turns before end conversation")
	plt.ylim([0,7])
	plt.title("Average success over rounds (beta = " + str(beta) + " and dist of " + str(dist) + ")")
	# plt.show()
	plt.savefig(savelocation + "average_succes_over_rounds" + filename + ".png")
	plt.close()

def average_succes_over_rounds_single(location, savelocation, explicit, beta, dist, filestarter):
	fig, ax = plt.subplots()
	# for dist in distributions:
	filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
	results_rounds = read_results_rounds(location, filename)

	sub_rounds = results_rounds.loc[:,['round', 'nrTurns']]
	sub_rounds_2 = sub_rounds.loc[sub_rounds['nrTurns'] < 7]
	grouped = sub_rounds_2.groupby('round')
	x = [0,1,2,3,4,5]
	average_success_over_turns = grouped['nrTurns'].agg(np.nanmean)
	std_succes_over_turns = grouped['nrTurns'].agg(np.nanstd)
	# p025 = grouped['nrTurns'].quantile(0.025)
	# p975 = grouped['nrTurns'].quantile(0.975)
	# print(grouped['nrTurns'])
	# ci = st.t.interval(0.95, len(grouped['nrTurns'])-1, loc=average_success_over_turns, scale=st.sem(grouped['nrTurns']))
	
	# yerr=[average_success_over_turns - p025, p975 - average_success_over_turns],
	ax.errorbar(x, average_success_over_turns, std_succes_over_turns, label = str(dist))
		# ax.plot(x, average_success_over_turns, label = str(dist))
	plt.xlabel("rounds")
	plt.ylabel("turns before end conversation")
	plt.ylim([0,7])
	plt.title("Average nr turns over rounds (beta = " + str(beta) + ")")
	plt.legend()
	savename = "_a500_b" + str(beta)
	plt.savefig(savelocation + "single_turns_over_rounds_no_cut_off_test" + savename + ".png")
	plt.close()	

def full_average_success_over_rounds(location, savelocation, explicit, betas, distributions, filestarter):
	for beta in betas:
		fig, ax = plt.subplots()
		for dist in distributions:
			filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
			results_rounds = read_results_rounds(location, filename)

			sub_rounds = results_rounds.loc[:,['round', 'nrTurns']]
			grouped = sub_rounds.groupby('round')
			x = [0,1,2,3,4,5]
			average_success_over_turns = grouped['nrTurns'].agg(np.nanmean)
			std_succes_over_turns = grouped['nrTurns'].agg(np.nanstd)
			# p025 = grouped['nrTurns'].quantile(0.025)
			# p975 = grouped['nrTurns'].quantile(0.975)
			# print(grouped['nrTurns'])
			# ci = st.t.interval(0.95, len(grouped['nrTurns'])-1, loc=average_success_over_turns, scale=st.sem(grouped['nrTurns']))
			
			# yerr=[average_success_over_turns - p025, p975 - average_success_over_turns],
			ax.errorbar(x, average_success_over_turns, std_succes_over_turns, label = str(dist))
			# ax.plot(x, average_success_over_turns, label = str(dist))

		plt.xlabel("rounds")
		plt.ylabel("turns before end conversation")
		plt.ylim([0,7])
		plt.title("Average nr turns over rounds (beta = " + str(beta) + ")")
		plt.legend()
		savename = "_a500_b" + str(beta)
		plt.savefig(savelocation + "full_turns_over_rounds" + savename + ".png")
		plt.close()

def show_success_per_round(location, savelocation, explicit, betas, distributions, filestarter):
	colours = ['r', 'g', 'b', 'o']
	for beta in betas:
		fig, ax = plt.subplots()
		n=0
		for dist in distributions:
			colour = colours[n]
			filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
			results_rounds = read_results_rounds(location, filename)

			x = [0,1,2,3,4,5]

			yes = results_rounds.groupby('success').get_group(True)
			no = results_rounds.groupby('success').get_group(False)
			yes_grouped = yes.groupby('round')
			no_grouped = no.groupby('round')
			yes_average = yes_grouped['nrTurns'].agg(np.nanmean)
			no_average = no_grouped['nrTurns'].agg(np.nanmean)
			yes_std = yes_grouped['nrTurns'].agg(np.nanstd)
			no_std = no_grouped['nrTurns'].agg(np.nanstd)
			ax.plot(x, yes_average, label=str(dist) + ",yes", color = colour, linestyle='-')
			ax.plot(x, no_average, label=str(dist) + ",no", color = colour, linestyle = '--')
			# ax.errorbar(x, yes_average, yes_std, label=str(dist) + ",yes", color = colour, linestyle='-')
			# ax.errorbar(x, no_average, no_std, label=str(dist) + ",no", color = colour, linestyle='-')
			n += 1
		plt.xlabel("rounds")
		plt.ylabel("turns before end conversation")
		plt.ylim([0,7])
		plt.title("Average success over rounds (beta = " + str(beta) + ")")
		plt.legend()
		savename = "_a500_b" + str(beta)
		plt.savefig(savelocation + "full_average_succes_over_rounds_" + savename + ".png")
		plt.close()

def turns_to_succes(location, beta, dist, savelocation, explicit, filestarter):
	# for beta in betas:
	filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
	config = read_config(location, filename)
	results_rounds = read_results_rounds(location, filename)

	fig, ax = plt.subplots()
	yesses = results_rounds.groupby('success').get_group(True)
	noes = results_rounds.groupby('success').get_group(False)

	yes_turn = []
	no_turn = []

	for i in range(1,8):
	#assert group available, else replace with pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5])	
		try:	
			yes_turn.append(yesses.groupby('nrTurns').get_group(i).groupby('round').agg(np.size)['nrTurns'])
		except KeyError:
			yes_turn.append(pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5]))
		try:
			no_turn.append(noes.groupby('nrTurns').get_group(i).groupby('round').agg(np.size)['nrTurns'])
		except KeyError:
			no_turn.append(pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5]))
	
	# for each round: nr of turns averages
	# for each turn: success or not success
	x = np.arange(6)
	width = 1 / (len(yes_turn) + 1)
	plts = []
	for i in range(0, len(yes_turn)):
		for k in range(0,6):
			if yes_turn[i].get(k) == None:
				serie = pd.Series(0, index=[k])
				yes_turn[i] = yes_turn[i].append(serie)
		yes_turn[i] = yes_turn[i].sort_index()	
		for j in range(0,6):
			if no_turn[i].get(j) == None:
				serie = pd.Series(0, index=[j])
				no_turn[i] = no_turn[i].append(serie)
		no_turn[i] = no_turn[i].sort_index()	

		d = i * width
		plts.append(ax.bar(x + d, yes_turn[i], color = 'b', width = width))
		plts.append(ax.bar(x + d, no_turn[i], color = 'r', bottom=yes_turn[i], width = width))
		

	plt.xlabel("rounds")
	plt.ylabel("agents (n=500)")
	plt.ylim([0,500])
	if explicit:
		plt.title("Turns needed to end explicit conversation (beta = " + str(beta) + ", dist = " + str(dist) + ")")
	else:
		plt.title("Turns needed to end conversation (beta = " + str(beta) + ", dist = " + str(dist) + ")")
	plt.legend((plts[0][0], plts[1][0]), ('Success', 'No Success'))
	savename = "_a500_b" + str(beta) + "_d" + str(dist)
	plt.savefig(savelocation + "turns_to_succes" + savename + ".png")
	plt.close()

# first round goes wrong, then truth of values for other rounds
def successful_updating(results_rounds, filename, beta, dist, savelocation, explicit, filestarter):
	#get all rounds and success for: round 0, success == False
	# get agent pairs for case, then grab all info for these agent pairs
	# split data: .filter(success=False for round = 0)
	pairs_to_keep = results_rounds[(results_rounds['round'] == 0) & (results_rounds['success'] == False)].loc[:,'pair']
	new_data = results_rounds[results_rounds['pair'].isin(pairs_to_keep.array)]

	fig, ax = plt.subplots()
	yesses = new_data.groupby('success').get_group(True)
	noes = new_data.groupby('success').get_group(False)

	yes_turn = []
	no_turn = []

	#TODO: check this
	for i in range(1,8):
	#assert group available, else replace with pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5])	
		try:	
			yes_turn.append(yesses.groupby('nrTurns').get_group(i).groupby('round').agg(np.size)['nrTurns'])
		except KeyError:
			yes_turn.append(pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5]))
		try:
			no_turn.append(noes.groupby('nrTurns').get_group(i).groupby('round').agg(np.size)['nrTurns'])
		except KeyError:
			no_turn.append(pd.Series([0, 0, 0, 0, 0, 0], index=[0, 1, 2, 3, 4, 5]))
	
	# for each round: nr of turns averages
	# for each turn: success or not success
	x = np.arange(6)
	width = 1 / (len(yes_turn) + 1)
	plts = []
	for i in range(0, len(yes_turn)):
		for k in range(0,6):
			if yes_turn[i].get(k) == None:
				serie = pd.Series(0, index=[k])
				yes_turn[i] = yes_turn[i].append(serie)
		yes_turn[i] = yes_turn[i].sort_index()	
		for j in range(0,6):
			if no_turn[i].get(j) == None:
				serie = pd.Series(0, index=[j])
				no_turn[i] = no_turn[i].append(serie)
		no_turn[i] = no_turn[i].sort_index()	

		d = i * width
		plts.append(ax.bar(x + d, yes_turn[i], color = 'b', width = width))
		plts.append(ax.bar(x + d, no_turn[i], color = 'r', bottom=yes_turn[i], width = width))
		

	plt.xlabel("rounds")
	plt.ylabel("agents (n=" + str(len(pairs_to_keep.array)) +")")
	# plt.ylim([0,(len(pairs_to_keep.array)*1.1)])
	plt.ylim([0,500])
	if explicit:
		plt.title("Successful updating in explicit conversation (beta = " + str(beta) + ", dist = " + str(dist) + ")")
		savename = "_exp_a500_b" + str(beta) + "_d" + str(dist)
	else:
		plt.title("Successful updating in conversation (beta = " + str(beta) + ", dist = " + str(dist) + ")")
		savename = "_a500_b" + str(beta) + "_d" + str(dist)
	plt.legend((plts[0][0], plts[1][0]), ('Success', 'No Success'))
	plt.savefig(savelocation + "successful_updating" + savename + ".png")
	plt.close()


def main():
	cases = ['4x3', '4x3e', '3x2', '3x2e']
	for case in cases:
		if case == '4x3':
			location = "s4-r3-lanagadaptive/"
			savelocation = "fig/4x3/"
			explicit = False
		elif case == '3x2':
			location = "s3-r2-adaptive/"
			savelocation = "fig/3x2/"
			explicit = False
		elif case == '3x2e':
			location = "s3-r2-explicit/"
			savelocation = "fig/3x2-explicit/"
			explicit = True
		elif case == '4x3e':
			location = "s4-r3-explicit/"
			savelocation = "fig/4x3-explicit/"
			explicit = True

		if explicit == True:
			filestarter = "_exp_a500"
		else: filestarter = "_a500"
		print("Plotting case: " + str(case))

		betas = [2.0, 5.0, 10.0, 20.0]
		distributions = [0.4, 0.45, 0.5]


		average_succes_over_rounds_single(location, savelocation, explicit, 5.0, 0.45, filestarter)
		#TODO: fix the rest of code to be homogenous

		# full_average_success_over_rounds(location, savelocation, explicit, betas, distributions, filestarter)
		# show_success_per_round(location, savelocation, explicit, betas, distributions, filestarter)
		# # total_success(location, savelocation, explicit, betas, distributions, filestarter)
		# for beta in betas:
		# 	# print("b " + str(beta))
		# 	for dist in distributions:
		# 		# print("d " + str(dist))

		# 		filename = filestarter + "_b" + str(beta) + "_d" + str(dist)
		# 		turns_to_succes(location, beta, dist, savelocation, explicit, filestarter)			
		# 		# config = read_config(location, filename)
		# 		# results_turns = read_results_turns(location, filename)
		# 		results_rounds = read_results_rounds(location, filename)
		# 		successful_updating(results_rounds, filename, beta, dist, savelocation, explicit, filestarter)
		# 		# plot_succes_over_rounds(results_rounds, config, filename)
		# 		# plot_entropy_lexicons_over_rounds(results_turns,config, filename, beta, dist, savelocation)
		# 		# plot_average_succes_over_rounds(results_rounds, config, filename, beta, dist, savelocation)



main()

