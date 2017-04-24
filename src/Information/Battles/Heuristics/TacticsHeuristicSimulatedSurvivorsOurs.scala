package Information.Battles.Heuristics

import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.TacticsOptions
import Mathematics.Heuristics.HeuristicMath

object TacticsHeuristicSimulatedSurvivorsOurs extends TacticsHeuristic {
  
  override def evaluate(context: Battle, candidate: TacticsOptions): Double = {
    
    val simulation = context.simulation(candidate)
    
    if (simulation.isEmpty) return HeuristicMath.default
  
    simulation.get.us.units.filter(_.alive).map(_.unit.subjectiveValue).sum - simulation.get.us.lostValue
  }
}