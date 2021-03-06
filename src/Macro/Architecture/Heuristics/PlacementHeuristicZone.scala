package Macro.Architecture.Heuristics

import Macro.Architecture.Blueprint
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicZone extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    
    val candidateZone = candidate.zone
    val zoneMatches =
      if (blueprint.preferZone.isDefined)
        blueprint.preferZone.contains(candidateZone)
      else
        candidateZone.owner.isFriendly
    
    HeuristicMathMultiplicative.fromBoolean(zoneMatches)
  }
}
