package Micro.Actions.Combat.Maneuvering

import Debugging.Visualizations.ForceColors
import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Gravitate}
import Micro.Agency.OldExplosion
import Micro.Decisions.Potential
import Planning.Yolo
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable.ListBuffer

object Duck extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.canMove && ! Yolo.active
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    unit.agent.explosions ++= getExplosions(unit)
    
    if (unit.agent.explosions.nonEmpty) {
      val forceExplosion  = Potential.explosionsRepulsion(unit)
      val forceMobility   = Potential.mobilityAttraction(unit).normalize
      val forceSpreading  = Potential.collisionRepulsion(unit)
      unit.agent.forces.put(ForceColors.threat,     forceExplosion)
      unit.agent.forces.put(ForceColors.mobility,   forceMobility)
      unit.agent.forces.put(ForceColors.spreading,  forceSpreading)
      Gravitate.delegate(unit)
    }
  }
  
  private def getExplosions(unit: FriendlyUnitInfo): Iterable[OldExplosion] = {
    val output = new ListBuffer[OldExplosion]
    
    output ++= (
      unit.matchups.threats ++
      unit.matchups.allies.filter(a => a.is(Terran.SpiderMine) && (a.velocity.lengthSquared > 0 || a.matchups.targetsInRange.nonEmpty)))
      .flatMap(threat => explosion(unit, threat))
    
    output.filter(explosion => explosion.safetyRadius > unit.pixelDistanceFast(explosion.pixelCenter))
  }
  
  private def explosion(unit: FriendlyUnitInfo, threat: UnitInfo): Option[OldExplosion] = {
    
    if (threat.is(Terran.SpiderMine)
      && ! unit.flying
      && ( ! unit.unitClass.floats || threat.velocity.lengthSquared > 0)) {
      if (threat.burrowed) {
        if (Vector(unit, threat).exists(_.base.exists(_.owner.isUs))) {
          None
        }
        else {
          // Avoid activating the Spider Mine.
          // Spider Mines rely on normal target acquisition range math, which is 96px + attack range edge-to-edge
          // However, this is often limited by Spider Mine sight range, which is 96px exactly.
          // According to jaj22 the acqusition range is center-to-center (unlike most units, which are edge-to-edge)
          val safetyMarginPixels = 96.0
          Some(OldExplosion(
            threat.pixelCenter,
            32.0 * 3.0 + safetyMarginPixels,
            threat.damageOnNextHitAgainst(unit)
          ))
        }
      }
      else {
        if (unit.canAttack(threat) && unit.damageOnNextHitAgainst(threat) >= threat.totalHealth && unit.readyForAttackOrder && unit.framesBeforeAttacking(threat) < 24) {
          unit.agent.toAttack = Some(threat)
          Attack.consider(unit)
          None
        }
        else {
          val safetyMarginPixels = 0.0
          Some(OldExplosion(
            threat.projectFrames(4),
            32.0 * 2.0 + 10.0 + unit.unitClass.radialHypotenuse + safetyMarginPixels,
            threat.damageOnNextHitAgainst(unit) // 3 is the range; 1 is the safety margin to avoid triggering it
          ))
        }
      }
    }
    else if (threat.is(Protoss.Scarab) && ! unit.flying) {
      
      // Don't run if we're the target -- instead let other people run away from us.
      val target = threat.target.orElse(ByOption.minBy(threat.matchups.targets)(_.pixelDistanceFast(threat)))
      if ( ! target.contains(unit)) {
        val targetPixel = target.map(_.pixelCenter).getOrElse(threat.projectFrames(4))
        val safetyMarginPixels = 32.0
        Some(OldExplosion(
          targetPixel,
          60.0 * + unit.unitClass.radialHypotenuse + safetyMarginPixels,
          threat.damageOnNextHitAgainst(unit)
        ))
      }
      else {
        None
      }
    }
    else {
      None
    }
  }
}
