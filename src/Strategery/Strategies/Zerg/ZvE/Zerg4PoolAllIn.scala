package Strategery.Strategies.Zerg.ZvE

import Planning.Plan
import Planning.Plans.GamePlans.Zerg.ZvE.Zerg4Pool
import Strategery.Strategies.Strategy
import bwapi.Race

object Zerg4PoolAllIn extends Strategy {
  
  override def ourRaces: Iterable[Race] = Vector(Race.Zerg)
  
  override def gameplan: Option[Plan] = Some(new Zerg4Pool)
}