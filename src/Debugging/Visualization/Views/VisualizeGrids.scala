package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Grids.ArrayTypes.AbstractGridArray
import Information.Grids.AbstractGrid
import Startup.With
import Utilities.EnrichPosition._

object VisualizeGrids {
  def render() {
    renderGridArray(With.grids.dpsEnemyAirNormal,       0, 0)
    renderGridArray(With.grids.dpsEnemyAirExplosive,    1, 0)
    renderGridArray(With.grids.dpsEnemyGroundNormal,    0, 1)
    renderGridArray(With.grids.dpsEnemyGroundExplosive, 1, 1)
  }
  
  private def renderGridArray[T](map:AbstractGridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .filterNot(tilePosition => map.get(tilePosition) == map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
  
  private def renderGrid[T](map:AbstractGrid[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.get(tilePosition).toString))
  }
}
