package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.{Distribution, ConditionalDistribution}
import com.markblokpoel.probability4scala.Implicits._

case class AdaptiveAgent(order: Int,
                         signals: Set[StringSignal],
                         referents: Set[StringReferent],
                         history: List[(StringSignal, StringSignal)],
                         lexiconPriors: Distribution[Lexicon],
                         signalPriors: Distribution[StringSignal],
                         referentPriors: Distribution[StringReferent],
                         signalCosts : Map[StringSignal, Double],
                         beta: Double,
                         entropyThreshold: Double) {

//  def speak(intention: StringReferent): MetaSignal = ???
//
//  def listen(signal: StringSignal): StringReferent = ???

  def addObservation(signalPair: (StringSignal, StringSignal)): AdaptiveAgent =
    AdaptiveAgent(order, signals, referents, signalPair :: history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)

  def l0(lexicon: Lexicon): ConditionalDistribution[StringReferent, StringSignal] = lexicon.deltaL

  def s0(lexicon: Lexicon): ConditionalDistribution[StringSignal, StringReferent] = lexicon.deltaS

  def l(distribution: ConditionalDistribution[StringSignal, StringReferent]): ConditionalDistribution[StringReferent, StringSignal] =
    distribution.bayes(referentPriors)

  def s(distribution: ConditionalDistribution[StringReferent, StringSignal]): ConditionalDistribution[StringSignal, StringReferent] = {
    val ln_1 = distribution.bayes(signalPriors)
    val newDistribution = for((signal, referent) <- ln_1.distribution.keySet) yield {
      (signal, referent) -> math.exp(beta * math.log(ln_1.distribution((signal, referent)).doubleValue) - signalCosts(signal))
    }
    ConditionalDistribution(distribution.domainV2, distribution.domainV1, newDistribution.toMap)
  }

  def s(n: Int, lexicon: Lexicon): ConditionalDistribution[StringSignal, StringReferent] = {
    if(n==0) s0(lexicon)
    else s(l(n-1, lexicon))
  }

  def l(n: Int, lexicon: Lexicon): ConditionalDistribution[StringReferent, StringSignal] = {
    if(n==0) l0(lexicon)
    else l(s(n, lexicon))
  }

  def likelihood(lexicon: Lexicon): BigDecimal = {
    val speakerPerspective = s(order, lexicon)
    val listenerPerspective = l(order, lexicon)
    val lexiconLikelihood =
      (for((s1,s2) <- history) yield {
        (for(r <- referents) yield {
          speakerPerspective.pr(s1 | r) * listenerPerspective.pr(r | s2)
        }).sum
      }).product
    lexiconLikelihood * lexiconPriors.pr(lexicon)
  }

  def l: ConditionalDistribution[StringReferent, StringSignal] = {
    val parts =
      for(lexicon <- Lexicon.allPossibleLexicons(signals, referents)) yield {
        l(order, lexicon) * likelihood(lexicon)
      }
    parts.tail.foldLeft(parts.head)((acc, p) => acc + p)
  }

  // this need to be rewritten
  def s: ConditionalDistribution[StringSignal, StringReferent] = {
    val parts =
      (for(lexicon <- Lexicon.allPossibleLexicons(signals, referents)) yield {
        l(order - 1, lexicon) * likelihood(lexicon)
      })
    val sum = parts.tail.foldLeft(parts.head)((acc, p) => acc + p).bayes(signalPriors)

    val newDistribution = for((signal, referent) <- sum.distribution.keySet) yield {
      (signal, referent) -> math.exp(beta * math.log(sum.distribution((signal, referent)).doubleValue) - signalCosts(signal))
    }
    ConditionalDistribution(sum.domainV1, sum.domainV2, newDistribution.toMap)
  }

}

case object AdaptiveAgent {
  def apply(order: Int,
            signals: Set[StringSignal],
            referents: Set[StringReferent],
            history: List[(StringSignal, StringSignal)],
            beta: Double,
            entropyThreshold: Double): AdaptiveAgent = {
    val signalPriors = signals.uniformDistribution
    val referentPriors = referents.uniformDistribution
    val lexiconPriors = Lexicon.allPossibleLexicons(signals, referents).uniformDistribution
    val signalCosts = signals.map(_ -> 0.0).toMap
    AdaptiveAgent(order, signals, referents, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
  }
}
