package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

object BustBunker extends Action {
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    With.enemies.exists(_.race == Race.Terran)                    &&
    unit.action.canFight                                          &&
    unit.canMoveThisFrame                                         &&
    unit.is(Protoss.Dragoon)                                      &&
    With.self.hasUpgrade(Protoss.DragoonRange)                    &&
    unit.matchups.threats.forall( ! _.is(Terran.SiegeTankSieged)) &&
    unit.matchups.targets.exists(target =>
      target.aliveAndComplete   &&
      target.is(Terran.Bunker)  &&
      ! target.player.hasUpgrade(Terran.MarineRange))
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    // Goal: Take down the bunker. Don't take any damage from it.
  
    // If we're getting shot at by the bunker, back off.
    if (
      unit.damageInLastSecond > 0 &&
      unit.matchups.threats.exists(threat =>
        threat.is(Terran.Bunker) &&
        threat.pixelDistanceSlow(unit) < With.configuration.bunkerSafetyMargin)
      ) {
      Retreat.delegate(unit)
    }
  }
}