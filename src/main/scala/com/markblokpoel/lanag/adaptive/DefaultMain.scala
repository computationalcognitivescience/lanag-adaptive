package com.markblokpoel.lanag.adaptive

object DefaultMain extends App {

  val signals = List(StringSignal("s1"), StringSignal("s2"), StringSignal("s3"))
  val referents = List(StringReferent("r1"), StringReferent("r2"))

  val apl = Lexicon.allPossibleLexicons(signals, referents)

  println(apl.mkString("\n"))
}
