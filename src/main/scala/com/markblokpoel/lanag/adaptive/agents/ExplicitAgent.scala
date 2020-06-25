package com.markblokpoel.lanag.adaptive.agents

import com.markblokpoel.lanag.adaptive.atoms.{Lexicon, MetaSignal, StringReferent, StringSignal}
import com.markblokpoel.lanag.adaptive.storage.Data
import com.markblokpoel.probability4scala.{ConditionalDistribution, Distribution}
import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

abstract class ExplicitAgent (order: Int,
//															signals: Set[StringSignal],
//															referents: Set[StringReferent],
															history: List[(StringSignal, StringReferent)],
															allLexicons: Set[Lexicon],
															lexiconPriors: Distribution[Lexicon],
															signalPriors: Distribution[StringSignal],
															referentPriors: Distribution[StringReferent],
															signalCosts: Map[StringSignal, BigNatural],
														 beta: BigNatural) {

	protected val signalCostsAsDistr = new Distribution(signalCosts.keySet, signalCosts)

	def listenAndRespond(signal: StringSignal): (MetaSignal, ExplicitAgent, Data)

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
		distribution.bayes(signalPriors).softmax(beta, signalCostsAsDistr)
	//    exp(log(distribution.bayes(signalPriors) * beta) -- signalCostsAsDistr)

	// Eq 3: PrLn(Lex | d), adapted for explicit feedback
	def likelihood(lexicon: Lexicon): BigNatural = {
		val speakerPerspective = s(order, lexicon)
		val lexiconLikelihood =
			(for((s,r) <- history) yield {
				speakerPerspective.pr(s | r)
			}).fold(1.toBigNatural)(_ * _)
		if(history.nonEmpty) lexiconLikelihood * lexiconPriors.pr(lexicon)
		else lexiconPriors.pr(lexicon)
	}

	lazy val lexiconLikelihoodDistribution: Distribution[Lexicon] = {
		val lexiconDistribution = (for(l <- allLexicons) yield {
			l -> likelihood(l)
		}).toMap
		Distribution(allLexicons, lexiconDistribution).softmax(1.toBigNatural)
	}

	// Eq 2: Ln(r | s, d)
	protected def l: ConditionalDistribution[StringReferent, StringSignal] = {
		val inner =
			for(lexicon <- allLexicons) yield {
				l(order, lexicon) * lexiconLikelihoodDistribution.pr(lexicon)
			}
		inner.tail.foldLeft(inner.head)((acc, p) => acc + p)
	}

	// Eq 1: Sn(s | r, d) propto ..
	protected def s: ConditionalDistribution[StringSignal, StringReferent] = {
		//    println("ALL LEXICONS"+allLexicons)
		val inner: List[ConditionalDistribution[StringSignal, StringReferent]] =
			for(lexicon <- allLexicons.toList) yield {
				val liLex = lexiconLikelihoodDistribution.pr(lexicon)
				val lex = l(order - 1, lexicon).bayes(signalPriors)
				//        println("S\n"+liLex)
				//        lex.cpt()
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

