package com.markblokpoel.lanag.adaptive.storage

//import com.markblokpoel.lanag.adaptive.StringReferent
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class InitialInitiatorData(intendedReferent: StringReferent,
                                signal: MetaSignal,
																lexiconEntropy: BigNatural)
  extends Data
