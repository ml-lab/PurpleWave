package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Commands.Gravitate
import Micro.Decisions.Potential
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Smuggle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && unit.loadedUnits.nonEmpty
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    if (unit.matchups.threats.exists(_.framesBeforeAttacking(unit) < 24)) {
      OldAvoid.delegate(unit)
    }
    else if (unit.visibleToOpponents && unit.matchups.threats.nonEmpty) {
      val threatMagnitude = if (unit.matchups.threatsInRange.nonEmpty) 2.0 else 1.0
      val forceThreat     = Potential.threatsRepulsion(unit).normalize(threatMagnitude)
      val forceSmuggling  = Potential.smuggleRepulsion(unit)
      val forceHeading    = ForceMath.fromPixels(unit.pixelCenter, unit.agent.toTravel.getOrElse(unit.pixelCenter))
      unit.agent.forces.put(ForceColors.threat,     forceThreat)
      unit.agent.forces.put(ForceColors.bypassing,  forceSmuggling)
      unit.agent.forces.put(ForceColors.traveling,  forceHeading)
      Gravitate.delegate(unit)
    }
    
  }
}
