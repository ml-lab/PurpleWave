package Strategery.Strategies.Options.Protoss.VsZerg

import Strategery.Strategies.Strategy
import bwapi.Race

object EarlyFFE extends Strategy {
  
  override def ourRaces    : Iterable[Race] = Vector(Race.Random, Race.Protoss)
  override def enemyRaces  : Iterable[Race] = Vector(Race.Terran)
}
