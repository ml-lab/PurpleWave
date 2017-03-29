package Information.Battles

import Geometry.Positions
import ProxyBwapi.UnitInfo.UnitInfo

class BattleGroup(val units:Set[UnitInfo]) {
  
  var strength = 0.0
  var vanguard = Positions.middle
  var center = Positions.middle
}