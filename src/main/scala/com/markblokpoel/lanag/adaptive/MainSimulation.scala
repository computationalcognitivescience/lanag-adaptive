package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.adaptive.config.SimConf
import com.markblokpoel.probability4scala.Implicits.ImplDouble
import pureconfig._
import pureconfig.generic.auto._

/**
 *
 */
object MainSimulation extends App {
  // Read configuration
  val source = ConfigSource.file("./application.conf")
  val config = source.load[SimConf]

  // Output folder name
  def outputFolderRoot(nrSignals: Int, nrReferents: Int, nrPairs: Int): String = {
    def uuid = java.util.UUID.randomUUID.toString
    s"./output/S${nrSignals}_R${nrReferents}_N${nrPairs}_${uuid}"
  }

  // Run experiments
  config match {
    case Right(conf) =>

      // ./output/sim_{parameters}/betaX_distrY/files...

      if (conf.nonOstensiveSim)
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
      if (conf.ostensiveSim)
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
      println(
        "Cannot read configuration file.\n" + failures.toList.mkString("\n"))
  }
}
