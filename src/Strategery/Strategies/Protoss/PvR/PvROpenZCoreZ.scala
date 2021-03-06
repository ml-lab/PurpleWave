package Strategery.Strategies.Protoss.PvR

import Strategery.Strategies.Protoss.ProtossChoices
import Strategery.Strategies.Strategy
import bwapi.Race

object PvROpenZCoreZ extends Strategy {
  
  override lazy val choices = Vector(
    ProtossChoices.pvtOpenersTransitioningFrom1Gate,
    ProtossChoices.pvpOpenersTransitioningFrom2Gate,
    ProtossChoices.pvzOpenersTransitioningFrom1Gate)
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Unknown)
}
