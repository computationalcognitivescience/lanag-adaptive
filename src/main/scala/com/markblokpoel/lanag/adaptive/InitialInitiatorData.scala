package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.datastructures.BigNatural

case class InitialInitiatorData(intendedReferent: StringReferent,
                                signal: MetaSignal,
																lexiconEntropy: BigNatural)
  extends Data
