package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.{ConditionalDistribution, Distribution}
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.DistributionHelpers._
import com.markblokpoel.probability4scala.datastructures.BigNatural

abstract class AdaptiveAgent(order: Int,
                             signals: Set[StringSignal],
                             referents: Set[StringReferent],
                             history: List[(StringSignal, StringSignal)],
                             lexiconPriors: Distribution[Lexicon],
                             signalPriors: Distribution[StringSignal],
                             referentPriors: Distribution[StringReferent],
                             signalCosts: Map[StringSignal, BigNatural],
                             beta: BigNatural) {

  protected val signalCostsAsDistr = new Distribution(signalCosts.keySet, signalCosts)

  def listenAndRespond(signal: StringSignal): (MetaSignal, AdaptiveAgent, Data)

  private def s(n: Int, lexicon: Lexicon): ConditionalDistribution[StringSignal, StringReferent] = {
    if(n==0) s0(lexicon)
    else s(l(n-1, lexicon))
  }

  private def l(n: Int, lexicon: Lexicon): ConditionalDistribution[StringReferent, StringSignal] = {
    if(n==0) l0(lexicon)
    else l(s(n, lexicon))
  }

  // Eq 7?
  def s0(lexicon: Lexicon): ConditionalDistribution[StringSignal, StringReferent] = lexicon.deltaS

  // Eq 6
  def l0(lexicon: Lexicon): ConditionalDistribution[StringReferent, StringSignal] = lexicon.deltaL

  // Eq 5
  def l(distribution: ConditionalDistribution[StringSignal, StringReferent]): ConditionalDistribution[StringReferent, StringSignal] =
    distribution.bayes(referentPriors)

  // Eq 4: Sn(s | r, Lex) propto exp(alpha log L_{n-1}(r | s, Lex) - cost(s))
  def s(distribution: ConditionalDistribution[StringReferent, StringSignal]): ConditionalDistribution[StringSignal, StringReferent] =
    exp(log(distribution.bayes(signalPriors) * beta) -- signalCostsAsDistr)

  // Eq 3: PrLn(Lex | d)
  def likelihood(lexicon: Lexicon): BigNatural = {
    val speakerPerspective = s(order, lexicon)
    val listenerPerspective = l(order, lexicon)
    val lexiconLikelihood =
      (for((s1,s2) <- history) yield {
        (for(r <- referents) yield {
          speakerPerspective.pr(s1 | r) * listenerPerspective.pr(r | s2)
        }).fold(0.toBigNatural)(_ + _)
      }).fold(0.toBigNatural)(_ * _)
    lexiconLikelihood * lexiconPriors.pr(lexicon)
  }

  // Eq 2: Ln(r | s, d)
  protected def l: ConditionalDistribution[StringReferent, StringSignal] = {
    val inner =
      for(lexicon <- Lexicon.allPossibleLexicons(signals, referents)) yield {
        l(order, lexicon) * likelihood(lexicon)
      }
    inner.tail.foldLeft(inner.head)((acc, p) => acc + p)
  }

  // Eq 1: Sn(s | r, d) propto ..
  protected def s: ConditionalDistribution[StringSignal, StringReferent] = {
    val inner: Set[ConditionalDistribution[StringSignal, StringReferent]] =
      (for(lexicon <- Lexicon.allPossibleLexicons(signals, referents)) yield {
        l(order - 1, lexicon).bayes(signalPriors) * likelihood(lexicon)
      })
    val sum: ConditionalDistribution[StringSignal, StringReferent] =
      inner.tail.foldLeft(inner.head)((acc, p) => acc + p)

    exp(log(sum) * beta -- signalCostsAsDistr)
  }
}

//case object AdaptiveAgent {
//  def apply(order: Int,
//            signals: Set[StringSignal],
//            referents: Set[StringReferent],
//            history: List[(StringSignal, StringSignal)],
//            beta: Double,
//            entropyThreshold: Double): AdaptiveAgent = {
//    val signalPriors = signals.uniformDistribution
//    val referentPriors = referents.uniformDistribution
//    val lexiconPriors = Lexicon.allPossibleLexicons(signals, referents).uniformDistribution
//    val signalCosts = signals.map(_ -> 0.toBigNatural).toMap
//    AdaptiveAgent(order, signals, referents, history, lexiconPriors, signalPriors, referentPriors, signalCosts, beta, entropyThreshold)
//  }
//}
