package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.adaptive.config.SimConf
import com.markblokpoel.probability4scala.Implicits.ImplDouble
import pureconfig._
import pureconfig.generic.auto._


/** Runs the adaptive model, old version
 *
 */
object MainSimulation extends App {
  // Read configuration
  val source = ConfigSource.file("src/main/resources/application.conf")
  val config = source.load[SimConf]

  // Run experiments

  config match {
    case Right(conf) =>
      if(conf.simulationTypes.contains("non-ostensive"))
        RunAdaptiveExperiment.run(
          conf.nrSignals,
          conf.nrReferents,
          conf.nrPairs,
          conf.maxTurns,
          conf.nrRounds,
          conf.entropyThreshold.toBigNatural,
          conf.order,
          conf.costs.toBigNatural,
          conf.betaOptions.map(_.toBigNatural),
          conf.distributionOptions,
          conf.randomSeed
        )
      if(conf.simulationTypes.contains("ostensive"))
        RunExplicitExperiment.run(
          conf.nrSignals,
          conf.nrReferents,
          conf.nrPairs,
          conf.maxTurns,
          conf.nrRounds,
          conf.entropyThreshold.toBigNatural,
          conf.order,
          conf.costs.toBigNatural,
          conf.betaOptions.map(_.toBigNatural),
          conf.distributionOptions,
          conf.randomSeed
        )
    case Left(failures) =>
      // cannot read config, print error
      println("Cannot read configuration file.\n" + failures.toList.mkString("\n"))

  }
}
