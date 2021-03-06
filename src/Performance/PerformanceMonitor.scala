package Performance

import Lifecycle.With

class PerformanceMonitor {
  
  private val framesToTrack = 24 * 3
  private val frameTimes = Array.fill(framesToTrack)(1l)
  
  private var millisecondsBefore = 0l
  private var lastFrameDelayUpdate = 0
  
  var framesOver85    = 0
  var framesOver1000  = 0
  var framesOver10000 = 0
  
  var enablePerformanceStops = true // For disabling performance stops while debugging
  
  def startFrame() {
    millisecondsBefore = System.currentTimeMillis()
  }
  
  def endFrame() {
    val millisecondDifference = millisecondsSpentThisFrame
    frameTimes(With.frame % framesToTrack) = millisecondDifference
    if (millisecondDifference >= 85)    framesOver85    += 1
    if (millisecondDifference >= 1000)  framesOver1000  += 1
    if (millisecondDifference >= 10000) framesOver10000 += 1
  }
  
  def millisecondsLeftThisFrame: Long = {
    Math.max(0, With.configuration.initialTaskLengthMilliseconds - millisecondsSpentThisFrame)
  }
  
  def millisecondsSpentThisFrame: Long = {
    Math.max(0, System.currentTimeMillis - millisecondsBefore)
  }
  
  def continueRunning: Boolean = {
    millisecondsLeftThisFrame > 1 || ! enablePerformanceStops
  }
  
  def violatedThreshold: Boolean = {
    millisecondsLeftThisFrame <= 0
  }
  
  def violatedRules: Boolean = {
    millisecondsSpentThisFrame >= 85
  }
  
  def danger: Boolean = {
    framesOver85    > 160 ||
    framesOver1000  > 5   ||
    framesOver10000 > 1
  }
  
  def maxFrameMilliseconds  : Long = frameTimes.max
  def meanFrameMilliseconds : Long = frameTimes.sum / framesToTrack
  
  def disqualified: Boolean =
    framesOver85    > 320 ||
    framesOver1000  > 1
}
