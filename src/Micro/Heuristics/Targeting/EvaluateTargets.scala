package Micro.Heuristics.Targeting

import Mathematics.Heuristics.Heuristic
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object EvaluateTargets {
  
  def best(unit: FriendlyUnitInfo, targets: Iterable[UnitInfo]): Option[UnitInfo] = {
    val output = ByOption.maxBy(targets)(evaluate(unit, _))
    output
  }
  
  def evaluate(unit: FriendlyUnitInfo, target: UnitInfo): Double = {
    unit.agent.targetingProfile.weightedHeuristics
      .map(_.weighMultiplicatively(unit, target))
      .product
  }
  
  def audit(unit: FriendlyUnitInfo): Seq[(UnitInfo, Double, Iterable[(Heuristic[FriendlyUnitInfo, UnitInfo], Double, Double)])] = {
    val output = unit.matchups.targets.map(target =>
      (
        target,
        EvaluateTargets.evaluate(unit, target),
        unit.agent.targetingProfile.weightedHeuristics.map(h =>
        (
          h.heuristic,
          h.weighMultiplicatively(unit, target),
          h.heuristic.evaluate(unit, target)
        ))
      )
    ).sortBy(_._2)
    output
  }
}
