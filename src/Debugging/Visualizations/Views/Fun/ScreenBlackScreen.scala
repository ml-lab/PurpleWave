package Debugging.Visualizations.Views.Fun

import Lifecycle.With
import bwapi.Color

object ScreenBlackScreen {
  
  def render() {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
    //With.grids.friendlyVision.tiles
  }
}