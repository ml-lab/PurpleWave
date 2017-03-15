package Micro.Targeting
import Startup.With
import Micro.Intentions.Intention
import BWMirrorProxy.UnitInfo.UnitInfo

class TargetingProfile(
  preferInRange     : Double = 1,
  preferValue       : Double = 1,
  preferFocus       : Double = 1,
  preferDps         : Double = 1,
  avoidHealth       : Double = 1,
  avoidDistance     : Double = 1)
    extends EvaluateTarget {
  
  override def evaluate(intent:Intention, target:UnitInfo): Double = {
    List(
      weigh( inRange  (intent, target), preferInRange),
      weigh( value    (intent, target), preferValue),
      weigh( focus    (intent, target), preferFocus),
      weigh( dps      (intent, target), preferDps),
      weigh( health   (intent, target), -avoidHealth),
      weigh( distance (intent, target), -avoidDistance)
    )
    .product
  }
  
  //TODO: Consolidate with MovementProfile
  def weigh(value:Double, weight:Double):Double = Math.pow(value, weight)
  def unboolify(value:Boolean)                  = if (value) 2 else 1
  
  def inRange(intent:Intention, target:UnitInfo):Double = {
    unboolify(intent.unit.distanceFromEdge(target) <= intent.unit.range)
  }
  
  def value(intent:Intention, target:UnitInfo):Double = {
    intent.unit.totalCost
  }
  
  def focus(intent:Intention, target:UnitInfo):Double = {
    With.grids.friendlyGroundStrength.get(target.tileCenter)
  }
  
  def dps(intent:Intention, target:UnitInfo):Double = {
    target.groundDps
  }
  
  def health(intent:Intention, target:UnitInfo):Double = {
    target.totalHealth
  }
  
  def distance(intent:Intention, target:UnitInfo):Double = {
    Math.max(0, intent.unit.distanceFromEdge(target) - intent.unit.range)
  }
}