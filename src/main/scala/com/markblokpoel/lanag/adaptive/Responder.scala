package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

case class Responder(order: Int,
                     signals: Set[StringSignal],
                     referents: Set[StringReferent],
                     history: List[(StringSignal, StringSignal)],
                     allLexicons: Set[Lexicon],
                     lexiconPriors: Distribution[Lexicon],
                     signalPriors: Distribution[StringSignal],
                     referentPriors: Distribution[StringReferent],
                     signalCosts: Map[StringSignal, BigNatural],
                     beta: BigNatural,
                     entropyThreshold: BigNatural)
  extends AdaptiveAgent(order, referents, history ,allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta) {

  override def listenAndRespond(observedSignal: StringSignal): (MetaSignal, Responder, ResponderData) = {
    // listen part
    val posteriorReferentDistribution = l.pr(observedSignal)
    val listenEntropy = posteriorReferentDistribution.entropy
    val inferredReferent = posteriorReferentDistribution.sample

    if(listenEntropy <= entropyThreshold) {
      // I'm quite certain I understood what you intended. We're done.
      (MetaSignal(None), this, ResponderData(inferredReferent, MetaSignal(None), listenEntropy, posteriorReferentDistribution))
    } else {
      // I'm not quite certain, I'm gonna try to confirm
      // speak part
      val posteriorSignalDistribution = s.pr(inferredReferent)
      val inferredSignal = posteriorSignalDistribution.sample
      val metaSignal = MetaSignal(Some(inferredSignal))

      val updatedResponder = Responder(order, signals, referents, (observedSignal, inferredSignal) :: history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

      val responderData = ResponderData(inferredReferent, metaSignal, listenEntropy, posteriorReferentDistribution)

      (metaSignal, updatedResponder, responderData)
    }
  }
}

case object Responder {
  def apply(order: Int,
            signals: Set[StringSignal],
            referents: Set[StringReferent],
            history: List[(StringSignal, StringSignal)],
            allLexicons: Set[Lexicon],
            beta: BigNatural,
            entropyThreshold: BigNatural): Responder = {
    val signalPriors = signals.uniformDistribution
    val referentPriors = referents.uniformDistribution
    val lexiconPriors = allLexicons.uniformDistribution
    val signalCosts = signals.map(_ -> 0.toBigNatural).toMap
    Responder(order, signals, referents, history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
  }
}