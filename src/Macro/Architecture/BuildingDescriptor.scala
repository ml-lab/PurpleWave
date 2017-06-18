package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}

class BuildingDescriptor(
  val suggestor   : Plan,
  val argBuilding : Option[UnitClass] = None,
  argWidth        : Option[Int]       = None,
  argHeight       : Option[Int]       = None,
  argPowers       : Option[Boolean]   = None,
  argPowered      : Option[Boolean]   = None,
  argTownHall     : Option[Boolean]   = None,
  argGas          : Option[Boolean]   = None,
  argMargin       : Option[Boolean]   = None) {
  
  val frameCreated: Int = With.frame
  
  val width     : Int     = argWidth     .orElse(argBuilding.map(_.tileWidth)).getOrElse(1)
  val height    : Int     = argHeight    .orElse(argBuilding.map(_.tileHeight)).getOrElse(1)
  val powers    : Boolean = argPowers    .getOrElse(argBuilding.contains(Protoss.Pylon))
  val powered   : Boolean = argPowered   .getOrElse(argBuilding.exists(_.requiresPsi))
  val townHall  : Boolean = argTownHall  .getOrElse(argBuilding.exists(_.isTownHall))
  val gas       : Boolean = argGas       .getOrElse(argBuilding.exists(_.isRefinery))
  val margin    : Boolean = argMargin    .getOrElse( ! townHall && argBuilding.exists(building => UnitClasses.all.exists(unit => ! unit.isFlyer && unit.whatBuilds._1 == building)))
  
  def fulfilledBy(suggestion: BuildingDescriptor): Boolean = {
    if (suggestion == this) return true
    width     == suggestion.width                     &&
    height    == suggestion.height                    &&
    (powers    == suggestion.powers    || ! powers)   &&
    (powered   == suggestion.powered   || ! powered)  &&
    townHall  == suggestion.townHall                  &&
    gas       == suggestion.gas                       &&
    margin    <= suggestion.margin
  }
  
  def marginTiles: Int = if(margin) 1 else 0
  
  def buildStart  : Tile = Tile(0, 0)
  def buildEnd    : Tile = Tile(width, height)
  def marginStart : Tile = buildStart.subtract(marginTiles, marginTiles)
  def marginEnd   : Tile = buildEnd.add(marginTiles, marginTiles)
  
  def accepts(tile: Tile): Boolean = {
    
    if (powered) {
      if (width == 4 && ! With.grids.psi4x3.get(tile)) {
        return false
      }
      if (width == 3 && ! With.grids.psi2x2and3x2.get(tile)) {
        return false
      }
      if (width == 2 && ! With.grids.psi2x2and3x2.get(tile)) {
        return false
      }
    }
    
    if (townHall) {
      if ( ! With.geography.bases.exists(base => base.townHallArea.startInclusive == buildStart)) {
        return false
      }
    }
    
    if (gas) {
      if( ! With.units.neutral.exists(unit => unit.unitClass.isGas && unit.tileTopLeft == tile)) {
        return false
      }
    }
    
    var x             = tile.add(marginStart).x
    var y             = tile.add(marginStart).y
    val xMax          = tile.add(marginEnd).x
    val yMax          = tile.add(marginEnd).y
    val tileBuildEnd  = tile.add(buildEnd)
    
    // TODO: Reject areas occupied by units
    
    // While loops have lower overhead than other iterative mechanisms in Scala.
    while (x < xMax) {
      while (y < yMax) {
        val nextTile = Tile(x, y)
        if ( ! nextTile.valid) {
          return false
        }
        if (
          nextTile.x < tile.x         ||
          nextTile.y < tile.y         ||
          nextTile.x > tileBuildEnd.x ||
          nextTile.y > tileBuildEnd.y) {
          if ( ! With.grids.walkable.get(nextTile)) {
            return false
          }
        }
        else if ( ! gas && ! With.grids.buildable.get(nextTile)) {
            return false
        }
        y += 1
      }
      x += 1
    }
    
    true
  }
  
  override def toString: String =
    "#" + With.prioritizer.getPriority(suggestor) + " " +
    suggestor.toString.take(5) + "... " +
    argBuilding.map(_.toString + " ").getOrElse("") +
    width + "x" + height + " " +
    (if (margin) width + 2 * marginTiles + "x" + height + 2 * marginTiles + " " else "") +
    (if (powers) "(Powers) " else "") +
    (if (powered) "(Powered) " else "") +
    (if (townHall) "(Town hall) " else "") +
    (if (gas) "(Gas) " else "")
  
}