package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

case class Initiator(order: Int,
                     signals: Set[StringSignal],
                     referents: Set[StringReferent],
                     intendedReferent: StringReferent,
                     previousSignal: Option[StringSignal],
                     history: List[(StringSignal, StringSignal)],
                     lexiconPriors: Distribution[Lexicon],
                     signalPriors: Distribution[StringSignal],
                     referentPriors: Distribution[StringReferent],
                     signalCosts: Map[StringSignal, BigNatural],
                     beta: BigNatural,
                     entropyThreshold: BigNatural)
  extends AdaptiveAgent(order, signals, referents, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta) {

  def nextIntention(intention: StringReferent): Initiator =
    Initiator(order, signals, referents, intention, None, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

  def initialSpeak: (MetaSignal, Initiator, InitialInitiatorData) = {
    s.cpt()
    val posteriorSignalDistribution = s.pr(intendedReferent)

    // we ignore entropy
    val inferredSignal = posteriorSignalDistribution.sample

    val metaSignal = MetaSignal(Some(inferredSignal))
    val updatedAgent = Initiator(order, signals, referents, intendedReferent, Some(inferredSignal), history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
    val initiatorData = InitialInitiatorData(intendedReferent, metaSignal)

    (metaSignal, updatedAgent, initiatorData)
  }

  override def listenAndRespond(observedSignal: StringSignal): (MetaSignal, Initiator, InitiatorData) = {
    require(previousSignal.isDefined, "[Initiator] Cannot listenAndRespond when I have no previousSignal.")

    // listen part
    val posteriorReferentDistribution = l.pr(observedSignal)
    val listenEntropy = posteriorReferentDistribution.entropy
    val inferredReferent = posteriorReferentDistribution.sample

    if(listenEntropy <= entropyThreshold && inferredReferent == intendedReferent) {
        // I'm quite certain of the inference and I believe we have mutual understanding. We're done
        (MetaSignal(None), this, InitiatorData(intendedReferent, inferredReferent, MetaSignal(None), listenEntropy))
    } else {
        // I believe I was misunderstood, or I don't really understand you.
        // speak part
        val intermediateAgent = Initiator(order, signals, referents, intendedReferent, None, (previousSignal.get, observedSignal) :: history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
        val posteriorSignalDistribution = intermediateAgent.s.pr(intendedReferent)
        // we ignore entropy
        val inferredSignal = posteriorSignalDistribution.sample
        val metaSignal = MetaSignal(Some(inferredSignal))
        val updatedAgent = Initiator(order, signals, referents, intendedReferent, Some(inferredSignal), (previousSignal.get, observedSignal) :: history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
        val initiatorData = InitiatorData(intendedReferent, inferredReferent, metaSignal, listenEntropy)
        (metaSignal, updatedAgent, initiatorData)
    }
  }
}

case object Initiator {
  def apply(order: Int,
            signals: Set[StringSignal],
            referents: Set[StringReferent],
            intendedReferent: StringReferent,
            previousSignal: Option[StringSignal],
            history: List[(StringSignal, StringSignal)],
            beta: BigNatural,
            entropyThreshold: BigNatural): Initiator = {
    val signalPriors = signals.uniformDistribution
    val referentPriors = referents.uniformDistribution
    val lexiconPriors = Lexicon.allPossibleLexicons(signals, referents).uniformDistribution
    val signalCosts = signals.map(_ -> 0.toBigNatural).toMap
    Initiator(order, signals, referents, intendedReferent, previousSignal, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
  }
}