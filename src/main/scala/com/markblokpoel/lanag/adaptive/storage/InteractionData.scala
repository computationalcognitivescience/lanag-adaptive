package com.markblokpoel.lanag.adaptive.storage

import com.markblokpoel.probability4scala.datastructures.BigNatural

/** Data storage for the entire interaction
 *
 *  @param initialInitiatorData Data for the initial turn of the Initiator
 *  @param klInitItoR KL-divergence between initiator lexicon distribution and responder lexicon distribution, initial turn
 *  @param klInitRtoI KL-divergence between responder lexicon distribution and initiator lexicon distribution, initial turn
 *  @param initiatorData List of initiator Data for all turns
 *  @param responderData List of responder Data for all turns
 *  @param klInitiatorToResponder KL-divergence between initiator lexicon distribution and responder lexicon distribution, all other turns
 *  @param klResponderToInitiator KL-divergence between responder lexicon distribution and initiator lexicon distribution, all other turns
 */
case class InteractionData(initialInitiatorData: InitialInitiatorData,
                           klInitItoR: BigNatural,
                           klInitRtoI: BigNatural,
                           initiatorData: List[InitiatorData],
                           responderData: List[ResponderData],
                           klInitiatorToResponder: List[BigNatural],
                           klResponderToInitiator: List[BigNatural]) {

  /** Adding initiator data to InteractionData
   *
   *  @param moreInitiatorData Initiator data
   *  @return new InteractionData with updated initiator data
   */
  def addInitiatorData(moreInitiatorData: InitiatorData): InteractionData =
    InteractionData(initialInitiatorData, klInitItoR, klInitRtoI, initiatorData :+ moreInitiatorData, responderData, klInitiatorToResponder, klResponderToInitiator)

  /** Adding responder data to InteractionData
   *
   *  @param moreResponderData Responder data
   *  @return new InteractionData with updated responderdata
   */
  def addResponderData(moreResponderData: ResponderData): InteractionData =
    InteractionData(initialInitiatorData, klInitItoR, klInitRtoI, initiatorData, responderData :+ moreResponderData, klInitiatorToResponder, klResponderToInitiator)

  /** Adding KL-divergence information to InteractionData
   *
   *  @param initiatorToResponder KL-divergence initiator to responder
   *  @param responderToInitiator KL-divergence responder to initiator
   *  @return new InteractionData with updated KL-divergence data
   */
  def addKLDivergence(initiatorToResponder: BigNatural, responderToInitiator: BigNatural): InteractionData =
    InteractionData(initialInitiatorData, klInitItoR, klInitRtoI, initiatorData, responderData, klInitiatorToResponder :+ initiatorToResponder, klResponderToInitiator :+ responderToInitiator)
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
