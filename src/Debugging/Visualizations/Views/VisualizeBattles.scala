package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup}
import Information.Battles.BattleTypes.Battle
import Information.Battles.TacticsTypes.{Tactics, TacticsOptions}
import Lifecycle.With
import Mathematics.Pixels.Pixel
import Planning.Yolo
import Utilities.EnrichPixel._
import bwapi.Color

object VisualizeBattles {
  def render() = {
    With.game.drawTextScreen(438, 18, "Total strength:")
    With.game.drawTextScreen(521, 18, formatStrength(With.battles.global.us.strength))
    With.game.drawTextScreen(589, 18, formatStrength(With.battles.global.enemy.strength))
    With.battles.local.foreach(drawBattle)
    val battlesWithSimulations = With.battles.local.filter(_.simulations.nonEmpty)
    if (battlesWithSimulations.nonEmpty) {
      
      val battle = battlesWithSimulations.minBy(battle => battle.focus.pixelDistanceSquared(With.viewport.center))
      drawBattleReport(battle.simulations.minBy(_.us.tactics == battle.consensusTactics))
    }
    if (Yolo.enabled && With.frame / 24 % 2 == 0) {
      With.game.drawTextScreen(5, 5, "YOLO")
    }
  }
  
  def formatStrength(strength:Double):String = (strength/1000).toInt.toString
  
  private def drawBattle(battle:Battle) {
    val ourColor      = With.self.colorDark
    val enemyColor    = With.enemies.head.colorDark
    val neutralColor  = Color.Black
    DrawMap.circle(battle.focus,          8, neutralColor)
    DrawMap.circle(battle.us.vanguard,    8, ourColor)
    DrawMap.circle(battle.enemy.vanguard, 8, enemyColor)
    DrawMap.line(battle.focus, battle.us.vanguard,    ourColor)
    DrawMap.line(battle.focus, battle.enemy.vanguard, enemyColor)
    val topLeft     = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).minBound.subtract(16, 16)
    val bottomRight = (battle.us.units ++ battle.enemy.units).map(_.pixelCenter).maxBound.add(16, 16)
    val winnerStrengthColor = if (battle.us.strength >= battle.enemy.strength) ourColor else enemyColor
    DrawMap.box(
      topLeft,
      bottomRight,
      neutralColor)
    DrawMap.labelBox(
      Vector(formatStrength(battle.us.strength), formatStrength(battle.enemy.strength)),
      battle.focus.add(24, 0),
      drawBackground = true,
      backgroundColor = winnerStrengthColor)
  }
  
  private def drawBattleReport(battle:BattleSimulation) {
    
    val winner = if (battle.us.lostValue <= battle.enemy.lostValue) With.self else With.enemies.head
    With.game.drawTextScreen(new Pixel(5, 31).bwapi, "Advantage: " + winner.name)
    drawPlayerReport(battle.us,     With.self.name,         new Pixel(5, 50))
    drawPlayerReport(battle.enemy,  With.enemies.head.name, new Pixel(130, 50))
  }
  
  private def drawPlayerReport(group: BattleSimulationGroup, name:String, origin:Pixel) {
    DrawScreen.table(
      origin.x,
      origin.y,
      Vector(
        Vector(name),
        Vector("Losses:",   group.lostValue.toString),
        Vector("Move:",     getMove(group.tactics)),
        Vector("Focus:",    getFocus(group.tactics)),
        Vector("Workers:",  getWorkers(group.tactics)),
        Vector("Wounded:",  getWounded(group.tactics)),
        Vector(),
        Vector("Losses:")
      )
      ++ group.lostUnits
        .groupBy(_.unit.unitClass)
        .toVector
        .sortBy(_._1.toString)
        .map(u => Vector(u._1.toString, u._2.size.toString))
      ++ Vector(Vector.empty)
      ++ Vector(Vector("Survivors:"))
      ++ group.units
        .groupBy(_.unit.unitClass)
        .toVector
        .sortBy(_._1.toString)
        .map(u => Vector(u._2.size.toString, u._1.toString)))
  }
  
  private def getMove(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Movement.Charge))  return "Charge"
    if (tactics.has(Tactics.Movement.Kite))    return "Kite"
    if (tactics.has(Tactics.Movement.Flee))    return "Flee"
    return "-"
  }
  
  private def getFocus(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Focus.Air))    return "Air"
    if (tactics.has(Tactics.Focus.Ground)) return "Ground"
    return "-"
  }
  
  private def getWounded(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Wounded.Flee))  return "Flee"
    return "-"
  }
  
  private def getWorkers(tactics:TacticsOptions):String = {
    if (tactics.has(Tactics.Workers.FightAll))   return "Fight (All)"
    if (tactics.has(Tactics.Workers.FightHalf))  return "Fight (Half)"
    if (tactics.has(Tactics.Workers.Flee))       return "Flee"
    return "-"
  }
}
