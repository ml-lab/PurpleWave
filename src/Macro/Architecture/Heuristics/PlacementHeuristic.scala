package Macro.Architecture.Heuristics

import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.Heuristic
import Mathematics.Points.Tile

abstract class PlacementHeuristic extends Heuristic[BuildingDescriptor, Tile] {
  
  def evaluate(state: BuildingDescriptor, candidate: Tile): Double
  
}
