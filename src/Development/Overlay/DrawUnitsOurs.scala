package Development.Overlay

import Development.Debugger
import Startup.With
import bwapi.{Color, UnitCommandType}

object DrawUnitsOurs {
  def draw() {
    With.units.ours
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
        With.game.drawCircleMap(unit.pixel, 32, Color.Orange))
    With.units.ours
      .filterNot(_.command.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => Draw.label(
        List(
          With.commander._lastIntentions.get(unit).map(intent => (intent.motivation * 100).toInt.toString).getOrElse(""),
          With.commander._lastIntentions.get(unit).map(intent => intent.plan.toString).getOrElse(""),
          With.commander._lastCommands.get(unit).getOrElse(""),
          unit.command.getUnitCommandType.toString),
        unit.pixel,
        drawBackground = false))
  }
}
