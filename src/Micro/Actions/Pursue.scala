package Micro.Actions
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Pursue extends Action {
  
  override def perform(intent: Intention): Boolean = {
  
    if ( ! intent.unit.canAttackThisFrame) return false
    if ( ! intent.unit.canMove) return false
    
    if (intent.desireToFight >= 1.0) {
      intent.toAttack =
        intent.toAttack.orElse(EvaluateTargets.best(
          intent,
          intent.targetProfile,
          intent.targets))
    }
    
    false
  }
  
}