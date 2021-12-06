package com.markblokpoel.lanag.adaptive.storage

//import com.markblokpoel.lanag.adaptive.StringReferent
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.probability4scala.datastructures.BigNatural

/** Data storage for the initial speak of the initiator
  *
  * @param intendedReferent Referent to communicate
  * @param signal Signal communicated
  * @param lexiconEntropy Entropy over the current lexicon
  */
case class InitialInitiatorData(intendedReferent: StringReferent,
                                signal: MetaSignal,
                                lexiconEntropy: BigNatural)
    extends Data
