package Micro.Heuristics.Movement

import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Pixels.{Pixel, Tile}
import Micro.Intent.Intention
object MovementHeuristicTraffic extends MovementHeuristic {
  
  val scaling = 1.0 / 32.0 / 32.0
  
  override def evaluate(intent: Intention, candidate: Pixel): Double = {
  
    if (intent.unit.flying) return HeuristicMathMultiplicative.default
    
    val m0 = 1.00
    val m1 = 0.80
    val m2 = 0.70
    val m3 = 0.50
    
    val tile = candidate.tileIncluding
    
    Vector(
      measureTraffic(intent, m0, tile),
      measureTraffic(intent, m1, tile.add(-1,  0)),
      measureTraffic(intent, m1, tile.add( 1,  0)),
      measureTraffic(intent, m1, tile.add( 0, -1)),
      measureTraffic(intent, m1, tile.add( 0, -1)),
      measureTraffic(intent, m2, tile.add(-1, -1)),
      measureTraffic(intent, m2, tile.add( 1, -1)),
      measureTraffic(intent, m2, tile.add(-1,  1)),
      measureTraffic(intent, m2, tile.add( 1,  1)),
      measureTraffic(intent, m3, tile.add(-2, -1)),
      measureTraffic(intent, m3, tile.add(-2,  0)),
      measureTraffic(intent, m3, tile.add(-2,  1)),
      measureTraffic(intent, m3, tile.add( 2, -1)),
      measureTraffic(intent, m3, tile.add( 2,  0)),
      measureTraffic(intent, m3, tile.add( 2,  1)),
      measureTraffic(intent, m3, tile.add(-1, -2)),
      measureTraffic(intent, m3, tile.add( 0, -2)),
      measureTraffic(intent, m3, tile.add( 1, -2)),
      measureTraffic(intent, m3, tile.add(-1,  2)),
      measureTraffic(intent, m3, tile.add( 0,  2)),
      measureTraffic(intent, m3, tile.add( 1,  2))
    ).sum
  }
  
  def measureTraffic(
    intent:Intention,
    multiplier:Double,
    candidate:Tile)
  :Double = {
    
    multiplier *
    scaling *
    With.grids.units.get(candidate)
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
