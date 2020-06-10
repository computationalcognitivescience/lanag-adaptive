package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._

object MainSimulation extends App {

  val signals = Set("S1", "S2", "S3").map(StringSignal)
  val referents = Set("R1", "R2").map(StringReferent)
  val allLexicons = Lexicon.allConsistentLexicons(signals, referents)


  util.Random.setSeed(1000L)
  val lexiconPriors = allLexicons.binomialDistribution(BigNatural(0.5))

  val initiator = Initiator(
    1,
    signals,
    referents,
    StringReferent("R1"),
    None,
    List.empty,
    allLexicons,
    lexiconPriors,
    signals.uniformDistribution,
    referents.uniformDistribution,
    signals.map(_ -> 0.toBigNatural).toMap,
    20.toBigNatural,
    0.8.toBigNatural)

  val responder = Responder(
    1,
    signals,
    referents,
    List.empty,
    allLexicons,
    lexiconPriors,
    signals.uniformDistribution,
    referents.uniformDistribution,
    signals.map(_ -> 0.toBigNatural).toMap,
    20.toBigNatural,
    0.8.toBigNatural)

  val interaction = AdaptiveInteraction(referents, initiator, responder, maxTurns = 6, nrRounds = 6)

  val allData = interaction.toList

  println(allData.mkString("\n"))

}
