package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.core.{Agent, Listener, Speaker}
import com.markblokpoel.lanag.math.Distribution

@SerialVersionUID(100L)
class AdaptiveAgent(order: Int,
                    signals: Set[StringSignal],
                    referents: Set[StringReferent],
                    priorLexicon: Distribution[Lexicon],
                    beta: Double,
                    entropyThreshold: Double,
                    history: List[(StringSignal, StringSignal)])
  extends Agent[StringReferent, StringSignal]
    with Serializable {

  def withOrder(order: Int): AdaptiveAgent = new AdaptiveAgent(order, signals, referents, priorLexicon, beta, entropyThreshold, history)



  override def asSpeaker: AdaptiveSpeaker = AdaptiveSpeaker(order, signals, referents, priorLexicon, beta, entropyThreshold, history)

  override def asListener: AdaptiveListener = AdaptiveListener(order, signals, referents, priorLexicon, beta, entropyThreshold, history)
}
