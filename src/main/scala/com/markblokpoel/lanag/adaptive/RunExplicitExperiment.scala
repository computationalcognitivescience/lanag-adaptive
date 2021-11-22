package com.markblokpoel.lanag.adaptive

import java.io.{File, PrintWriter}

import com.markblokpoel.lanag.adaptive.agents.{ExplicitInitiator, ExplicitResponder}
import com.markblokpoel.lanag.adaptive.atoms.{Lexicon, StringReferent, StringSignal}
import com.markblokpoel.lanag.adaptive.storage.InteractionData
import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

import scala.util.Random

case object RunExplicitExperiment {
	/**
	 * Runs the ostensive simulations
	 *
	 *  Sets up the settings to run simulations
	 *  Runs the simulations
	 *  Saves datafiles of simulations automatically
	 *
	 * @param nrSignals
	 * @param nrReferents
	 * @param nrPairs
	 * @param maxTurns
	 * @param nrRounds
	 * @param entropyThreshold
	 * @param order
	 * @param costs
	 * @param betaOptions
	 * @param distributionOptions
	 * @param randomSeed
	 */
	def run(
					 nrSignals: Int,
					 nrReferents: Int,
					 nrPairs: Int,
					 maxTurns: Int,
					 nrRounds: Int,
					 entropyThreshold: BigNatural,
					 order: Int,
					 costs: BigNatural,
					 betaOptions: List[BigNatural],
					 distributionOptions: List[Double],
					 randomSeed: Option[Long] = None
				 ): Unit = {
		val signals = (1 to nrSignals).map(s => s"S$s").toSet.map(StringSignal)
		val referents = (1 to nrReferents).map(s => s"R$s").toSet.map(StringReferent)
		val allLexicons = Lexicon.allConsistentLexicons(signals, referents)

		if (randomSeed.isDefined) util.Random.setSeed(randomSeed.get)

		for (beta <- betaOptions) {
			for (distribution <- distributionOptions) {
				val lexiconPriorInitiator = allLexicons.binomialDistribution(BigNatural(distribution))
				val lexiconPriorResponder = allLexicons.binomialDistribution(BigNatural(1 - distribution))
				val firstReferent = referents.toList(Random.nextInt(referents.size))
				val interactionsParallelized = (for (pair <- 0 until nrPairs) yield {
					val initiator = ExplicitInitiator(
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

					val responder = ExplicitResponder(
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

					val interaction = ExplicitInteraction(referents, initiator, responder, maxTurns, nrRounds)

					pair -> interaction
				}).toParArray

				val allData = interactionsParallelized.map(interaction => interaction._1 -> interaction._2.toList)
					.toList

				val parameters = s"exp_a${nrPairs}_b${beta}_d$distribution"

				val pwt = new PrintWriter(new File("output/datafiles/results_turns_" + parameters + ".csv"))
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
						for (turn <- 0 until math.max(restTurnsI.size, restTurnsR.size)) {
							if (restTurnsI.isDefinedAt(turn) && restTurnsR.isDefinedAt(turn)) {
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
				pwc.println(s"$nrPairs;$maxTurns;$nrRounds;$beta;$entropyThreshold;$order;$costs;$distribution;${1 - distribution}")
				pwc.close()

			}
		}
	}
}

