package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.datastructures.BigNatural
import com.markblokpoel.probability4scala.Implicits._
import java.io._

object MainSimulation extends App {

  val signals = Set("S1", "S2", "S3").map(StringSignal)
  val referents = Set("R1", "R2").map(StringReferent)
  val allLexicons = Lexicon.allConsistentLexicons(signals, referents)

  util.Random.setSeed(1000L)
  val lexiconPriors = allLexicons.binomialDistribution(BigNatural(0.5))

  val nrPairs = 3

  val allData = (for(pair <- 0 until nrPairs) yield {
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

    (pair -> interaction.toList)
  }).toMap


  val pwt = new PrintWriter(new File("output/results_turns.csv" ))
  pwt.println("pair;round;turn;initiatorIntention;initiatorSignal;responderInference;responderSignal;entropyInitiatorListen;entropyResponderListen;entropyInitiatorLexicon;entropyResponderLexicon")
  allData.foreach(pairData => {
    val pair: Int = pairData._1
    val rounds: List[InteractionData] = pairData._2
    rounds.indices.foreach(round => {
      val roundData: InteractionData = rounds(round)
      val turn0i = roundData.initialInitiatorData
      val turn0r = roundData.responderData.head
      pwt.println(s"$pair;$round;0;${turn0i.intendedReferent};${turn0i.signal.toString};${turn0r.inferredReferent};${turn0r.signal.toString};NA;${turn0r.listenEntropy};${turn0i.lexiconEntropy};${turn0r.lexiconEntropy}")

      val restTurnsI = roundData.initiatorData
      val restTurnsR = roundData.responderData.tail
      for(turn <- 0 until math.max(restTurnsI.size, restTurnsR.size)) {
        if(restTurnsI.isDefinedAt(turn) && restTurnsR.isDefinedAt(turn)) {
          val turni = restTurnsI(turn)
          val turnr = restTurnsR(turn)
          pwt.println(s"$pair;$round;$turn;${turni.intendedReferent};${turni.signal.toString};${turnr.inferredReferent};${turnr.signal.toString};${turni.listenEntropy};${turnr.listenEntropy};${turni.lexiconEntropy};${turnr.lexiconEntropy}")
        } else {
          val turni = restTurnsI(turn)
          pwt.println(s"$pair;$round;$turn;${turni.intendedReferent};${turni.signal.toString};NA;NA;${turni.listenEntropy};NA;${turni.lexiconEntropy};NA")
        }
      }
    })
  })
  pwt.close()

  val pwr = new PrintWriter(new File("output/results_rounds.csv"))
  pwr.println("pair;round;nrTurns;success")
  allData.foreach(pairData => {
    val pair: Int = pairData._1
    val rounds: List[InteractionData] = pairData._2
    rounds.indices.foreach(round => {
      val nrTurns = rounds(round).initiatorData.length + 1
      val success = rounds(round).initialInitiatorData.intendedReferent == rounds(round).responderData.last.inferredReferent
      pwr.println(s"$pair;$round;$nrTurns;$success")
    })
  })
  pwr.close()

}
