package Micro.Behaviors
import Lifecycle.With
import Micro.Actions._
import Micro.Intent.Intention

object Behavior {
  
  def execute(intent: Intention) {
    if ( ! readyForOrders(intent)) return
    
    intent.desireToFight = With.battles.byUnit.get(intent.unit)
      .map(battle => (1.0 + battle.us.strength) / (1.0 + battle.enemy.strength))
      .getOrElse(1.0)
  
    Gather.perform(intent)  ||
    Build.perform(intent)   ||
    Perform.perform(intent) ||
    Flee.perform(intent)    ||
    Pursue.perform(intent)  ||
    Poke.perform(intent)    ||
    Attack.perform(intent)  ||
    Move.perform(intent)
  }
  
  def readyForOrders(intent:Intention):Boolean = {
    ! intent.unit.attackStarting && ! intent.unit.attackAnimationHappening
  }
}
