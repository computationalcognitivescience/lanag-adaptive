package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

import scala.util.Random
import java.io._

/*
run experiment with settings:
- beta of 2, 5, 10, 20
- many pairs of agents (500?)
- start distribution for agents - .5 / .45 - .55 / .4 - .6
- entropyThreshold?
- lexicon size?
- seed for the lexicons?

Store all output: same as in main.simulation, but name files with parameters
*/
object RunExperiment extends App{

	val signals = Set("S1", "S2", "S3").map(StringSignal)
	val referents = Set("R1", "R2").map(StringReferent)
	val allLexicons = Lexicon.allConsistentLexicons(signals, referents)

	util.Random.setSeed(1000L)

	val nrPairs = 500
	val maxTurns = 6
	val nrRounds = 6
	val entropyThreshold = 0.8.toBigNatural
	val order = 1
	val costs = 0.toBigNatural
	val betaOptions = List(2.toBigNatural, 5.toBigNatural, 10.toBigNatural, 20.toBigNatural)
	val distributionOptions = List(0.5, 0.45, 0.4)


	for (beta <- betaOptions){
		for (distribution <- distributionOptions){
			val lexiconPriorInitiator = allLexicons.binomialDistribution(BigNatural(distribution))
			val lexiconPriorResponder = allLexicons.binomialDistribution(BigNatural(1-distribution))
			val firstReferent = referents.toList(Random.nextInt(referents.size))
			val interactionsParallelized = (for(pair <- 0 until nrPairs) yield {
				val initiator = Initiator(
					order,
					signals,
					referents,
					firstReferent,
					None,
					List.empty,
					allLexicons,
					lexiconPriorInitiator,
					signals.uniformDistribution,
					referents.uniformDistribution,
					signals.map(_ -> costs).toMap,
					beta,
					entropyThreshold)

				val responder = Responder(
					order,
					signals,
					referents,
					List.empty,
					allLexicons,
					lexiconPriorResponder,
					signals.uniformDistribution,
					referents.uniformDistribution,
					signals.map(_ -> costs).toMap,
					beta,
					entropyThreshold)

				val interaction = AdaptiveInteraction(referents, initiator, responder, maxTurns, nrRounds)

				pair -> interaction
			}).toParArray

			val allData = interactionsParallelized.map(interaction => interaction._1 -> interaction._2.toList)
				.toList

			val parameters = s"a${nrPairs}_b${beta}_d$distribution"

			val pwt = new PrintWriter(new File("output/datafiles/results_turns_" + parameters + ".csv" ))
			pwt.println("pair;round;turn;initiatorIntention;initiatorSignal;responderInference;responderSignal;entropyInitiatorListen;entropyResponderListen;entropyInitiatorLexicon;entropyResponderLexicon;KLDivItoR;KLDivRtoI")
			allData.foreach(pairData => {
				val pair: Int = pairData._1
				val rounds: List[InteractionData] = pairData._2
				rounds.indices.foreach(round => {
					val roundData: InteractionData = rounds(round)
					val turn0i = roundData.initialInitiatorData
					val turn0r = roundData.responderData.head
					pwt.println(s"$pair;$round;0;${turn0i.intendedReferent};${turn0i.signal.toString};${turn0r.inferredReferent};${turn0r.signal.toString};NA;${turn0r.listenEntropy};${turn0i.lexiconEntropy};${turn0r.lexiconEntropy};${roundData.klInitItoR};${roundData.klInitRtoI}")

					val restTurnsI = roundData.initiatorData
					val restTurnsR = roundData.responderData.tail
					for(turn <- 0 until math.max(restTurnsI.size, restTurnsR.size)) {
						if(restTurnsI.isDefinedAt(turn) && restTurnsR.isDefinedAt(turn)) {
							val turni = restTurnsI(turn)
							val turnr = restTurnsR(turn)
							val turnklItoR = roundData.klInitiatorToResponder(turn)
							val turnklRtoI = roundData.klResponderToInitiator(turn)
							pwt.println(s"$pair;$round;$turn;${turni.intendedReferent};${turni.signal.toString};${turnr.inferredReferent};${turnr.signal.toString};${turni.listenEntropy};${turnr.listenEntropy};${turni.lexiconEntropy};${turnr.lexiconEntropy};$turnklItoR;$turnklRtoI")
						} else {
							val turni = restTurnsI(turn)
							pwt.println(s"$pair;$round;$turn;${turni.intendedReferent};${turni.signal.toString};NA;NA;${turni.listenEntropy};NA;${turni.lexiconEntropy};NA;NA;NA")
						}
					}
				})
			})
			pwt.close()

			val pwr = new PrintWriter(new File("output/datafiles/results_rounds_" + parameters + ".csv"))
			pwr.println("pair;round;nrTurns;success")
			allData.foreach(pairData => {
				val pair: Int = pairData._1
				val rounds: List[InteractionData] = pairData._2
				rounds.indices.foreach(round => {
					val nrTurns = rounds(round).initiatorData.length + 1
					val success = rounds(round).initialInitiatorData.intendedReferent == rounds(round).responderData.last.inferredReferent
					pwr.println(s"$pair;$round;$nrTurns;$success")
				})
			})
			pwr.close()

			val pwc = new PrintWriter(new File("output/datafiles/config_" + parameters + ".csv"))
			pwc.println("agentPairs;maxTurns;roundsPlayed;beta;entropyThreshold;order;costs;initiatorDistribution;responderDistribution")
			pwc.println(s"$nrPairs;$maxTurns;$nrRounds;$beta;$entropyThreshold;$order;$costs;$distribution;${1-distribution}")
			pwc.close()

		}
	}
}
