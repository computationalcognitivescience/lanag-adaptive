package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

case class ResponderData(inferredReferent: StringReferent,
                         signal: MetaSignal,
                         listenEntropy: BigNatural,
                         posteriorDistribution: Distribution[StringReferent])
  extends Data