package com.markblokpoel.lanag.adaptive.agents

import com.markblokpoel.lanag.adaptive.atoms.{
  Lexicon,
  MetaSignal,
  StringReferent,
  StringSignal
}
import com.markblokpoel.lanag.adaptive.storage._
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.datastructures.BigNatural

/** The ostensive initiator agent
  *
  *  @param order Order of reasoning
  *  @param signals Set of possible signals
  *  @param referents Set of possible referents
  *  @param intendedReferent The referent this agent should communicate
  *  @param previousSignal The signal communicated last turn
  *  @param history List of pair of a signal and a referent: the conversation history so far
  *  @param allLexicons Set of all possible lexicons
  *  @param lexiconPriors Distribution of Lexicon Priors
  *  @param signalPriors Distribution of Signal Priors
  *  @param referentPriors Distribution of Referent Priors
  *  @param signalCosts Map of signal costs per signal
  *  @param beta beta parameter value
  *  @param entropyThreshold Entropy threshold value
  */
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
    extends ExplicitAgent(order,
                          history,
                          allLexicons,
                          lexiconPriors,
                          signalPriors,
                          referentPriors,
                          signalCosts,
                          beta) {

  /** Sets up the next intention
	 *
	 *  @param intention the intention to be communicated next
	 *  @return the ostensive initiator that will communicate
	 */
  def nextIntention(intention: StringReferent): ExplicitInitiator =
    ExplicitInitiator(order,
                      signals,
                      referents,
                      intention,
                      None,
                      history,
                      allLexicons,
                      lexiconPriors,
                      signalPriors,
                      referentPriors,
                      signalCosts,
                      beta,
                      entropyThreshold)

  /** Performs the inital speaking turn of the ostensive initiator
	 *  Is used at the start of each dialogue
	 *  @return The signal communicated, the initiator with this speak process stored, and the data from this interaction
	 */
  def initialSpeak: (MetaSignal, ExplicitInitiator, InitialInitiatorData) = {
    val posteriorSignalDistribution = s.pr(intendedReferent)

    // we ignore entropy
    val inferredSignal = posteriorSignalDistribution.sample

    val metaSignal = MetaSignal(Some(inferredSignal))
    val updatedAgent = ExplicitInitiator(
      order,
      signals,
      referents,
      intendedReferent,
      Some(inferredSignal),
      history,
      allLexicons,
      lexiconPriors,
      signalPriors,
      referentPriors,
      signalCosts,
      beta,
      entropyThreshold
    )
    val initiatorData = InitialInitiatorData(
      intendedReferent,
      metaSignal,
      lexiconLikelihoodDistribution.entropy)

    (metaSignal, updatedAgent, initiatorData)
  }

  /** Performs all turns that are not the initial turn
	 *
	 *  Consists of listening (interpreting) the received input
	 *  And responding to this
	 *
	 *  @param observedSignal the signal observed from the responder
	 *  @param explicitReferent the referent pointed at by the responder
	 *  @return The signal communicated, the ostensive initiator with this dialogue stored, and the data from this interaction
	 */
  def listenAndRespond(observedSignal: StringSignal,
                       explicitReferent: StringReferent)
    : (MetaSignal, ExplicitInitiator, ExplicitInitiatorData) = {
    require(
      previousSignal.isDefined,
      "[ExplicitInitiator] Cannot listenAndRespond when I have no previousSignal.")

    // listen part
    val posteriorReferentDistribution = l.pr(observedSignal)
    val listenEntropy = posteriorReferentDistribution.entropy
//		val inferredReferent = posteriorReferentDistribution.sample

    if (listenEntropy <= entropyThreshold && explicitReferent == intendedReferent) {
      // I'm quite certain of the inference and I believe we have mutual understanding. We're done
      val intermediateAgent = ExplicitInitiator(
        order,
        signals,
        referents,
        intendedReferent,
        None,
        (previousSignal.get, explicitReferent) :: history,
        allLexicons,
        lexiconPriors,
        signalPriors,
        referentPriors,
        signalCosts,
        beta,
        entropyThreshold
      )
//      println(s"initiator_end: ${intermediateAgent.history}")
      (MetaSignal(None),
       intermediateAgent,
       ExplicitInitiatorData(
         intendedReferent,
         explicitReferent,
         MetaSignal(None),
         listenEntropy,
         posteriorReferentDistribution,
         lexiconLikelihoodDistribution.entropy,
         intermediateAgent.history
       ))
    } else {
      // I believe I was misunderstood, or I don't really understand you.
      // speak part
      val intermediateAgent = ExplicitInitiator(
        order,
        signals,
        referents,
        intendedReferent,
        None,
        (previousSignal.get, explicitReferent) :: history,
        allLexicons,
        lexiconPriors,
        signalPriors,
        referentPriors,
        signalCosts,
        beta,
        entropyThreshold
      )
      //      println("Speak\n"+intermediateAgent.s)
      val posteriorSignalDistribution = intermediateAgent.s.pr(intendedReferent)
      // we ignore entropy
      val inferredSignal = posteriorSignalDistribution.sample
      val metaSignal = MetaSignal(Some(inferredSignal))
      val updatedAgent = ExplicitInitiator(
        order,
        signals,
        referents,
        intendedReferent,
        Some(inferredSignal),
        (previousSignal.get, explicitReferent) :: history,
        allLexicons,
        lexiconPriors,
        signalPriors,
        referentPriors,
        signalCosts,
        beta,
        entropyThreshold
      )
//      println(s"initiator: ${updatedAgent.history}")
      val initiatorData = ExplicitInitiatorData(
        intendedReferent,
        explicitReferent,
        metaSignal,
        listenEntropy,
        posteriorReferentDistribution,
        lexiconLikelihoodDistribution.entropy,
        updatedAgent.history)
      (metaSignal, updatedAgent, initiatorData)
    }
  }
}

/** Creates an ostensive initiator with default Distribution priors and signal costs
  *
  */
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
    ExplicitInitiator(
      order,
      signals,
      referents,
      intendedReferent,
      previousSignal,
      history,
      allLexicons,
      lexiconPriors,
      signalPriors,
      referentPriors,
      signalCosts,
      beta,
      entropyThreshold
    )
  }
}
