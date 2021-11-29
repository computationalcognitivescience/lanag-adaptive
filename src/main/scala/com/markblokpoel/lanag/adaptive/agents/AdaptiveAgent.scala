package com.markblokpoel.lanag.adaptive.agents

//import com.markblokpoel.lanag.adaptive._
import com.markblokpoel.lanag.adaptive.atoms.{
  Lexicon,
  MetaSignal,
  StringReferent,
  StringSignal
}
import com.markblokpoel.lanag.adaptive.storage.Data
import com.markblokpoel.probability4scala.Implicits._
import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.{
  ConditionalDistribution,
  Distribution
}

/** Abstract Non-ostensive agent class
  *
  *  Contains all reasoning the agents do
  *  referenced equations to current version of the paper
  *  @param order Order of reasoning
  *  @param referents Set of all possible referents
  *  @param history List of pair of two signals: the conversation history so far
  *  @param allLexicons Set of all possible lexicons
  *  @param lexiconPriors Distribution of Lexicon priors
  *  @param signalPriors Distribution of Signal Priors
  *  @param referentPriors Distribution of Referent Priors
  *  @param signalCosts Map of signal costs per signal
  *  @param beta beta parameter value
  */
abstract class AdaptiveAgent(order: Int,
//                             signals: Set[StringSignal],
                             referents: Set[StringReferent],
                             history: List[(StringSignal, StringSignal)],
                             allLexicons: Set[Lexicon],
                             lexiconPriors: Distribution[Lexicon],
                             signalPriors: Distribution[StringSignal],
                             referentPriors: Distribution[StringReferent],
                             signalCosts: Map[StringSignal, BigNatural],
                             beta: BigNatural) {

  protected val signalCostsAsDistr =
    new Distribution(signalCosts.keySet, signalCosts)

  /** Abstract Listen and Respond function
    *
    * Performs all turns that are not the initial turn
    * Consists of listening (interpreting) the received input
    * And responding to this
    * @param signal The signal observed
    * @return The signal communicated, the initiator with this dialogue stored, and the data from this interaction
    */
  def listenAndRespond(signal: StringSignal): (MetaSignal, AdaptiveAgent, Data)

  /** Computes S for order (set up for equation 1)
    *
    *  @param n Order of reasoning
    *  @param lexicon the lexicon S is computed for
    *  @return Conditional Distribution over signals and referents given the lexicon, speaker perspective
    */
  private def s(n: Int, lexicon: Lexicon)
    : ConditionalDistribution[StringSignal, StringReferent] = {
    if (n == 0) s0(lexicon)
    else s(l(n - 1, lexicon))
  }

  /** Computes L for order (set up for equation 2)
    *
    *  @param n Order of reasoning
    *  @param lexicon the lexicon L is computed over
    *  @return Conditional Distribution over referents and signals given the lexicon, listener perspective
    */
  private def l(n: Int, lexicon: Lexicon)
    : ConditionalDistribution[StringReferent, StringSignal] = {
    if (n == 0) l0(lexicon)
    else l(s(n, lexicon))
  }

  /** Computes S for order 0 (analogous to equation 3)
    *
    *  @param lexicon the lexicon S is computed over
    *  @return Conditional Distribution over signals and referents given the lexicon, speaker perspective
    */
  def s0(
      lexicon: Lexicon): ConditionalDistribution[StringSignal, StringReferent] =
    lexicon.deltaS

  /** Computes L for order 0 (equation 3)
    *
    *  @param lexicon the lexicon L is computed over
    *  @return Conditional Distribution over referents and signals given the lexicon, listener perspective
    */
  def l0(
      lexicon: Lexicon): ConditionalDistribution[StringReferent, StringSignal] =
    lexicon.deltaL

  /** Computes L (equation 2)
    *
    *  @param distribution Conditional Distribution of signals and referents, speaker perspective
    *  @return Conditional Distribution of referents and signals, listener perspective
    */
  def l(distribution: ConditionalDistribution[StringSignal, StringReferent])
    : ConditionalDistribution[StringReferent, StringSignal] =
    distribution.bayes(referentPriors)

  /** Computes S (equation 3)
    *
    *  @param distribution Conditional Distribution of signals and referents, speaker perspective
    *  @return Conditional Distribution of referents and signals, listener perspective
    */
  def s(distribution: ConditionalDistribution[StringReferent, StringSignal])
    : ConditionalDistribution[StringSignal, StringReferent] =
    distribution.bayes(signalPriors).softmax(beta, signalCostsAsDistr)
//    exp(log(distribution.bayes(signalPriors) * beta) -- signalCostsAsDistr)

  /** Computes likelihood of lexicon given conversation history, non-ostensive version (equation 7)
    *
    * @param lexicon the lexicon likelihood is to be determined of
    * @return lexicon likelihood
    */
  def likelihood(lexicon: Lexicon): BigNatural = {
    val speakerPerspective = s(order, lexicon)
    val listenerPerspective = l(order, lexicon)
    val lexiconLikelihood =
      (for ((s1, s2) <- history) yield {
        (for (r <- referents) yield {
          speakerPerspective.pr(s1 | r) * listenerPerspective.pr(r | s2)
        }).fold(0.toBigNatural)(_ + _)
      }).fold(1.toBigNatural)(_ * _)
    if (history.nonEmpty) lexiconLikelihood * lexiconPriors.pr(lexicon)
    else lexiconPriors.pr(lexicon)
  }

  lazy val lexiconLikelihoodDistribution: Distribution[Lexicon] = {
    val lexiconDistribution = (for (l <- allLexicons) yield {
      l -> likelihood(l)
    }).toMap
    Distribution(allLexicons, lexiconDistribution).softmax(1.toBigNatural)
  }

  /** Computes L over history (equation 5)
    *
    *  @return Conditional distribution over referents and signals, listener perspective
    */
  protected def l: ConditionalDistribution[StringReferent, StringSignal] = {
    val inner =
      for (lexicon <- allLexicons) yield {
        l(order, lexicon) * lexiconLikelihoodDistribution.pr(lexicon)
      }
    inner.tail.foldLeft(inner.head)((acc, p) => acc + p)
  }

  /** Computes S over history (equation 6)
    *
    *  @return Conditional Distribution over signals and referents, speaker perspective
    */
  protected def s: ConditionalDistribution[StringSignal, StringReferent] = {
    val inner: List[ConditionalDistribution[StringSignal, StringReferent]] =
      for (lexicon <- allLexicons.toList) yield {
        val liLex = lexiconLikelihoodDistribution.pr(lexicon)
        val lex = l(order - 1, lexicon).bayes(signalPriors)
        lex * liLex
      }

    val sum: ConditionalDistribution[StringSignal, StringReferent] =
      inner.tail.foldLeft(inner.head)((acc, p) => acc + p)

//    println("SUM")
//    sum.cpt()
//    sum.softmax(beta, signalCostsAsDistr).cpt()

    sum.softmax(beta, signalCostsAsDistr)
  }
}
