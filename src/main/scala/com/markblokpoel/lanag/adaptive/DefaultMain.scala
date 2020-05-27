package com.markblokpoel.lanag.adaptive

object DefaultMain extends App {

//  val signals: Set[StringSignal] = Set(StringSignal("s1"), StringSignal("s2"), StringSignal("s3"))
//  val referents: Set[StringReferent] = Set(StringReferent("r1"), StringReferent("r2"))
//  val history: List[(StringSignal, StringSignal)] = List((StringSignal("s1"),StringSignal("s2")))
//
//  val aa = AdaptiveAgent(
//    1,
//    signals,
//    referents,
//    history,
//    1.0,
//    0.0
//  )

  // java.lang.ArithmeticException: Division undefined
  // for aa.s

  val signals = (1 to 4).toSet.map((s: Int) => StringSignal("s"+ s))
  val referents = (1 to 3).toSet.map((s: Int) => StringReferent("s"+ s))

  val lexs = Lexicon.allPossibleLexicons(signals, referents)
  println(lexs.size)

  println(math.pow(2, 16))
}
