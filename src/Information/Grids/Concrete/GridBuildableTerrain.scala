package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Startup.With

class GridBuildableTerrain extends GridBoolean {
  override def onInitialization() {
    tiles.foreach(tile => set(tile, With.game.isBuildable(tile)))
  }
}
