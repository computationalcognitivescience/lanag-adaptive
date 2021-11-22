package com.markblokpoel.lanag.adaptive

import com.markblokpoel.lanag.adaptive.config.SimConf
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



    case Left(failures) =>
      // cannot read config, print error
      println("Cannot read configuration file.\n" + failures.toList.mkString("\n"))
    
  }
}
