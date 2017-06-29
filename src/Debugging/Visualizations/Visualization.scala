package Debugging.Visualizations

import Debugging.Visualizations.Views.Battles.VisualizeBattles
import Debugging.Visualizations.Views.Economy.ViewEconomy
import Debugging.Visualizations.Views.Fun.{ViewHappy, ViewTextOnly}
import Debugging.Visualizations.Views.Geography._
import Debugging.Visualizations.Views.Micro._
import Debugging.Visualizations.Views.Performance.ViewPerformance
import Debugging.Visualizations.Views.Planning.{MapPlans, ViewPlanning}
import Debugging.Visualizations.Views.{ScreenClock, View}
import Lifecycle.With

import scala.util.Random

class Visualization {
  
  val lineHeightSmall = 9
  
  private val viewCycle = Vector(
    ViewGeography,
    ViewEconomy,
    ViewPerformance,
    ViewPlanning
  )
  
  private var view: View = ViewGeography
  private var lastCycle = 0
  
  var enabled   = false
  var cycle     = false
  var screen    = true
  var grids     = false
  var map       = true
  var happy     = false
  var textOnly  = false
  
  def setView(newView: View) {
    view = newView
  }
  
  def forceCycle() {
    cycle = false
    pickNextView()
  }
  
  def render() {
    requireInitialization()
    if ( ! enabled) return
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    
    if (happy) {
      ViewHappy.render()
    }
    else if (textOnly) {
      ViewTextOnly.render()
    }
    else {
      ScreenClock.render()
      if (map) {
        MapChokepoints.render()
        MapBases.render()
        if (grids) {
          MapGrids.render()
        }
        MapPlans.render()
        ViewMicro.render()
      }
      if (screen) {
        view.render()
        if (cycle && (With.frame - lastCycle) > 24 * 8) {
          pickNextView()
        }
      }
  
      VisualizeBattles.render()
    }
  }
  
  private var initialized = false
  private def requireInitialization() {
    if (initialized) return
    initialized = true
    enabled = With.configuration.visualize
    var random = Random.nextDouble()
    random -= With.configuration.visualizationProbabilityHappyVision
    if (random < 0) {
      happy = true
    }
    else {
      random -= With.configuration.visualizationProbabilityTextOnly
      if (random < 0) {
        textOnly = true
      }
    }
  }
  
  private def pickNextView() {
    var i = -1
    if (viewCycle.contains(view)) {
      i = (viewCycle.indexOf(view) + 1) % viewCycle.length
    }
    if (i >= 0) {
      view = viewCycle(i)
      lastCycle = With.frame
    }
  }
}
