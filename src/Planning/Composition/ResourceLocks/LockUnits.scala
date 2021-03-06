package Planning.Composition.ResourceLocks

import Planning.Plan
import Lifecycle.With
import Planning.Composition.{Property, UnitCountEverything}
import Planning.Composition.UnitCounters.UnitCounter
import Planning.Composition.UnitMatchers.{UnitMatchAnything, UnitMatcher}
import Planning.Composition.UnitPreferences.{UnitPreferAnything, UnitPreference}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class LockUnits extends ResourceLock {
  
  var canPoach        = new Property[Boolean](false)
  var interruptable   = new Property[Boolean](true)
  val unitMatcher     = new Property[UnitMatcher](UnitMatchAnything)
  val unitPreference  = new Property[UnitPreference](UnitPreferAnything)
  val unitCounter     = new Property[UnitCounter](UnitCountEverything)
  
  var owner: Plan = _
  
  var isSatisfied:Boolean = false
  override def satisfied: Boolean = isSatisfied
  
  override def acquire(plan: Plan) {
    owner = plan
    With.recruiter.add(this)
  }
  
  def inquire(plan: Plan): Option[Iterable[FriendlyUnitInfo]] = {
    val ownerBefore = owner //This is supposed to be free of side-effects so retain the owner
    owner = plan
    val output = With.recruiter.inquire(this)
    owner = ownerBefore
    output
  }
  
  override def release() {
    With.recruiter.remove(this)
  }
  
  def units: Set[FriendlyUnitInfo] = With.recruiter.getUnits(this)
  
  def offerUnits(candidates: Iterable[FriendlyUnitInfo]): Option[Iterable[FriendlyUnitInfo]] = {
  
    val desiredUnits    = With.recruiter.getUnits(this).to[mutable.Set]
    val candidateQueue  = new mutable.PriorityQueue[FriendlyUnitInfo]()(Ordering.by( - unitPreference.get.preference(_))) //Negative because priority queue is highest-first
    candidateQueue ++= candidates.filter(unitMatcher.get.accept)
    
    unitMatcher.get.reset()
    unitCounter.get.reset()
    
    while (unitCounter.get.continue(desiredUnits) && candidateQueue.nonEmpty) {
      desiredUnits += candidateQueue.dequeue()
    }
    
    isSatisfied = unitCounter.get.accept(desiredUnits)
    if (isSatisfied)
      Some(desiredUnits)
    else
      None
  }
}
