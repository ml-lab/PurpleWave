package Micro.Decisions

import Information.Grids.AbstractGrid
import Lifecycle.With
import Mathematics.Physics.{BuildForce, Force}
import Mathematics.Points.{Pixel, Tile}
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object PotentialFieldMath {
  
  def threatForce(unit: FriendlyUnitInfo): Force = {
    val threats     = unit.matchups.threats.filterNot(_.is(Protoss.Interceptor))
    val forces      = threats.map(singleThreatForce(unit, _))
    val forceSum    = forces.reduce(_ + _)
    val output      = forceSum.normalize
    output
  }
  
  def singleThreatForce(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val magnitudeDamage   = threat.dpfOnNextHitAgainst(unit)
    val magnitudeDistance = Math.max(1.0, threat.framesToGetInRange(unit)) //This may fail vs. static defense -- we may want a bit more force
    val magnitudeFinal    = magnitudeDamage / magnitudeDistance
    val output            = BuildForce.fromPixels(threat.pixelCenter, unit.pixelCenter, magnitudeFinal)
    output
  }
  
  def detectionForce(unit: FriendlyUnitInfo): Force = {
    val threats     = unit.matchups.enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
    val forces      = threats.map(singleDetectorForce(unit, _))
    val forceSum    = forces.reduce(_ + _)
    val output      = forceSum.normalize
    output
  }
  
  def singleDetectorForce(unit: FriendlyUnitInfo, threat: UnitInfo): Force = {
    val magnitudeDistance = Math.max(1.0, threat.framesToGetInRange(unit)) //This may fail vs. static defense -- we may want a bit more force
    val magnitudeFinal    = 1.0 / magnitudeDistance
    val output            = BuildForce.fromPixels(threat.pixelCenter, unit.pixelCenter, magnitudeFinal)
    output
  }
  
  def mobilityForce(unit: FriendlyUnitInfo): Force = {
    if (unit.flying)
      mobilityForce(unit.pixelCenter, 12, With.grids.mobilityBorder)
    else
      mobilityForce(unit.pixelCenter, unit.pixelCenter.zone.maxMobility, With.grids.mobility)
  }
  
  def mobilityForce(pixel: Pixel, maxMobility: Int, mobilitySource: AbstractGrid[Int]): Force = {
    val tile                = pixel.tileIncluding
    val mobility            = mobilitySource.get(tile)
    val forces              = tile.adjacent8.filter(_.valid).map(neighbor => singleMobilityForce(tile, neighbor, mobilitySource))
    val totalForce          = forces.reduce(_ + _)
    val magnitude           = 2.0 * Math.max(0.0, 1.0 - 2 * mobility / maxMobility.toDouble)
    val output              = totalForce.normalize(magnitude)
    output
  }
  
  def singleMobilityForce(here: Tile, there: Tile, mobilitySource: AbstractGrid[Int]): Force = {
    val mobilityHere  = mobilitySource.get(here)
    val mobilityThere = mobilitySource.get(there)
    val magnitude     = PurpleMath.signum(mobilityHere - mobilityThere)
    val output        = BuildForce.fromPixels(there.pixelCenter, here.pixelCenter, magnitude.toInt)
    output
  }
  
  def spreadingForce(unit: FriendlyUnitInfo): Force = {
    if (unit.flying) return new Force
    
    val blockers        = unit.matchups.allies.filterNot(_.flying)
    val nearestBlocker  = ByOption.minBy(blockers)(_.pixelsFromEdgeFast(unit))
    
    if (nearestBlocker.isEmpty) return new Force
    
    val maximumDistance = Math.max(32.0, unit.pixelRangeMax)
    val blockerDistance = nearestBlocker.get.pixelsFromEdgeFast(unit)
    val magnitude       = Math.max(0.0, 1.0 - blockerDistance / maximumDistance)
    val output          = BuildForce.fromPixels(nearestBlocker.get.pixelCenter, unit.pixelCenter, magnitude)
    output
  }
  
  def exitForce(unit: FriendlyUnitInfo): Force = {
    val destination = unit.agent.origin.zone
    
    val path = With.paths.zonePath(
      unit.pixelCenter.zone,
      destination)
    
    if (path.isEmpty || path.get.steps.isEmpty) return new Force
    
    BuildForce.fromPixels(unit.pixelCenter, path.get.steps.head.edge.centerPixel, 1.0)
  }
  
  def sumForces(forces: Traversable[Force], origin: Pixel): Pixel = {
    val forceTotal  = forces.reduce(_ + _)
    val forceNormal = forceTotal.normalize(85.0)
    val forcePoint  = forceNormal.toPoint
    val output      = origin.add(forcePoint)
    output
  }
}
