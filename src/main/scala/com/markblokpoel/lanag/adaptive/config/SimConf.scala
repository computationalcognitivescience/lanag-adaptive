package com.markblokpoel.lanag.adaptive.config

case class SimConf(
                    nrSignals: Int,
                    nrReferents: Int,
                    nrPairs: Int,
                    maxTurns: Int,
                    nrRounds: Int,
                    entropyThreshold: Double,
                    order: Int,
                    costs: Double,
                    betaOptions: List[Double],
                    distributionOptions: List[Double],
                    ostensiveSim: Boolean,
                    nonOstensiveSim: Boolean,
                    randomSeed: Option[Long]
                  )
