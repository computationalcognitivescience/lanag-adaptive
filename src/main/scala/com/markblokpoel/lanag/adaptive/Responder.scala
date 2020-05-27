package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class Responder(order: Int,
                     signals: Set[StringSignal],
                     referents: Set[StringReferent],
                     history: List[(StringSignal, StringSignal)],
                     lexiconPriors: Distribution[Lexicon],
                     signalPriors: Distribution[StringSignal],
                     referentPriors: Distribution[StringReferent],
                     signalCosts: Map[StringSignal, BigNatural],
                     beta: BigNatural,
                     entropyThreshold: BigNatural)
  extends AdaptiveAgent(order, signals, referents, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta) {

  override def listenAndRespond(observedSignal: StringSignal): (MetaSignal, Responder, ResponderData) = {
    // listen part
    val posteriorReferentDistribution = l.pr(observedSignal)
    val listenEntropy = posteriorReferentDistribution.entropy
    val inferredReferent = posteriorReferentDistribution.sample

    if(listenEntropy <= entropyThreshold) {
      // I'm quite certain I understood what you intended. We're done.
      (MetaSignal(None), this, ResponderData(inferredReferent, MetaSignal(None), listenEntropy))
    } else {
      // I'm not quite certain, I'm gonna try to confirm
      // speak part
      val posteriorSignalDistribution = s.pr(inferredReferent)
      val inferredSignal = posteriorSignalDistribution.sample
      val metaSignal = MetaSignal(Some(inferredSignal))

      val updatedResponder = Responder(order, signals, referents, (observedSignal, inferredSignal) :: history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

      val responderData = ResponderData(inferredReferent, metaSignal, listenEntropy)

      (metaSignal, updatedResponder, responderData)
    }
  }
}