package com.markblokpoel.lanag.adaptive.agents

import com.markblokpoel.lanag.adaptive.atoms.{Lexicon, MetaSignal, StringReferent, StringSignal}
import com.markblokpoel.lanag.adaptive.storage._
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.datastructures.BigNatural

/** The non-ostensive responder agent
 *
 *  @param order Order of reasoning
 *  @param signals Set of possible signals
 *  @param referents Set of possible referents
 *  @param history List of pairs of signals: the conversation history so far
 *  @param allLexicons Set of all possible lexicons
 *  @param lexiconPriors Distribution of Lexicon Priors
 *  @param signalPriors Distribution of Signal Priors
 *  @param referentPriors Distribution of Referent Priors
 *  @param signalCosts Map of signal costs per signal
 *  @param beta beta parameter value
 *  @param entropyThreshold Entropy threshold value
 */
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

  /** Performs all turns for the responder
   *
   *  Consists of listening (interpreting) the received input
   *  And responding to this
   *  @param observedSignal the signal observed from the Initiator
   *  @return The signal communicated, the responder with this dialogue stored, and the data from this interaction
   */
  override def listenAndRespond(observedSignal: StringSignal): (MetaSignal, Responder, ResponderData) = {
    // listen part
    val posteriorReferentDistribution = l.pr(observedSignal)
    val listenEntropy = posteriorReferentDistribution.entropy
    val inferredReferent = posteriorReferentDistribution.sample

    if(listenEntropy <= entropyThreshold) {
      // I'm quite certain I understood what you intended. We're done.
      (MetaSignal(None), this, ResponderData(inferredReferent, MetaSignal(None), listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy))
    } else {
      // I'm not quite certain, I'm gonna try to confirm
      // speak part
      val posteriorSignalDistribution = s.pr(inferredReferent)
      val inferredSignal = posteriorSignalDistribution.sample
      val metaSignal = MetaSignal(Some(inferredSignal))

      val updatedResponder = Responder(order, signals, referents, (observedSignal, inferredSignal) :: history, allLexicons, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

      val responderData = ResponderData(inferredReferent, metaSignal, listenEntropy, posteriorReferentDistribution, lexiconLikelihoodDistribution.entropy)

      (metaSignal, updatedResponder, responderData)
    }
  }
}

/** Creates a non-ostensive responder with default Distribution priors and signal costs
 *
 */
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