package com.markblokpoel.lanag.adaptive

case class InteractionData(initialInitiatorData: InitialInitiatorData,
                           initiatorData: List[InitiatorData],
                           responderData: List[ResponderData]) {
  def addInitiatorData(moreInitiatorData: InitiatorData): InteractionData =
    InteractionData(initialInitiatorData, initiatorData :+ moreInitiatorData, responderData)

  def addResponderData(moreResponderData: ResponderData): InteractionData =
    InteractionData(initialInitiatorData, initiatorData, responderData :+ moreResponderData)

  override def toString: String = {
    s"==NEW ROUND==\n[Initiator] ${initialInitiatorData.intendedReferent} -> ${initialInitiatorData.signal}\n" +
      (for(i <- 0 until math.max(initiatorData.size, responderData.size)) yield {
        val reData = if(responderData.isDefinedAt(i)) Some(responderData(i)) else None
        val inData = if(initiatorData.isDefinedAt(i)) Some(initiatorData(i)) else None

        val responder = if(reData.isDefined)
          "[Responder] " + reData.get.inferredReferent + " from " + reData.get.posteriorResponderDistribution +
            f" with H=${reData.get.listenEntropy.doubleValue()}%1.2f\n" +
            "[Responder] " + reData.get.inferredReferent + " -> " + reData.get.signal
        else
          "[Responder] ..."

        val initiator = if(inData.isDefined)
          "[Initiator] " + inData.get.inferredReferent +  " from " + inData.get.posteriorReferentDistribution +
            f" with H=${inData.get.listenEntropy.doubleValue()}%1.2f" + "\n" +
            "[Initiator] " + inData.get.intendedReferent + " -> " + inData.get.signal
        else
          "[Initiator] ..."

        responder + "\n" + initiator
      }).mkString("\n")
  }



}