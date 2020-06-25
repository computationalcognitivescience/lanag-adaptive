package com.markblokpoel.lanag.adaptive.storage

//import com.markblokpoel.lanag.adaptive.StringReferent
import com.markblokpoel.lanag.adaptive.atoms.{MetaSignal, StringReferent}
import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class InitiatorData(intendedReferent: StringReferent,
												 inferredReferent: StringReferent,
												 signal: MetaSignal,
												 listenEntropy: BigNatural,
												 posteriorReferentDistribution: Distribution[StringReferent],
												 lexiconEntropy: BigNatural)
  extends Data
