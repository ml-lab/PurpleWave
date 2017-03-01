package Global.Combat

import Global.Combat.Commands.Command
import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Position

import scala.collection.mutable

class Commander {
  
  val _intentions = new mutable.HashSet[Intention]
  val _nextOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] { override def default(key: FriendlyUnitInfo): Int = 0 }
  
  def intend(intention:Intention) { _intentions.add(intention) }
  
  def onFrame() {
    _intentions.filter(_isAwake).foreach(intent => intent.command.execute(intent))
    _intentions.clear()
    _nextOrderFrame.keySet.filter(_.alive).foreach(_nextOrderFrame.remove)
  }
  
  def attack(command:Command, unit:FriendlyUnitInfo, target:UnitInfo) {
    if (target.visible) {
      unit.baseUnit.attack(target.baseUnit)
      _sleep(unit, true)
    } else {
      attack(command, unit, target.position)
    }
  }
  
  def attack(command:Command, unit:FriendlyUnitInfo, position:Position) {
    if (With.game.isVisible(position.toTilePosition)) {
      unit.baseUnit.patrol(position)
    }
    else {
      unit.baseUnit.attack(position)
    }
    _sleep(unit, true)
  }
  
  def move(command:Command, unit:FriendlyUnitInfo, position:Position) {
    unit.baseUnit.move(position)
    _sleep(unit, false)
  }
  
  def _sleep(unit:FriendlyUnitInfo, startedAttacking:Boolean = false) {
    val baseDelay = With.game.getRemainingLatencyFrames
    val attackDelay = if (startedAttacking) unit.attackFrames + 8 else 0
    _nextOrderFrame.put(unit, baseDelay + attackDelay + With.game.getFrameCount)
  }
  
  def _isAwake(intent:Intention):Boolean = {
    return _nextOrderFrame(intent.unit) < With.game.getFrameCount
  }
}