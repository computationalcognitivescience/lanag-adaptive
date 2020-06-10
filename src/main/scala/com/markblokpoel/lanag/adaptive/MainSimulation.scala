package com.markblokpoel.lanag.adaptive

//import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.datastructures.BigNatural

//import com.markblokpoel.probability4scala.{ConditionalDistribution, Distribution}
//import com.markblokpoel.probability4scala.DistributionHelpers._
import com.markblokpoel.probability4scala.datastructures.BigNatural
//import com.markblokpoel.probability4scala.Distribution
import com.markblokpoel.probability4scala.Implicits._

object MainSimulation extends App {

  val signals = Set("S1", "S2", "S3").map(StringSignal)
  val referents = Set("R1", "R2").map(StringReferent)
  val allLexicons = Lexicon.allConsistentLexicons(signals, referents)
//
//  val initiator = Initiator(
//    order = 1,
//    signals,
//    referents,
//    intendedReferent = StringReferent("R1"),
//    previousSignal = None,
//    history = List((StringSignal("S1"), StringSignal("S2")), (StringSignal("S2"), StringSignal("S2"))),
//    allLexicons,
//    beta = 20.toBigNatural,
//    entropyThreshold = 0.8.toBigNatural)
//
//  val lexiconDistribution = (for(l <- allLexicons) yield {
//    l -> initiator.likelihood(l)
//  }).toMap
//
//  val ld = Distribution(allLexicons, lexiconDistribution).softmax(1.toBigNatural)
//  ld.hist()



  val d = Set("a","b","c","d","e")




  d.binomialDistribution(BigNatural(0.45)).hist()
  d.binomialDistribution(BigNatural(0.55)).hist()


//  val lexiconPriors = binom(allLexicons, BigNatural(0.5))
//
//  val initiator = Initiator(
//    1,
//    signals,
//    referents,
//    StringReferent("R1"),
//    None,
//    List.empty,
//    allLexicons,
//    lexiconPriors,
//    signals.uniformDistribution,
//    referents.uniformDistribution,
//    signals.map(_ -> 0.toBigNatural).toMap,
//    20.toBigNatural,
//    0.8.toBigNatural)
//
//  val responder = Responder(
//    order = 1,
//    signals,
//    referents,
//    history = List.empty,
//    allLexicons,
//    beta = 20.toBigNatural,
//    entropyThreshold = 0.8.toBigNatural)
//
//  val interaction = AdaptiveInteraction(referents, initiator, responder, maxTurns = 6, nrRounds = 6)
//
//  val allData = interaction.toList
//
//  println(allData.mkString("\n"))


//  val cd = ConditionalDistribution(signals, referents,
//    Map(
//      (StringSignal("S1"), StringReferent("R1")) -> 0.0006,
//      (StringSignal("S2"), StringReferent("R1")) -> 0.0006,
//      (StringSignal("S3"), StringReferent("R1")) -> 0.0006,
//      (StringSignal("S1"), StringReferent("R2")) -> 0.0006,
//      (StringSignal("S2"), StringReferent("R2")) -> 0.0000,
//      (StringSignal("S3"), StringReferent("R2")) -> 0.0012
//    )
//  )
//  cd.cpt()
//  val cost = Distribution(signals, Map(StringSignal("S1") -> 0.0, StringSignal("S2") -> 0.0, StringSignal("S3") -> 0.0))
//
//  val expie = cd.softmax(BigNatural(10), cost)
//  expie.cpt()
//  expie.pr(StringReferent("R1")).hist()
//  expie.pr(StringReferent("R2")).hist()

//  println((BigNatural(0.006).log*2).exp)

}
