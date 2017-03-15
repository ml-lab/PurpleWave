package Debugging.Visualization

import Debugging.TypeDescriber
import Startup.With
import BWMirrorProxy.UnitInfo.ForeignUnitInfo

object VisualizeUnitsForeign {
  
  def render() {
    With.units.enemy.foreach(drawTrackedUnit)
    With.units.neutral.foreach(drawTrackedUnit)
  }
  
  private def drawTrackedUnit(trackedUnit:ForeignUnitInfo) {
    if (trackedUnit._possiblyStillThere && ! trackedUnit.visible) {
      DrawMap.circle(
        trackedUnit.pixel,
        trackedUnit.utype.width / 2,
        DrawMap.playerColor(trackedUnit.player))
      DrawMap.label(
        TypeDescriber.unit(trackedUnit.utype),
        trackedUnit.pixel,
        drawBackground = true,
        DrawMap.playerColor(trackedUnit.player))
    }
  }
}
