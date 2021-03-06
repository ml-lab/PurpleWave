package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.Prediction
import Information.Battles.Types.{BattleLocal, Team}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

class Simulation(
  val battle    : BattleLocal,
  val weAttack  : Boolean) {
  
  private def buildSimulacra(team: Team) = team.units.map(new Simulacrum(this, _))
  
  val estimation            : Prediction          = new Prediction
  val focus                 : Pixel               = battle.focus
  val unitsOurs             : Vector[Simulacrum]  = buildSimulacra(battle.us)
  val unitsEnemy            : Vector[Simulacrum]  = buildSimulacra(battle.enemy)
  val everyone              : Vector[Simulacrum]  = unitsOurs ++ unitsEnemy
  var updated               : Boolean             = true
  lazy val ourWidth         : Double              = battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else unit.unitClass.dimensionMin + unit.unitClass.dimensionMax).sum
  lazy val chokeMobility    : Map[Zone, Double]   = battle.us.units.map(_.zone).distinct.map(zone => (zone, getChokeMobility(zone))).toMap
  
  val simulacra: Map[UnitInfo, Simulacrum] =
    (unitsOurs.filter(_.canMove) ++ unitsEnemy)
      .map(simulacrum => (simulacrum.realUnit, simulacrum))
      .toMap
  
  def complete: Boolean = (
    estimation.frames > With.configuration.battleEstimationFrames
    || ! updated
    || unitsOurs.forall(_.dead)
    || unitsEnemy.forall(_.dead)
    || everyone.forall(e => e.dead || ! e.fighting)
  )
  
  def run() {
    while ( ! complete) step()
    cleanup()
  }
  
  def step() {
    updated = false
    estimation.frames += 1
    everyone.foreach(_.step())
    everyone.foreach(_.updateDeath())
  }
  
  def cleanup() {
    estimation.costToUs         = unitsOurs   .map(_.valueReceived).sum
    estimation.costToEnemy      = unitsEnemy  .map(_.valueReceived).sum
    estimation.damageToUs       = unitsOurs   .map(_.damageReceived).sum
    estimation.damageToEnemy    = unitsEnemy  .map(_.damageReceived).sum
    estimation.deathsUs         = unitsOurs   .count(_.dead)
    estimation.deathsEnemy      = unitsEnemy  .count(_.dead)
    estimation.totalUnitsUs     = unitsOurs   .size
    estimation.totalUnitsEnemy  = unitsEnemy  .size
    estimation.reportCards      ++= everyone  .map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
    estimation.simulation       = Some(this)
    estimation.events           = everyone.flatMap(_.events).sortBy(_.frame)
  }
  
  private def getChokeMobility(zoneUs: Zone): Double = {
    val zoneEnemy = battle.enemy.centroid.zone
    if (zoneUs == zoneEnemy) return 1.0
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.max(32.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 10.0))
    PurpleMath.nanToOne(2.5 * edgeWidth / ourWidth)
  }
}
