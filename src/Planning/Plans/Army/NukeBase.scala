package Planning.Plans.Army

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Micro.Squads.Goals.SquadDrop
import Micro.Squads.Squad
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchTransport
import Planning.Plan
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class NukeBase extends Plan {
  
  private val nukerLock = new Property(new LockUnits)
  nukerLock.get.unitMatcher.set(Terran.Ghost)
  nukerLock.get.unitCounter.set(UnitCountOne)
  nukerLock.get.interruptable.set(false)
  
  private val nukeLock = new Property(new LockUnits)
  nukeLock.get.unitMatcher.set(Terran.NuclearMissile)
  nukeLock.get.unitCounter.set(UnitCountOne)
  nukeLock.get.interruptable.set(false)
  
  private val transportLock = new Property(new LockUnits)
  transportLock.get.unitMatcher.set(UnitMatchTransport)
  transportLock.get.unitCounter.set(UnitCountOne)
  transportLock.get.interruptable.set(false)
  
  val squad = new Squad(this)
  
  override def onUpdate() {
    nukeLock.get.acquire(this)
    if ( ! nukeLock.get.isSatisfied) return
    
    nukerLock.get.acquire(this)
    val nukers = nukerLock.get.units
    if (nukers.isEmpty) {
      nukeLock.get.release()
      return
    }
    val targets = With.geography.enemyBases.map(base => (base, evaluateBase(nukers.head, base)))
    val targetBest = ByOption.maxBy(targets)(_._2)
    
    if (targetBest.exists(_._2 >= 0)) {
      val targetPixel = targetBest.get._1.heart.pixelCenter
      squad.conscript(transportLock.get.units)
      nukers.foreach(squad.recruit)
      squad.goal = new SquadDrop(targetPixel)
      nukers.foreach(nuker => nuker.agent.intend(this, new Intention {
        toNuke = Some(targetPixel)
      }))
    }
    else {
      nukeLock.get.release()
      nukerLock.get.release()
    }
  }
  
  private def evaluateBase(nuker: FriendlyUnitInfo, base: Base): Double = {
    val value     = base.workers.size.toDouble
    val distance  = 1.0 + nuker.pixelDistanceTravelling(base.heart.pixelCenter.project(nuker.pixelCenter, 32.0 * 7.5))
    val safety    = (1.0 +  base.defenders.size) * (1.0 + base.units.count(_.unitClass.isDetector))
    val output    = value / safety / distance
    output
  }
  
}
