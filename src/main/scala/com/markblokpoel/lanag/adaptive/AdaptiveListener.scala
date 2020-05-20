package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.core.{Data, Listener, Speaker}
import com.markblokpoel.lanag.math.Distribution

case class AdaptiveListener(order: Int,
                            signals: Set[StringSignal],
                            referents: Set[StringReferent],
                            priorLexicon: Distribution[Lexicon],
                            beta: Double,
                            entropyThreshold: Double,
                            history: List[(StringSignal, StringSignal)])
  extends AdaptiveAgent(order, signals, referents, priorLexicon, beta, entropyThreshold, history)
    with Listener[StringReferent, StringSignal] {

  // do complicated math stuff and collect data
  override def interpretSignal(signal: StringSignal): (StringReferent, Data) = ???
}
