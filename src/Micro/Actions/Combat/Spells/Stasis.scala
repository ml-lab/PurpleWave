package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

object Stasis extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.Arbiter
  override protected def tech           : Tech      = Protoss.Stasis
  override protected def aoe            : Boolean   = true
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = casterClass.subjectiveValue / 2.0
  
  override protected def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding) return 0.0
    if (target.underStorm) return 0.0
    if (target.invincible) return 0.0
  
    target.subjectiveValue *
    Math.min(1.0, target.matchups.targets.size          / 3.0)  *
    Math.min(1.0, target.matchups.framesToLiveDiffused  / 72.0) *
    (
      if(target.isFriendly)
        -2.0
      else if (target.isEnemy)
        1.0
      else
        0.0
    )
  }
}
