package Micro.Actions.Combat

import Micro.Actions.Action
import Micro.Actions.Commands.Attack
import Micro.Heuristics.Targeting.EvaluateTargets
import Micro.Intent.Intention

object Pursue extends Action {
  
  override def allowed(intent: Intention): Boolean = {
    intent.canAttack &&
    intent.canPursue &&
    intent.toAttack.isEmpty &&
    intent.unit.canMoveThisFrame &&
    intent.targets.nonEmpty
  }
  
  override def perform(intent: Intention) {
    val pursuableTargets = intent.targets.filter(_.topSpeed < intent.unit.topSpeed)
    intent.toAttack = EvaluateTargets.best(intent, pursuableTargets)
    Attack.delegate(intent)
  }
}