package Information.Battles.Types

import Information.Battles.BattleUpdater
import Information.Battles.Estimations.Estimation
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class Battle(
  val us    : Team,
  val enemy : Team) {
  
  us.battle     = this
  enemy.battle  = this
  
  //////////////
  // Features //
  //////////////
  
  def teams: Vector[Team] = Vector(us, enemy)
  def teamOf(unit: UnitInfo): Team = if (unit.isFriendly) us else enemy
  def focus: Pixel = teams.map(_.vanguard).centroid
  def happening: Boolean = teams.forall(_.units.nonEmpty) && teams.exists(_.units.exists(_.canAttack))
  
  /////////////////
  // Estimations //
  /////////////////
  
  lazy val estimationAbstract           : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = true,  weRetreat = false)
  lazy val estimationAbstractOffense    : Estimation  = BattleUpdater.estimateAvatar(this, geometric = false, weAttack = true,  enemyAttacks = false, weRetreat = false)
  lazy val estimationSimulationAttack   : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = true)
  lazy val estimationSimulationRetreat  : Estimation  = BattleUpdater.estimateSimulation(this, weAttack = false)
  
  ///////////////
  // Judgement //
  ///////////////
  
  lazy val analysis = new Analysis(this)
  lazy val desire: Double = analysis.desire
  
  lazy val globalSafeToAttack: Boolean = {
    estimationAbstractOffense.weSurvive || estimationAbstractOffense.enemyDies || estimationAbstractOffense.netValue > 0
  }
}
