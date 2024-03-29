package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.adaptive.agents.{AdaptiveInitiator, AdaptiveResponder}
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.lanag.adaptive.storage.{InitiatorData, InteractionData}

import scala.util.Random

/** The interaction for non-ostensive interaction
  *
  * @param referents the set of possible referents
  * @param initialInitiator the non-ostensive initiator agent
  * @param initialResponder the non-ostensive responder agent
  * @param maxTurns maximum number of turns in a dialogue
  * @param nrRounds maximum number of dialogues before conversation ends
  */
case class AdaptiveInteraction(referents: Set[StringReferent],
                               initialInitiator: AdaptiveInitiator,
                               initialResponder: AdaptiveResponder,
                               maxTurns: Int,
                               nrRounds: Int)
    extends Iterator[InteractionData] {

  private var round = 0
  private var responder = initialResponder
  private var initiator = initialInitiator

  /** Checks whether there are dialogues left to be performed
    *
    * @return true if current dialogue is not the final one
    */
  override def hasNext: Boolean = round < nrRounds

  /** Performs the whole conversation
    *
    * @return the data stored during the interaction
    */
  override def next(): InteractionData = {
    val randomIntention = referents.toList(Random.nextInt(referents.size))

    val klInitItoR = initiator.lexiconLikelihoodDistribution.klDivergence(
      responder.lexiconLikelihoodDistribution)
    val klInitRtoI = responder.lexiconLikelihoodDistribution.klDivergence(
      initiator.lexiconLikelihoodDistribution)
    val (initialMetaSignal, updatedInitiator, initialInitiatorData) =
      initiator.nextIntention(randomIntention).initialSpeak
    var turn = 0
    var done = initialMetaSignal.understood
    initiator = updatedInitiator
    var interactionData = storage.InteractionData(initialInitiatorData,
                                                  klInitItoR,
                                                  klInitRtoI,
                                                  List.empty,
                                                  List.empty,
                                                  List.empty,
                                                  List.empty)
    var initiatorMetaSignal = initialMetaSignal

    while (!done) {
      print(s"r$round/t$turn\r")
      // Responder
      val (responderMetaSignal, updatedResponder, responderData) =
        responder.listenAndRespond(initiatorMetaSignal.getSignal)
      responder = updatedResponder
      done = responderMetaSignal.understood
      interactionData = interactionData.addResponderData(responderData)

      if (!done) {
        val klItoR = initiator.lexiconLikelihoodDistribution.klDivergence(
          responder.lexiconLikelihoodDistribution)
        val klRtoI = responder.lexiconLikelihoodDistribution.klDivergence(
          initiator.lexiconLikelihoodDistribution)
        interactionData = interactionData.addKLDivergence(klItoR, klRtoI)
        // Initiator
        val result: (MetaSignal, AdaptiveInitiator, InitiatorData) =
          initiator.listenAndRespond(responderMetaSignal.getSignal)
        initiatorMetaSignal = result._1
        initiator = result._2
        interactionData = interactionData.addInitiatorData(result._3)
        done = initiatorMetaSignal.understood
      }
      turn = turn + 1
      done = done || turn == maxTurns
    }

    round = round + 1
    interactionData
  }
}
