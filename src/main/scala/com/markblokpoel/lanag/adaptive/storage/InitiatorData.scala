package com.markblokpoel.lanag.adaptive.storage

//import com.markblokpoel.lanag.adaptive.StringReferent
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

/** Data storage for the initiator for a non-starting turn
 *
 *  @param intendedReferent the Referent to communicate
 *  @param inferredReferent Referent inferred from the signal produced by the responder
 *  @param signal Signal spoken by initiator this turn
 *  @param listenEntropy Entropy over the posterior distribution given the inferred referent from the responder
 *  @param posteriorReferentDistribution Posterior distribution given the inferred referent from the responder
 *  @param lexiconEntropy Entropy over the lexicon likelihood
 */
case class InitiatorData(intendedReferent: StringReferent,
												 inferredReferent: StringReferent,
												 signal: MetaSignal,
												 listenEntropy: BigNatural,
												 posteriorReferentDistribution: Distribution[StringReferent],
												 lexiconEntropy: BigNatural)
  extends Data
