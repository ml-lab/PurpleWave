package Planning.Plans.Macro.Automatic

import Planning.Plan
import Startup.With
import Macro.BuildRequests.RequestUnitAtLeast
import Performance.Caching.CacheFrame
import bwapi.UnitType

abstract class AbstractBuildContinuously extends Plan {
  
  def _totalRequired:Int
  def _unitType:UnitType
  
  override def isComplete:Boolean = totalRequired == 0
  override def onFrame() = With.scheduler.request(this, List(new RequestUnitAtLeast(totalRequired, _unitType)))
  
  def totalRequired:Int = _totalRequiredCache.get
  val _totalRequiredCache = new CacheFrame(() => _totalRequired)
}