package Debugging

import Lifecycle.With

object KeyboardCommands {
  def onSendText(text:String) {
    text match {
      case "q"    => breakpoint()
      case "c"    => With.configuration.camera      = ! With.configuration.camera
      case "v"    => With.configuration.visualize   = ! With.configuration.visualize
      case "vc"   => With.visualization.cycle       = ! With.visualization.cycle
      case "vm"   => With.visualization.map         = ! With.visualization.map
      case "vs"   => With.visualization.screen      = ! With.visualization.map
      case "vh"   => With.visualization.happy       = ! With.visualization.happy
      case "vt"   => With.visualization.textOnly    = ! With.visualization.textOnly
      case "1"    => With.game.setLocalSpeed(1000)  ; With.configuration.camera = false
      case "2"    => With.game.setLocalSpeed(60)    ; With.configuration.camera = false
      case "3"    => With.game.setLocalSpeed(30)    ; With.configuration.camera = false
      case "4"    => With.game.setLocalSpeed(0)     ; With.configuration.camera = false
      case "map"  => With.logger.debug("The current map is " + With.game.mapName + ": " + With.game.mapFileName)
    }
  }
  
  def breakpoint() {
    val setABreakpointHere = 12345
  }
}
