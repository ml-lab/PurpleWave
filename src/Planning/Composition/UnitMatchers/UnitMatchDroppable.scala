package Planning.Composition.UnitMatchers

import Planning.Composition.UnitPreferences.UnitPreferDroppable
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

object UnitMatchDroppable extends UnitMatcher {
  
  override def accept(unit: UnitInfo): Boolean = (
    unit.aliveAndComplete
    && UnitPreferDroppable.preferenceOrder.contains(unit.unitClass)
    && ( ! unit.is(Protoss.HighTemplar) || (unit.energy >= Protoss.PsionicStorm.energyCost - 5 && unit.player.hasTech(Protoss.PsionicStorm)))
  )
}
