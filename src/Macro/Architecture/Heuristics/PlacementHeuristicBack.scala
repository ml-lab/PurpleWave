package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Tile

object PlacementHeuristicBack extends PlacementHeuristic {
  
  override def evaluate(state: BuildingDescriptor, candidate: Tile): Double = {
    
    HeuristicMathMultiplicative.default
    
  }
  
}
