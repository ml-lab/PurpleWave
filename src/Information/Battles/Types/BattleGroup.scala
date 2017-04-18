package Information.Battles.Types

import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units:Vector[UnitInfo]) {
  
  var vanguard:Pixel = Points.middle
  var center:Pixel = Points.middle
  
  var strength      : Double = 0.0
}