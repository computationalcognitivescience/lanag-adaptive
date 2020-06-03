package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Implicits._

object MainSimulation extends App {

  val signals = Set("S1", "S2", "S3").map(StringSignal)
  val referents = Set("R1", "R2").map(StringReferent)
  val allLexicons = Lexicon.allConsistentLexicons(signals, referents)

  val initiator = Initiator(
    order = 1,
    signals,
    referents,
    intendedReferent = StringReferent("R1"),
    previousSignal = None,
    history = List.empty,
    allLexicons,
    beta = 2.toBigNatural,
    entropyThreshold = 0.8.toBigNatural)

  val responder = Responder(
    order = 1,
    signals,
    referents,
    history = List.empty,
    allLexicons,
    beta = 2.toBigNatural,
    entropyThreshold = 0.8.toBigNatural)

  val interaction = AdaptiveInteraction(referents, initiator, responder, maxTurns = 6, nrRounds = 6)

  val allData = interaction.toList

  println(allData.mkString("\n"))

}
