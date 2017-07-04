package Debugging.Visualizations.Views.Performance

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPerformance extends View {
  
  override def renderScreen() {
    DrawScreen.header(5,  With.game.getLatencyFrames              + " latency frames")
    DrawScreen.header(80, With.latency.turnSize                   + " frames/turn")
    DrawScreen.header(155, With.performance.meanFrameMilliseconds + "ms avg")
    DrawScreen.header(230, With.performance.maxFrameMilliseconds  + "ms max")
    DrawScreen.header(305, With.performance.cacheLength(1)        + "-frame cache")
    With.game.drawTextScreen(5,   2 * With.visualization.lineHeightSmall, "+55ms: "     + With.performance.framesOver55     + "/200")
    With.game.drawTextScreen(80,  2 * With.visualization.lineHeightSmall, "+1000ms: "   + With.performance.framesOver1000   + "/10")
    With.game.drawTextScreen(155, 2 * With.visualization.lineHeightSmall, "+10000ms: "  + With.performance.framesOver10000  + "/2")
    if (With.performance.disqualified) {
      With.game.setTextSize(bwapi.Text.Size.Enum.Large)
      With.game.drawTextScreen(230, 4 * With.visualization.lineHeightSmall, "Disqualified!")
      With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    }
    
    renderDetails()
  }
  
  def renderDetails() {
    val title = Vector("Cutoff: ", With.configuration.initialTaskLengthMilliseconds + "ms")
    val headers = Vector("System", "Last run", "Total runs", "Total skips", "Avg ms", "Max (Recent)", "Max (All time)", "Threshold :(", "Rules :(")
    val body = With.tasks.tasks
      .sortBy(_.getClass.getSimpleName + "  ")
      .map(task => Vector(
        task.getClass.getSimpleName.replace("Task", ""),
        "X" * Math.min(10, Math.max(0, task.framesSinceRunning - 1)),
        task.totalRuns.toString,
        task.totalSkips.toString,
        task.runMillisecondsMean.toString,
        task.runMillisecondsMaxRecent.toString,
        task.runMillisecondsMaxAllTime.toString,
        task.totalViolatedThreshold.toString,
        task.totalViolatedRules.toString
      ))
    DrawScreen.table(5, With.visualization.lineHeightSmall * 8, Vector(title) ++ Vector(headers) ++ body)
  }
}