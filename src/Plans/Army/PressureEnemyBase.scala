package Plans.Army

import Plans.Allocation.LockUnitsGreedily
import Plans.Compound.AllSerial
import Plans.Information.RequireEnemyBaseLocation
import Strategies.UnitMatchers.UnitMatchWarriors

class PressureEnemyBase extends AllSerial {
  
  val meDE = this
  var _fighters = new LockUnitsGreedily {
    unitMatcher.set(new UnitMatchWarriors)
  }
  
  children.set(List(
    new AllSerial { children.set(List(
      new RequireEnemyBaseLocation {
        this.scoutPlan.set(meDE._fighters)
      },
      _fighters
    )) },
    new PressureEnemyBaseFulfiller {
      fighters.set(_fighters)
    }
  ))
}