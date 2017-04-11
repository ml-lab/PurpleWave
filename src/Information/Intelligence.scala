package Information

import Information.Geography.Types.Base
import Performance.Caching.CacheFrame
import Lifecycle.With
import bwapi.TilePosition

class Intelligence {
  
  def mostBaselikeEnemyPosition:TilePosition = mostBaselikeEnemyPositionCache.get
  val mostBaselikeEnemyPositionCache = new CacheFrame(() =>
    With.units.enemy
      .toList
      .filterNot(_.flying)
      .sortBy(unit => ! unit.unitClass.isBuilding)
      .sortBy(unit => ! unit.unitClass.isTownHall)
      .map(_.tileIncluding)
      .headOption
      .getOrElse(leastScoutedBases.head.townHallArea.midpoint))
  
  def leastScoutedBases:Iterable[Base] = leastScoutedBasesCache.get
  private val leastScoutedBasesCache = new CacheFrame(() =>
    With.geography.bases
      .toList
      .sortBy( ! _.isStartLocation)
      .sortBy(_.lastScoutedFrame))
}
