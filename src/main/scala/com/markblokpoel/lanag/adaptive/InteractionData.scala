package com.markblokpoel.lanag.adaptive

case class InteractionData(initialInitiatorData: InitialInitiatorData,
                           initiatorData: List[InitiatorData],
                           responderData: List[ResponderData]) {
  def addInitiatorData(moreInitiatorData: InitiatorData): InteractionData =
    InteractionData(initialInitiatorData, moreInitiatorData :: initiatorData, responderData)

  def addResponderData(moreResponderData: ResponderData): InteractionData =
    InteractionData(initialInitiatorData, initiatorData, moreResponderData :: responderData)

  override def toString: String = {
    s"==NEW ROUND==\n[Initiator] ${initialInitiatorData.intendedReferent} -> ${initialInitiatorData.signal}\n" +
      (for(riData <- responderData.map(Option.apply).zipAll(initiatorData.map(Option.apply), None, None)) yield {
        val responder = if(riData._1.isDefined)
          "[Responder] " + riData._1.get.inferredReferent + " from " + riData._1.get.posteriorDistribution + f" with H=${riData._2.get.listenEntropy.doubleValue()}%1.2f\n" +
            "[Responder] " + riData._1.get.inferredReferent + " -> " + riData._1.get.signal
        else
          "[Responder] ..."

        val initiator = if(riData._1.isDefined)
          "[Initiator] " + riData._2.get.inferredReferent +  " from " + riData._2.get.posteriorDistribution + f" with=${riData._2.get.listenEntropy.doubleValue()}%1.2f" + "\n" +
            "[Initiator] " + riData._2.get.intendedReferent + " -> " + riData._2.get.signal
        else
          "[Initiator] ..."

        responder + "\n" + initiator
      }).mkString("\n")
  }

}