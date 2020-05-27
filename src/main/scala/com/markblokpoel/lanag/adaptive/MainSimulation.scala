package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.Implicits._

object MainSimulation extends App {

  val signals = Set("S1","S2","S3").map(StringSignal)
  val referents = Set("R1","R2").map(StringReferent)

  val initiator = Initiator(1, signals, referents, StringReferent("R1"), None, List.empty, 1.toBigNatural, 0.5.toBigNatural)
  val responder = Responder(1, signals, referents, List.empty, 1.toBigNatural, 0.5.toBigNatural)

  val interaction = AdaptiveInteraction(referents, initiator, responder, 10, 6)

  val allData = interaction.toList

  println(allData.mkString("\n"))

}
