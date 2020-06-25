package com.markblokpoel.lanag.adaptive.agents

import com.markblokpoel.lanag.adaptive.atoms.{Lexicon, MetaSignal, StringReferent, StringSignal}
import com.markblokpoel.lanag.adaptive.storage.ResponderData
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class ExplicitResponder (order: Int,
												 signals: Set[StringSignal],
												 referents: Set[StringReferent],
												 history: List[(StringSignal, StringReferent)],
												 allLexicons: Set[Lexicon],
												 lexiconPriors: Distribution[Lexicon],
												 signalPriors: Distribution[StringSignal],
												 referentPriors: Distribution[StringReferent],
												 signalCosts: Map[StringSignal, BigNatural],
												 beta: BigNatural,
												 entropyThreshold: BigNatural)
	extends ExplicitAgent(order, history ,allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta) {

	override def listenAndRespond(observedSignal: StringSignal): (MetaSignal, ExplicitResponder, ResponderData) = {
		// listen part
		val posteriorReferentDistribution = l.pr(observedSignal)
		val listenEntropy = posteriorReferentDistribution.entropy
		val inferredReferent = posteriorReferentDistribution.sample
		val explicitReferent = inferredReferent //TODO: access this properly

		if(listenEntropy <= entropyThreshold) {
			// I'm quite certain I understood what you intended. We're done.
			(MetaSignal(None), this, ResponderData(inferredReferent, MetaSignal(None), listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy))
		} else {
			// I'm not quite certain, I'm gonna try to confirm
			// speak part
			val posteriorSignalDistribution = s.pr(inferredReferent)
			val inferredSignal = posteriorSignalDistribution.sample
			val metaSignal = MetaSignal(Some(inferredSignal))

			val updatedResponder = ExplicitResponder(order, signals, referents, (observedSignal, explicitReferent) :: history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

			val responderData = ResponderData(inferredReferent, metaSignal, listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy)

			(metaSignal, updatedResponder, responderData)
		}
	}
}

case object ExplicitResponder {
	def apply(order: Int,
						signals: Set[StringSignal],
						referents: Set[StringReferent],
						history: List[(StringSignal, StringReferent)],
						allLexicons: Set[Lexicon],
						beta: BigNatural,
						entropyThreshold: BigNatural): ExplicitResponder = {
		val signalPriors = signals.uniformDistribution
		val referentPriors = referents.uniformDistribution
		val lexiconPriors = allLexicons.uniformDistribution
		val signalCosts = signals.map(_ -> 0.toBigNatural).toMap
		ExplicitResponder(order, signals, referents, history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
	}
}
