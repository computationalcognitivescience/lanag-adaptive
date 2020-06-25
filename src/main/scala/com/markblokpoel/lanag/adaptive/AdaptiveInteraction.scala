package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.adaptive.agents.{Initiator, Responder}
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.lanag.adaptive.storage.{InitiatorData, InteractionData}

import scala.util.Random

case class AdaptiveInteraction(referents: Set[StringReferent],
                               initialInitiator: Initiator,
                               initialResponder: Responder,
                               maxTurns: Int,
                               nrRounds: Int)
  extends Iterator[InteractionData] {

  private var round = 0
  private var responder = initialResponder
  private var initiator = initialInitiator

  override def hasNext: Boolean = round < nrRounds

  override def next(): InteractionData = {
    val randomIntention = referents.toList(Random.nextInt(referents.size))

    val klInitItoR = initiator.lexiconLikelihoodDistribution.klDivergence(responder.lexiconLikelihoodDistribution)
    val klInitRtoI = responder.lexiconLikelihoodDistribution.klDivergence(initiator.lexiconLikelihoodDistribution)
    val (initialMetaSignal, updatedInitiator, initialInitiatorData) =
      initiator.nextIntention(randomIntention).initialSpeak
    var turn = 0
    var done = initialMetaSignal.understood
    initiator = updatedInitiator
    var interactionData = storage.InteractionData(initialInitiatorData, klInitItoR, klInitRtoI, List.empty, List.empty, List.empty, List.empty)
    var initiatorMetaSignal = initialMetaSignal

    while(!done) {
      print(s"r$round/t$turn\r")
      // Responder
      val (responderMetaSignal, updatedResponder, responderData) = responder.listenAndRespond(initiatorMetaSignal.getSignal)
      responder = updatedResponder
      done = responderMetaSignal.understood
      interactionData = interactionData.addResponderData(responderData)

      if(!done) {
        val klItoR = initiator.lexiconLikelihoodDistribution.klDivergence(responder.lexiconLikelihoodDistribution)
        val klRtoI = responder.lexiconLikelihoodDistribution.klDivergence(initiator.lexiconLikelihoodDistribution)
        interactionData = interactionData.addKLDivergence(klItoR, klRtoI)
        // Initiator
        val result: (MetaSignal, Initiator, InitiatorData) = initiator.listenAndRespond(responderMetaSignal.getSignal)
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
