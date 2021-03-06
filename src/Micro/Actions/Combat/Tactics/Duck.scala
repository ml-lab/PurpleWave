package Micro.Actions.Combat

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Duck extends Action {
  
  val burrowFrames = 6
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    ! unit.agent.shouldEngage
    && With.self.hasTech(Zerg.Burrow)
    && Vector(Zerg.Zergling, Zerg.Hydralisk, Zerg.Defiler).contains(unit.unitClass)
    && unit.matchups.enemyDetectors.isEmpty
    && (unit.matchups.framesToLiveDiffused < Math.max(16, unit.matchups.framesOfEntanglementDiffused)
      || unit.matchups.framesOfEntanglementPerThreatDiffused.exists(pair => pair._1.topSpeed > unit.topSpeed && pair._2 > -burrowFrames))
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    With.commander.burrow(unit)
  }
}
