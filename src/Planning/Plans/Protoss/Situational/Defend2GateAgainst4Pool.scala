package Planning.Plans.Protoss.Situational

import Lifecycle.With
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo

class Defend2GateAgainst4Pool extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  override def onUpdate() {
    
    def inOurBase(unit: UnitInfo): Boolean = unit.pixelCenter.zone.bases.exists(_.owner.isUs)
    
    val zealots           = With.units.ours .filter(u => u.aliveAndComplete && u.is(Protoss.Zealot) && inOurBase(u))
    lazy val zerglings    = With.units.enemy.filter(u => u.aliveAndComplete && u.is(Zerg.Zergling)  && inOurBase(u))
    lazy val workers      = With.units.ours.filter(u => u.aliveAndComplete && u.unitClass.isWorker)
    lazy val threatening  = zerglings.filter(_.inPixelRadius(32 * 4).exists(n => n.isOurs && n.totalHealth < 200))
    
    if (zealots.isEmpty) {
      return
    }
    if (zealots.size > 3) {
      return
    }
    if (zerglings.isEmpty) {
      return
    }
    if (threatening.isEmpty) {
      return
    }
    
    val workersNeeded   = 1 + 2 * zerglings.size - 3 * zealots.size
    val workerCap       = workers.size - 4
    val workersToFight  = Math.max(0, Math.min(workerCap, workersNeeded))
    val target          = zealots.minBy(zealot => zerglings.map(_.pixelDistanceFast(zealot)).min).pixelCenter
    
    defenders.get.unitCounter.set(UnitCountExactly(workersToFight))
    defenders.get.acquire(this)
    defenders.get.units.foreach(defender => With.executor.intend(new Intention(this, defender) {
      toTravel = Some(target)
    }))
  }
}
