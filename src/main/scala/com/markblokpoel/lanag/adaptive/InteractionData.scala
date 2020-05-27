package com.markblokpoel.lanag.adaptive

case class InteractionData(initialInitiatorData: InitialInitiatorData,
                           initiatorData: List[InitiatorData],
                           responderData: List[ResponderData]) {
  def addInitiatorData(moreInitiatorData: InitiatorData): InteractionData =
    InteractionData(initialInitiatorData, moreInitiatorData :: initiatorData, responderData)

  def addResponderData(moreResponderData: ResponderData): InteractionData =
    InteractionData(initialInitiatorData, initiatorData, moreResponderData :: responderData)

}