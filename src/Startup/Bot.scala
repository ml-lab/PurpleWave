package Startup

import Development.{AutoCamera, Configuration, Logger, Overlay}
import Global.Allocation._
import Global.Information.Combat.BattleSimulator
import Global.Information.UnitAbstraction.Units
import Global.Information._
import Plans.GamePlans.WinTheGame
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    try {
      With.configuration = new Configuration
      With.logger = new Logger
      With.logger.debug("Loading BWTA.")
      BWTA.readMap()
      BWTA.analyze()
      With.logger.debug("BWTA analysis complete.")
      
      With.architect = new Architect
      With.bank = new Banker
      With.camera = new AutoCamera
      With.simulator = new BattleSimulator
      With.commander = new Commander
      With.economy = new Economy
      With.geography = new Geography
      With.gameplan = new WinTheGame
      With.history = new History
      With.intelligence = new Intelligence
      With.prioritizer = new Prioritizer
      With.recruiter = new Recruiter
      With.scheduler = new Scheduler
      With.units = new Units
      
      With.game.enableFlag(1)
      With.game.setLocalSpeed(With.configuration.gameSpeed)
    }
    catch { case exception:Exception =>
      var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onFrame() {
    try {
      With.units.onFrame()
      With.simulator.onFrame()
      With.economy.onFrame()
      With.bank.onFrame()
      With.recruiter.onFrame()
      With.prioritizer.onFrame()
      With.gameplan.onFrame() //This needs to be last!
      With.scheduler.onFrame()
      With.commander.onFrame()
      With.camera.onFrame()
      Overlay.onFrame()
      _considerSurrender
    }
    catch {
      case exception:Exception =>
        var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
        With.logger.onException(exception)
    }
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    try {
      With.units.onUnitDestroy(unit)
      With.history.onUnitDestroy(unit)
    }
    catch { case exception:Exception =>
        var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
        With.logger.onException(exception)}
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  override def onEnd(isWinner: Boolean) {
    try {
      With.logger.debug(if (isWinner) "Looks like we won. Good game!" else "Good game! Let's pretend this never happened.")
      With.logger.onEnd
      BWTA.cleanMemory()
    }
    catch { case exception:Exception =>
      var dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  def _considerSurrender() = {
    if (With.game.self.supplyUsed == 0
      && With.game.self.minerals < 50
      && With.units.enemy.exists(_.utype.isWorker)
      && With.units.enemy.exists(_.utype.isResourceDepot)) {
      With.game.sendText("Good game! Let's pretend this never happened.")
      With.game.leaveGame()
    }
  }
}
