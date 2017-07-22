package Performance.Tasks.Grids

import Lifecycle.With
import Performance.Tasks.AbstractTask

class TaskGridDpfEnemyAirConcussive extends AbstractTask {
  
  override protected def onRun(): Unit = With.grids.dpfEnemyAirConcussive.update()
  
}