package Development.Overlay

import Startup.With

object DrawScheduler {
  
  def draw() {
    With.game.drawTextScreen(0, 85, "Build queue")
    Draw.table(0, 100, With.scheduler.queue.take(20).map(buildable => List(buildable.toString)))
    With.game.drawTextScreen(150, 85, "Next to build")
    Draw.table(150, 100, With.scheduler.simulationResults.suggestedEvents
      .toList
      .sortBy(_.buildable.toString)
      .sortBy(_.frameEnd)
      .sortBy(_.frameStart)
      .take(20)
      .map(event => List(event.toString.split("\\s+")(0), _reframe(event.frameStart), _reframe(event.frameEnd))))
    With.game.drawTextScreen(300, 85, "Started")
    Draw.table(300, 100, With.scheduler.simulationResults.simulatedEvents
      .filter(e => e.frameStart < With.frame)
      .toList
      .sortBy(_.buildable.toString)
      .sortBy(_.frameStart)
      .sortBy(_.frameEnd)
      .take(20)
      .map(event => List(event.toString.split("\\s+")(0), _reframe(event.frameStart), _reframe(event.frameEnd))))
    With.game.drawTextScreen(500, 85, "Impossible")
    Draw.table(500, 100, With.scheduler.simulationResults.unbuildable
      .toList
      .take(20)
      .map(buildable => List(buildable.toString.split("\\s+")(0))))
  }
  
  def _reframe(frameAbsolute:Int):String = {
    val reframed = (frameAbsolute - With.frame)/24
    if (reframed <= 0) "Started" else reframed.toString
  }
}
