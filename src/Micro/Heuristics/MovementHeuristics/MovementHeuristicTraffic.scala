package Micro.Heuristics.MovementHeuristics

import Lifecycle.With
import Micro.Intent.Intention
import bwapi.TilePosition
object MovementHeuristicTraffic extends MovementHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(intent: Intention, candidate: TilePosition): Double = {
  
    if (intent.unit.flying) 1.0 else measureTraffic(intent, 1.0, candidate)
  }
  
  def measureTraffic(
    intent:Intention,
    multiplier:Double,
    tile:TilePosition)
  :Double = {
    
    multiplier *
    scaling *
    With.grids.units.get(tile)
      .filter(neighbor =>
        neighbor.possiblyStillThere
        && neighbor != intent.unit
        && ! neighbor.flying
        && ! neighbor.unitClass.isBuilding)
      .map(neighbor =>
        Math.min(32.0, neighbor.unitClass.width) *
        Math.min(32.0, neighbor.unitClass.height))
      .sum
  }
  
}