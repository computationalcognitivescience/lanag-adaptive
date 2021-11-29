package com.markblokpoel.lanag.adaptive.storage

//import com.markblokpoel.lanag.adaptive.StringReferent
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

/** Data storage for the responder
  *
  *  @param inferredReferent Referent inferred from the signal produced by the initiator
  *  @param signal Signal spoken by the responder this turn
  *  @param listenEntropy Entropy over the posterior distribution given the inferred referent from the initiator
  *  @param posteriorResponderDistribution Posterior distribution given the inferred referent from the initiator
  *  @param lexiconEntropy Entropy over the lexicon likelihood
  */
case class ResponderData(
    inferredReferent: StringReferent,
    signal: MetaSignal,
    listenEntropy: BigNatural,
    posteriorResponderDistribution: Distribution[StringReferent],
    lexiconEntropy: BigNatural)
    extends Data
