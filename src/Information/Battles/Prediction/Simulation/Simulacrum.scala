package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Views.Battles.ShowBattleDetails
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Simulacrum(
  val simulation  : Simulation,
  val realUnit    : UnitInfo) {
  
  // Constant
  private val SIMULATION_STEP_FRAMES = 4
  
  // Modifiers
  val movementDelay     : Int     = if (realUnit.isEnemy || simulation.weAttack)  0   else realUnit.unitClass.framesToTurn(Math.PI) + 2 * realUnit.unitClass.accelerationFrames + With.configuration.retreatMovementDelay
  val speedMultiplier   : Double  = if (realUnit.isEnemy || realUnit.flying)      1.0 else simulation.chokeMobility(realUnit.zone)
  val bonusDistance     : Double  = if (realUnit.isOurs  || realUnit.visible)     0.0 else 32.0 * Math.min(realUnit.mobility, 3)
  val bonusRange        : Double  = if (realUnit.isOurs  || ! realUnit.unitClass.isSiegeTank || ! simulation.weAttack) 0.0 else With.configuration.bonusTankRange
  val multiplierSplash  : Double  = MicroValue.maxSplashFactor(realUnit)
  
  // Unit state
  val canMove             : Boolean                     = realUnit.canMove
  val topSpeed            : Double                      = realUnit.topSpeed * speedMultiplier
  var shieldPoints        : Int                         = realUnit.shieldPoints + realUnit.defensiveMatrixPoints
  var hitPoints           : Int                         = realUnit.hitPoints
  var cooldownShooting    : Int                         = 1 + realUnit.cooldownLeft
  var cooldownMoving      : Int                         = movementDelay
  var pixel               : Pixel                       = realUnit.pixelCenter
  var dead                : Boolean                     = false
  var target              : Option[Simulacrum]          = None
  var killed              : Int                         = 0
  var targetPathBendiness : Double                      = 1.0
  
  // Scorekeeping
  var damageDealt       : Double                      = 0.0
  var damageReceived    : Double                      = 0.0
  var valueDealt        : Double                      = 0.0
  var valueReceived     : Double                      = 0.0
  var kills             : Int                         = 0
  var valuePerDamage    : Double                      = PurpleMath.nanToZero(realUnit.subjectiveValue.toDouble / realUnit.unitClass.maxTotalHealth)
  var moves             : ArrayBuffer[(Pixel, Pixel)] = new ArrayBuffer[(Pixel, Pixel)]
  
  lazy val targetQueue: mutable.PriorityQueue[Simulacrum] = (
    new mutable.PriorityQueue[Simulacrum]()(Ordering.by(x => (x.realUnit.unitClass.helpsInCombat, - x.pixel.pixelDistanceFast(pixel))))
      ++ realUnit.matchups.targets
        .filter(target =>
          realUnit.inRangeToAttack(target)
          || simulation.weAttack
          || ! realUnit.isOurs)
        .flatMap(simulation.simulacra.get)
    )
  
  val fighting: Boolean = {
    if ( ! realUnit.canAttack) {
      false
    }
    else if ( ! realUnit.unitClass.helpsInCombat) {
      false
    }
    else if (realUnit.unitClass.isWorker) {
      realUnit.isBeingViolent || ( ! realUnit.gathering && ! realUnit.constructing)
    }
    else if (realUnit.isEnemy) {
      true
    }
    else if (simulation.weAttack) {
      realUnit.canMove || realUnit.matchups.targetsInRange.nonEmpty
    }
    else {
      ! realUnit.canMove
    }
  }
  
  def splashFactor : Double = if (multiplierSplash == 1.0) 1.0 else Math.max(1.0, Math.min(targetQueue.count( ! _.dead) / 4.0, multiplierSplash))
  
  def updateDeath() {
    dead = dead || hitPoints <= 0
  }
  
  def step() {
    if (dead) return
    cooldownMoving    -= 1
    cooldownShooting  -= 1
    tryToAcquireTarget()
    tryToChaseTarget()
    tryToStrikeTarget()
    tryToRunAway()
    if (cooldownShooting > 0)           simulation.updated = true
    if (cooldownMoving > 0 && fighting) simulation.updated = true
  }
  
  def tryToAcquireTarget() {
    if ( ! fighting)                                return
    if (cooldownMoving > 0 || cooldownShooting > 0) return
    if (validTargetExistsInRange)                   return
    if (validTargetExists) {
      //Target is out of range, but maybe we can get them later. Put them back in the queue.
      targetQueue += target.get
    }
    
    val lastTarget = target
    target = None
    while (target.isEmpty && targetQueue.nonEmpty) {
      // Clear crud from the queue
      while (targetQueue.nonEmpty && ! isValidTarget(targetQueue.head)) {
        targetQueue.dequeue()
      }
      
      if (targetQueue.nonEmpty) {
        target = Some(targetQueue.dequeue())
      }
    }
    if (target.isDefined && target != lastTarget) {
      lazy val distanceByAir    : Double = realUnit.pixelDistanceEdge(target.get.realUnit)
      lazy val distanceByGround : Double = realUnit.pixelDistanceTravelling(target.get.realUnit.pixelCenter)
      targetPathBendiness = Math.min(3.0, PurpleMath.nanToInfinity(distanceByAir / distanceByGround))
    }
  }
  
  def tryToChaseTarget() {
    lazy val victim          = target.get
    lazy val pixelsFromRange = pixelsOutOfRange(victim)
    if ( ! canMove)           return
    if ( ! fighting)          return
    if (cooldownMoving > 0)   return
    if ( ! validTargetExists) return
    if (pixelsFromRange <= 0) return
    
    val travelFrames    = Math.min(SIMULATION_STEP_FRAMES, realUnit.framesToTravelPixels(pixelsFromRange))
    val travelSpeed     = realUnit.topSpeed * speedMultiplier / targetPathBendiness
    val travelPixel     = pixel.project(victim.pixel, travelSpeed * travelFrames)
    val originalPixel   = pixel
    cooldownMoving      = travelFrames
    pixel               = travelPixel
    simulation.updated  = true
  
    if (ShowBattleDetails.inUse) {
      moves += ((originalPixel, travelPixel))
    }
  }
  
  def tryToStrikeTarget() {
    if ( ! fighting)                  return
    if (cooldownShooting > 0)         return
    if ( ! validTargetExistsInRange)  return
    val victim = target.get
    val victimWasAliveBefore = victim.hitPoints > 0
    val damageToVictim = Math.min(
      victim.hitPoints,
      realUnit.damageOnNextHitAgainst(
        victim.realUnit,
        Some(victim.shieldPoints),
        Some(pixel),
        Some(victim.pixel)))
    val damageToShields   = Math.min(victim.shieldPoints, damageToVictim)
    val damageToHitPoints = damageToVictim - damageToShields
    val valueDamage       = damageToVictim * victim.valuePerDamage * With.configuration.nonLethalDamageValue
    cooldownShooting      = Math.max(1, (realUnit.cooldownMaxAgainst(victim.realUnit) / splashFactor).toInt)
    cooldownMoving        = Math.max(cooldownMoving, cooldownMoving + realUnit.unitClass.stopFrames)
    damageDealt           += damageToVictim
    valueDealt            += valueDamage
    victim.shieldPoints   -= damageToShields
    victim.hitPoints      -= damageToHitPoints
    victim.damageReceived += damageToVictim
    victim.valueReceived  += valueDamage
    if (victim.hitPoints <= 0 && victimWasAliveBefore) {
      kills += 1
      val killValue = victim.realUnit.subjectiveValue * (1.0 - With.configuration.nonLethalDamageValue)
      valueDealt += killValue
      victim.valueReceived += killValue
    }
    dead                  = dead || realUnit.unitClass.suicides
    simulation.updated    = true
  
    if (realUnit.canStim && ! realUnit.stimmed) {
      cooldownShooting = cooldownShooting / 2
    }
  }
  
  def tryToRunAway() {
    if (fighting)           return
    if ( ! canMove)         return
    if (cooldownMoving > 0) return
    val focus           = simulation.focus
    val distanceBefore  = pixel.pixelDistanceFast(focus)
    val distanceAfter   = distanceBefore + realUnit.topSpeed * SIMULATION_STEP_FRAMES
    val fleePixel       = focus.project(pixel, distanceAfter).clamp
    if (ShowBattleDetails.inUse) {
      moves += ((pixel, fleePixel))
    }
    pixel           = fleePixel
    cooldownMoving  = SIMULATION_STEP_FRAMES
  }
  
  def isValidTarget(target: Simulacrum): Boolean = (
    ! target.dead
    && target.hitPoints > 0
    && (realUnit.pixelRangeMin <= 0 || pixel.pixelDistanceFast(target.pixel) >= realUnit.pixelRangeMin)
  )
  
  def pixelsOutOfRange(target: Simulacrum): Double = {
    val distance = PurpleMath.broodWarDistanceBox(
      realUnit.pixelStartAt(pixel),
      realUnit.pixelEndAt(pixel),
      target.realUnit.pixelStartAt(pixel),
      target.realUnit.pixelEndAt(pixel))
    distance - realUnit.pixelRangeAgainst(target.realUnit) - bonusRange + target.bonusDistance
  }
  
  def validTargetExists: Boolean = target.exists(isValidTarget)
  
  def validTargetExistsInRange: Boolean = {
    validTargetExists && pixelsOutOfRange(target.get) <= 0
  }
  
  def reportCard: ReportCard = ReportCard(
    simulacrum      = this,
    estimation      = simulation.estimation,
    valueDealt      = valueDealt,
    valueReceived   = valueReceived,
    damageDealt     = damageDealt,
    damageReceived  = damageReceived,
    dead            = dead,
    killed          = killed
  )
}
