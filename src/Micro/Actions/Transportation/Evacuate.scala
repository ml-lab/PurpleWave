package Micro.Actions.Transportation

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Evacuate extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.isTransport                                  &&
    unit.loadedUnits.nonEmpty                         &&
    With.grids.walkable.get(unit.tileIncludingCenter) &&
    unit.matchups.framesToLiveCurrently < unit.loadedUnits.size * 24 * 4
  }
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    With.commander.unload(unit, unit.loadedUnits.head)
  }
}
