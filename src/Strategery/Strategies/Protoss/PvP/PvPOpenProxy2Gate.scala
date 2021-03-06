package Strategery.Strategies.Protoss.PvP

import Strategery.Maps.{MapGroups, StarCraftMap}
import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvPOpenProxy2Gate extends Strategy {
  
  override def choices: Iterable[Iterable[Strategy]] = Vector(ProtossChoices.pvpOpenersTransitioningFrom2Gate)
  
  override def ourRaces   : Iterable[Race]  = Vector(Race.Protoss)
  override def enemyRaces : Iterable[Race]  = Vector(Race.Protoss)
  
  override def prohibitedMaps: Iterable[StarCraftMap] = MapGroups.badForProxying
}
