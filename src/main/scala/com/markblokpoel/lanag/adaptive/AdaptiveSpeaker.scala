package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.core.{Data, Listener, Speaker}
import com.markblokpoel.lanag.math.Distribution
import com.markblokpoel.lanag.util.RNG

case class AdaptiveSpeaker(order: Int,
                           signals: Set[StringSignal],
                           referents: Set[StringReferent],
                           intention: StringReferent,
                           history: List[(StringSignal, StringSignal)],
                           priorLexicon: Distribution[Lexicon],
                           beta: Double,
                           entropyThreshold: Double)
  extends AdaptiveAgent(order, signals, referents, priorLexicon, beta, entropyThreshold, history)
    with Speaker[StringReferent, StringSignal] {

    /**
     * Selects a random intention (referent)
     * @return
     */
    override def selectIntention: StringReferent = referents.toList(RNG.nextInt(referents.size))


    // more complex math stuff
    override def produceSignal(intention: StringReferent): (StringSignal, Data) = ???

    /*
    Interaction

    agent1 en agent2
    maxturns

    hasNext()

    next(): Data {
    // kletsen :)
    if(curTurn == maxturn) agent1.newIntention -> new Speaker
    }

    ---
    interaction(ag1, ag2) -> List[Data]

     */

}
