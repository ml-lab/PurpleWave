package Types.Plans.Generic.Allocation

import Startup.With
import Types.Plans.Plan
import Types.Traits.UnitRequest
import bwapi.Unit

import scala.collection.mutable

abstract class PlanAcquireUnits extends Plan with UnitRequest {
  
  override def isComplete(): Boolean = { requestFulfilled }
  
  override def execute() {
    With.recruiter.add(this)
  }
  
  override def abort() {
    With.recruiter.remove(this)
  }
  
  def units():mutable.Set[Unit] = {
    With.recruiter.getUnits(this)
  }
}