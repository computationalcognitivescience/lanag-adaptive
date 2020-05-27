package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.datastructures.BigNatural

case class ResponderData(inferredReferent: StringReferent,
                         signal: MetaSignal,
                         listenEntropy: BigNatural)
  extends Data