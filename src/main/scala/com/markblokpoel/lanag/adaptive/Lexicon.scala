package com.markblokpoel.lanag.adaptive

import com.markblokpoel.probability4scala.ConditionalDistribution

case class Lexicon(signals: List[StringSignal], referents: List[StringReferent], data: List[List[Double]]) {
  require(data.nonEmpty, "data is empty")
  require(data.head.nonEmpty, "data mustn't contain non empty referents")
  require(data.length == signals.length,
    s"the lexicon data (len ${data.length}) must contain exactly the right number of signals  (len ${signals.length})")
  require({
    data.forall(_.length == data.head.length)
  }, "each signal should refer to the same number of referents")
  require({
    data.forall(_.length == referents.length)
  }, "each signal should refer to the correct number of referents")

  val vocabularySize: Int = data.length
  val contextSize: Int = data.head.length


  def deltaL: ConditionalDistribution[StringReferent, StringSignal] = {
    val newData = data.map((conditionalProbabilities: List[Double]) => {
      val sum = conditionalProbabilities.sum
      conditionalProbabilities.map {
        case 0 => 0
        case x => x / sum
      }
    })

    val map: Map[(StringReferent, StringSignal), BigDecimal]=
      (for(ri <- referents.indices; si <- signals.indices) yield {
        (referents(ri), signals(si)) -> BigDecimal(newData(si)(ri))
      }).toMap

    ConditionalDistribution(referents.toSet, signals.toSet, map)
  }

  def deltaS: ConditionalDistribution[StringSignal, StringReferent] = {
    val sumColumn = Array.fill(contextSize)(0.0)
    for (i <- 0 until vocabularySize) {
      for (j <- 0 until contextSize) {
        sumColumn(j) = sumColumn(j) + data(i)(j)
      }
    }
    val newData = data.map((conditionalProbabilities: List[Double]) => {
      (conditionalProbabilities zip sumColumn) map {
        case (_,0) => 0
        case (x,y) => x / y
      }
    })

    val map: Map[(StringSignal, StringReferent), BigDecimal]=
      (for(ri <- referents.indices; si <- signals.indices) yield {
        (signals(si), referents(ri)) -> BigDecimal(newData(si)(ri))
      }).toMap

    ConditionalDistribution(signals.toSet, referents.toSet, map)
  }

  def maxIndexOfColumn(column: Int): Int = {
    // find maximum in column $column, and return max value and its index
    val columnValues = Array.ofDim[Double](vocabularySize)
    for(i <- 0 until vocabularySize) {
      columnValues(i) = data(i)(column)
    }
    //		TODO: this still returns the FIRST max value element, Hawkins' model uses random max value
    //		(columnValues.max, columnValues.indexOf(columnValues.max))
    columnValues.indexOf(columnValues.max)
  }

  //TODO: create pretty printing function
  def printLexicon(): Unit = println(data.toString())
  /*
    round the numbers 2 decimals
    each row on a newline
   */
}

case object Lexicon {
  def allPossibleLexicons(signals: List[StringSignal], referents: List[StringReferent]): Set[Lexicon] = {
    val length1d = signals.length * referents.length

    def apl(curLength: Int): Set[List[Double]] = {
      if(curLength == length1d) Set(List.empty)
      else {
        val rest = apl(curLength+1)
        val left = rest.map(0.0 :: _)
        val right = rest.map(1.0 :: _)
        left ++ right
      }
    }

    apl(0)
      .map(lex1d => Lexicon(signals, referents, lex1d.sliding(referents.length, referents.length).toList))
  }
}