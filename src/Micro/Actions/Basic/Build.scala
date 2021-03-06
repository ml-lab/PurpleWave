package Micro.Actions.Basic

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Target
import Micro.Actions.Combat.Decisionmaking.{Fight, FightOrFlight}
import Micro.Actions.Commands.{Attack, Move}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Build extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toBuild.isDefined &&
    unit.agent.toBuildTile.isDefined
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    
    val ourBuilding = With.grids.units.get(unit.agent.toBuildTile.get).find(_.unitClass == unit.agent.toBuild.get)
    
    if (ourBuilding.isDefined) {
      unit.agent.toGather = ByOption.minBy(With.geography.ourBases.flatMap(_.minerals))(_.pixelDistanceCenter(unit.pixelCenter))
      Gather.consider(unit)
      return
    }
    
    val distance  = unit.pixelDistanceCenter(unit.agent.toBuildTile.get.pixelCenter)
    val buildArea = unit.agent.toBuild.get.tileArea.add(unit.agent.toBuildTile.get)
    
    def blockersForTile(tile: Tile) = {
      With.grids.units
        .get(tile)
        .filter(blocker =>
          blocker != unit
          && ! blocker.unitClass.isGas
          && ! blocker.flying
          && blocker.likelyStillThere)
    }
    
    val ignoreBlockers = distance > 32.0 * 8.0
    lazy val blockersIn       = if (ignoreBlockers) Seq.empty else buildArea.tiles.flatMap(blockersForTile).toSeq
    lazy val blockersNear     = if (ignoreBlockers) Seq.empty else buildArea.expand(2, 2).tiles.flatMap(blockersForTile).toSeq
    lazy val blockersOurs     = blockersNear.filter(_.isOurs)
    lazy val blockersEnemy    = blockersNear.filter(_.isEnemy)
    lazy val blockersMineral  = blockersIn.filter(_.unitClass.isMinerals)
    lazy val blockersNeutral  = blockersIn.filter(blocker => blocker.isNeutral && ! blockersMineral.contains(blocker)).filterNot(_.invincible)
    lazy val blockersToKill   = if (blockersEnemy.nonEmpty) blockersEnemy else blockersNeutral
    
    if (blockersMineral.nonEmpty && blockersEnemy.isEmpty) {
      unit.agent.toGather = Some(blockersMineral.head)
      Gather.delegate(unit)
    }
    else if(blockersToKill.nonEmpty) {
      unit.agent.canFight = true
      lazy val noThreats  = unit.matchups.threats.isEmpty
      lazy val allWorkers = unit.matchups.threats.size == 1 && unit.matchups.threats.head.unitClass.isWorker
      lazy val healthy    = unit.totalHealth > 10 || unit.totalHealth >= unit.matchups.threats.head.totalHealth
      if (noThreats || (allWorkers && healthy)) {
        Target.delegate(unit)
        unit.agent.toAttack = unit.agent.toAttack.orElse(Some(blockersToKill.minBy(_.pixelDistanceEdge(unit))))
        Attack.delegate(unit)
      }
      else {
        FightOrFlight.consider(unit)
        Fight.consider(unit)
      }
    }
    else {
      blockersOurs.flatMap(_.friendly).filter(_.matchups.framesOfSafetyDiffused > GameTime(0, 1)()).foreach(_.agent.shove(unit))
      
      val buildPixel = unit.agent.toBuildTile.get.pixelCenter
      val waypoint = unit.agent.nextWaypoint(buildPixel)
      if (
        unit.zone != buildPixel.zone
        && unit.pixelDistanceCenter(buildPixel) > 32.0 * 5.0
        && unit.pixelDistanceCenter(waypoint) > unit.unitClass.haltPixels) {
        unit.agent.toTravel = Some(waypoint)
        Move.delegate(unit)
      }
      With.commander.build(unit, unit.agent.toBuild.get, unit.agent.lastIntent.toBuildTile.get)
    }
  }
}
