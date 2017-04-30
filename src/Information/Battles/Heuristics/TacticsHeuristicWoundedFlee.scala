package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicWoundedFlee extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    HeuristicMath.fromBoolean(candidate.has(Tactics.Wounded.Flee))
    
  }
}