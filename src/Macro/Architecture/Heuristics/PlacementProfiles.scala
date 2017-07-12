package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint

object PlacementProfiles {
  
  val pylon = new PlacementProfile(
    "Pylon",
    preferZone                  = 3.0,
    preferSpace                 = 0.1,
    preferPowering              = 1.0,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 0.25,
    avoidDistanceFromIdealRange  = 0.25
  )
  
  val factory = new PlacementProfile(
    "Factory",
    preferZone                  = 1.0,
    preferSpace                 = 0.5,
    avoidDistanceFromBase       = 2.0,
    avoidDistanceFromIdealRange  = 2.0
  )
  
  val tech = new PlacementProfile(
    "Tech",
    preferZone                  = 1.0,
    preferDistanceFromEnemy     = 3.0,
    avoidDistanceFromBase       = 1.0)
  
  val gas = new PlacementProfile(
    "Gas",
    preferZone                  = 100.0,
    preferDistanceFromEnemy     = 1.0,
    avoidDistanceFromBase       = 1.0)
  
  val townHall = new PlacementProfile(
    "Town Hall",
    preferNatural               = 6.0,
    preferGas                   = 1.0,
    preferDistanceFromEnemy     = 1.5,
    avoidDistanceFromBase       = 2.0)
  
  val naturalCannonPylon = new PlacementProfile(
    "Pylon for natural Cannons",
    preferPowering              = 0.5,
    preferDistanceFromEnemy     = 0.5,
    preferCoveringWorkers       = 0.5,
    avoidDistanceFromBase       = 1.5,
    avoidSurfaceArea            = 0.25,
    avoidDistanceFromIdealRange = 5.0)
  
  val naturalCannon = new PlacementProfile(
    "Natural cannons",
    avoidSurfaceArea            = 0.25,
    avoidDistanceFromIdealRange = 5.0)
  
  val mineralCannon = new PlacementProfile(
    "Pylon for mineral line Cannons",
    preferPowering              = 0.1,
    preferCoveringWorkers       = 1.0)
  
  val wall = new PlacementProfile(
    "Ground defense",
    preferCoveringWorkers       = 0.25,
    avoidDistanceFromBase       = 0.25,
    avoidSurfaceArea            = 1.0,
    avoidDistanceFromIdealRange = 2.0)
  
  var proxy = new PlacementProfile(
    "Proxy",
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 1.0)
  
  var proxyPylon = new PlacementProfile(
    "Proxy Pylon",
    preferPowering              = 1.0,
    avoidDistanceFromBase       = 1.0,
    avoidDistanceFromEnemy      = 1.0)
  
  def default(blueprint: Blueprint): PlacementProfile = {
    if (blueprint.townHall)
      townHall
    else if (blueprint.gas)
      gas
    else if (blueprint.powers)
      pylon
    else if (blueprint.building.exists(_.trainsGroundUnits))
      factory
    else if (blueprint.building.exists(_.canAttack) || blueprint.wall)
      wall
    else
      tech
  }
}
