package Micro.Heuristics.Movement

import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Decisions.MicroValue
import Micro.Execution.ActionState

object MovementHeuristicVpfDealing extends MovementHeuristic {
  
  override def evaluate(state: ActionState, candidate: Pixel): Double = {
  
    val targetsAt = state.unit.matchups.targets.filter(target =>
      state.unit.pixelRangeAgainstFromCenter(target) >=
      target.pixelDistanceFast(candidate))
    
    if (targetsAt.isEmpty) return HeuristicMathMultiplicative.default
    
    targetsAt.map(target => state.unit.dpfOnNextHitAgainst(target) * MicroValue.valuePerDamage(target)).max
  }
}
