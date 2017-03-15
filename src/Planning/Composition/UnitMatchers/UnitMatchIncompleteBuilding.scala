package Planning.Composition.UnitMatchers

import BWMirrorProxy.UnitInfo.FriendlyUnitInfo
import bwapi.UnitType

class UnitMatchIncompleteBuilding(unitType:UnitType) extends UnitMatchType(unitType) {
  override def accept(unit: FriendlyUnitInfo): Boolean = {
    unit.utype == unitType &&
      ! unit.complete &&
      unit.getBuildUnit.nonEmpty
  }
}