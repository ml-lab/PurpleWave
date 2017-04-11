package Information.Battles

import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Information.Geography.Types.Zone
import Performance.Caching.Limiter
import Lifecycle.With
import Utilities.EnrichPosition._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Battles {
  
  private val delayLength = 1
  
  private var combatantsOurs  : Set[FriendlyUnitInfo] = Set.empty
  private var combatantsEnemy : Set[ForeignUnitInfo]  = Set.empty
          var global          : Battle                = null
          var byZone          : Map[Zone, Battle]     = Map.empty
          var byUnit          : Map[UnitInfo, Battle] = Map.empty
          var local           : List[Battle]          = List.empty
  
  def onFrame() = updateLimiter.act()
  private val updateLimiter = new Limiter(delayLength, update)
  private def update() {
    combatantsOurs  = With.units.ours .filter(unit => unit.unitClass.helpsInCombat)
    combatantsEnemy = With.units.enemy.filter(unit => unit.unitClass.helpsInCombat && unit.possiblyStillThere)
    buildBattleGlobal()
    buildBattlesByZone()
    buildBattlesLocal()
    BattleUpdater.assess(local ++ byZone.values :+ global)
  }
  
  private def buildBattleGlobal() {
    global = new Battle(
      new BattleGroup(upcastOurs(combatantsOurs)),
      new BattleGroup(upcastEnemy(combatantsEnemy)))
  }
  
  private def buildBattlesByZone() {
    val combatantsOursByZone  = combatantsOurs .groupBy(_.tileIncluding.zone)
    val combatantsEnemyByZone = combatantsEnemy.groupBy(_.tileIncluding.zone)
    byZone = With.geography.zones
      .map(zone => (
        zone,
        new Battle(
          new BattleGroup(upcastOurs(combatantsOursByZone .getOrElse(zone, Set.empty))),
          new BattleGroup(upcastEnemy(combatantsEnemyByZone.getOrElse(zone, Set.empty)))
        )))
      .toMap
  }
  
  private def buildBattlesLocal() {
    if (combatantsEnemy.isEmpty) {
      byUnit = Map.empty
      return
    }
    
    val framesToLookAhead = Math.max(12, 2 * With.performance.frameDelay(delayLength))
    val unassigned = mutable.HashSet.empty ++ combatantsOurs ++ combatantsEnemy
    val clusters = new ListBuffer[mutable.HashSet[UnitInfo]]
    val horizon = new mutable.HashSet[UnitInfo]
    val searchMarginTiles = 2
    val searchRadius = searchMarginTiles + Math.min(With.configuration.combatEvaluationDistanceTiles, unassigned.map(_.pixelReachTotal(framesToLookAhead)).max/32 + 1)
    while (unassigned.nonEmpty) {
      val nextCluster = new mutable.HashSet[UnitInfo]
      horizon.add(unassigned.head)
      while (horizon.nonEmpty) {
        val nextUnit = horizon.head
        horizon.remove(nextUnit)
        unassigned.remove(nextUnit)
        nextCluster.add(nextUnit)
        horizon ++= nextUnit
          .inTileRadius(With.configuration.combatEvaluationDistanceTiles)
          .filter(unassigned.contains)
          .filter(otherUnit =>
            nextUnit.pixelsFromEdgeFast(otherUnit) <=
              32.0 * searchMarginTiles +
              Math.max(
                nextUnit.pixelReachTotal(framesToLookAhead),
                otherUnit.pixelReachTotal(framesToLookAhead)))
      }
      clusters.append(nextCluster)
    }
    local =
      clusters
        .map(cluster =>
          new Battle(
            new BattleGroup(cluster.filter(_.isOurs).toSet),
            new BattleGroup(cluster.filter(_.isEnemy).toSet)))
        .filter(battle =>
          battle.us.units.nonEmpty &&
          battle.enemy.units.nonEmpty)
        .toList
    byUnit = local
      .flatten(battle =>
        List(
          battle.us.units,
          battle.enemy.units)
        .flatten
        .map(unit => (unit, battle)))
      .toMap
  }

  def upcastOurs  (units:Set[FriendlyUnitInfo]) : Set[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
  def upcastEnemy (units:Set[ForeignUnitInfo])  : Set[UnitInfo] = units.map(_.asInstanceOf[UnitInfo])
}
