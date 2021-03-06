package Strategery.Strategies.Zerg.ZvE

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.ProxyHatch
import Strategery.Strategies.Strategy
import bwapi.Race

object ProxyHatchZerglings extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = Some(new ProxyHatch)
  
  override def enemyRaces: Iterable[Race] = Vector(Race.Unknown, Race.Terran, Race.Protoss)
}
