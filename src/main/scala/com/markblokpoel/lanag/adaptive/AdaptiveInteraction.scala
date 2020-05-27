package com.markblokpoel.lanag.adaptive

import scala.util.Random

case class AdaptiveInteraction(referents: Set[StringReferent],
                               initialInitiator: Initiator,
                               initialResponder: Responder,
                               maxTurns: Int,
                               nrRounds: Int)
  extends Iterator[InteractionData] {

  var round = 0

  override def hasNext: Boolean = round < nrRounds

  override def next(): InteractionData = {
    val randomIntention = referents.toList(Random.nextInt(referents.size))

    val (initialMetaSignal, updatedInitiator, initialInitiatorData) =
      initialInitiator.nextIntention(randomIntention).initialSpeak

    var turn = 0
    var done = initialMetaSignal.understood
    var responder = initialResponder
    var initiator = updatedInitiator
    var interactionData = InteractionData(initialInitiatorData, List.empty, List.empty)
    var initiatorMetaSignal = initialMetaSignal

    while(!done) {
      // Responder
      val (responderMetaSignal, updatedResponder, responderData) = responder.listenAndRespond(initiatorMetaSignal.getSignal)
      responder = updatedResponder
      done = responderMetaSignal.understood
      interactionData = interactionData.addResponderData(responderData)

      if(!done) {
        val result: (MetaSignal, Initiator, InitiatorData) = initiator.listenAndRespond(responderMetaSignal.getSignal)
        initiatorMetaSignal = result._1
        initiator = result._2
        interactionData = interactionData.addInitiatorData(result._3)
        done = initiatorMetaSignal.understood
      }
      turn = turn + 1
      done = done || turn == maxTurns
    }

    interactionData
  }
}
