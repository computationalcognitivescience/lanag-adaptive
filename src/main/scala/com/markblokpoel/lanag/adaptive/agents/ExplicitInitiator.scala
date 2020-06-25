package com.markblokpoel.lanag.adaptive.agents

import com.markblokpoel.lanag.adaptive.atoms.{Lexicon, MetaSignal, StringReferent, StringSignal}
import com.markblokpoel.lanag.adaptive.storage._
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class ExplicitInitiator(order: Int,
														 signals: Set[StringSignal],
														 referents: Set[StringReferent],
														 intendedReferent: StringReferent,
														 previousSignal: Option[StringSignal],
														 history: List[(StringSignal, StringReferent)],
														 allLexicons: Set[Lexicon],
														 lexiconPriors: Distribution[Lexicon],
														 signalPriors: Distribution[StringSignal],
														 referentPriors: Distribution[StringReferent],
														 signalCosts: Map[StringSignal, BigNatural],
														 beta: BigNatural,
														 entropyThreshold: BigNatural)
extends ExplicitAgent(order, history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta) {

	def nextIntention(intention: StringReferent): ExplicitInitiator =
		ExplicitInitiator(order, signals, referents, intention, None, history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

	def initialSpeak: (MetaSignal, ExplicitInitiator, InitialInitiatorData) = {
		val posteriorSignalDistribution = s.pr(intendedReferent)

		// we ignore entropy
		val inferredSignal = posteriorSignalDistribution.sample

		val metaSignal = MetaSignal(Some(inferredSignal))
		val updatedAgent = ExplicitInitiator(order, signals, referents, intendedReferent, Some(inferredSignal), history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
		val initiatorData = InitialInitiatorData(intendedReferent, metaSignal, lexiconLikelihoodDistribution.entropy)

		(metaSignal, updatedAgent, initiatorData)
	}

def listenAndRespond(observedSignal: StringSignal, explicitReferent: StringReferent): (MetaSignal, ExplicitInitiator, InitiatorData) = {
		require(previousSignal.isDefined, "[ExplicitInitiator] Cannot listenAndRespond when I have no previousSignal.")

		// listen part
		val posteriorReferentDistribution = l.pr(observedSignal)
		val listenEntropy = posteriorReferentDistribution.entropy
//		val inferredReferent = posteriorReferentDistribution.sample

		if(listenEntropy <= entropyThreshold && explicitReferent == intendedReferent) {
			// I'm quite certain of the inference and I believe we have mutual understanding. We're done
			(MetaSignal(None), this, InitiatorData(intendedReferent, explicitReferent, MetaSignal(None), listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy))
		} else {
			// I believe I was misunderstood, or I don't really understand you.
			// speak part
			val intermediateAgent = ExplicitInitiator(order, signals, referents, intendedReferent, None, (previousSignal.get, explicitReferent) :: history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
			//      println("Speak\n"+intermediateAgent.s)
			val posteriorSignalDistribution = intermediateAgent.s.pr(intendedReferent)
			// we ignore entropy
			val inferredSignal = posteriorSignalDistribution.sample
			val metaSignal = MetaSignal(Some(inferredSignal))
			val updatedAgent = ExplicitInitiator(order, signals, referents, intendedReferent, Some(inferredSignal), (previousSignal.get, explicitReferent) :: history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
			val initiatorData = InitiatorData(intendedReferent, explicitReferent, metaSignal, listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy)
			(metaSignal, updatedAgent, initiatorData)
		}
	}
}

case object ExplicitInitiator {
	def apply(order: Int,
						signals: Set[StringSignal],
						referents: Set[StringReferent],
						intendedReferent: StringReferent,
						previousSignal: Option[StringSignal],
						history: List[(StringSignal, StringReferent)],
						allLexicons: Set[Lexicon],
						beta: BigNatural,
						entropyThreshold: BigNatural): ExplicitInitiator = {
		val signalPriors = signals.uniformDistribution
		val referentPriors = referents.uniformDistribution
		val lexiconPriors = allLexicons.uniformDistribution
		val signalCosts = signals.map(_ -> 0.toBigNatural).toMap
		ExplicitInitiator(order, signals, referents, intendedReferent, previousSignal, history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
	}
}
