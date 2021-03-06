package Planning.Plans.Scouting

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Plan
import Planning.Plans.Predicates.Milestones.EnemyHasShownCloakedThreat
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class ChillOverlords extends Plan {
  
  val overlords = new LockUnits
  overlords.unitMatcher.set(Zerg.Overlord)
  overlords.unitCounter.set(UnitCountEverything)
  
  val cloakedThreat = new EnemyHasShownCloakedThreat
  override def onUpdate() {
    
    if (With.self.hasUpgrade(Zerg.OverlordSpeed)) {
      return
    }
    if (cloakedThreat.isComplete) {
      return
    }
    
    overlords.acquire(this)
    overlords.units.foreach(chillOut)
  }
  
  private def chillOut(overlord: FriendlyUnitInfo) {
    val intent = new Intention
    intent.toTravel = Some(ByOption.minBy(With.geography.bases.map(_.heart.pixelCenter))(overlord.pixelDistanceSquared).getOrElse(With.geography.home.pixelCenter))
    intent.canCower = true
    overlord.agent.intend(this, intent)
  }
  
}
