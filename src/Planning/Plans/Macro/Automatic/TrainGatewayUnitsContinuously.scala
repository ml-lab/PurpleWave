package Planning.Plans.Macro.Automatic

import Startup.With
import bwapi.{UnitType, UpgradeType}

class TrainGatewayUnitsContinuously extends AbstractBuildContinuously {
  
  override protected def totalRequiredRecalculate:Int =
    With.units.ours.filter(_.complete)
      .count(unit => List(UnitType.Protoss_Gateway, UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon, UnitType.Protoss_Dark_Templar).contains(unit.utype))
  
  override protected def unitType:UnitType = nextUnitType
  
  private def nextUnitType:UnitType = {
    val haveLegSpeed          = With.self.getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0
    val numberOfZealots       = With.units.ours.filter(_.utype == UnitType.Protoss_Zealot).size
    val numberOfDragoons      = With.units.ours.filter(_.utype == UnitType.Protoss_Dragoon).size
    val numberOfDarkTemplar   = With.units.ours.filter(_.utype == UnitType.Protoss_Dark_Templar).size
    val canBuildDragoon       = With.units.ours.exists(_.utype == UnitType.Protoss_Cybernetics_Core) && With.self.gas >= UnitType.Protoss_Dragoon.gasPrice
    val canBuildDarkTemplar   = With.units.ours.exists(_.utype == UnitType.Protoss_Templar_Archives) && With.self.gas >= UnitType.Protoss_Dark_Templar.gasPrice
    
    if(canBuildDarkTemplar && numberOfDarkTemplar < 2) {
      UnitType.Protoss_Dark_Templar
    }
    else if (canBuildDragoon && (numberOfDragoons * 2 < numberOfZealots || ! haveLegSpeed )) {
      UnitType.Protoss_Dragoon
    }
    else {
      UnitType.Protoss_Zealot
    }
  }
}
