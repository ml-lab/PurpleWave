package Micro.Agency

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade

class Intention {
  var toReturn    : Option[Pixel]     = None
  var toTravel    : Option[Pixel]     = None
  var toAttack    : Option[UnitInfo]  = None
  var toScan      : Option[Pixel]     = None
  var toGather    : Option[UnitInfo]  = None
  var toAddon     : Option[UnitClass] = None
  var toBuild     : Option[UnitClass] = None
  var toBuildTile : Option[Tile]      = None
  var toTrain     : Option[UnitClass] = None
  var toTech      : Option[Tech]      = None
  var toFinish    : Option[UnitInfo]  = None
  var toForm      : Option[Pixel]     = None
  var toNuke      : Option[Pixel]     = None
  var toUpgrade   : Option[Upgrade]   = None
  var canAttack   : Boolean           = true
  var canFlee     : Boolean           = true
  var canCower    : Boolean           = false
  var canMeld     : Boolean           = false
  var canTickle   : Boolean           = false
  var canScout    : Boolean           = false
  var canPillage  : Boolean           = false
  var canBerzerk  : Boolean           = false
  var canLiftoff  : Boolean           = false
}
