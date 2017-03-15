package Planning.Plans.GamePlans

import Planning.Plans.Army.{Attack, DefendChoke}
import Planning.Plans.Compound.{IfThenElse, Parallel}
import Planning.Plans.Information.ScoutAt
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.Build.ScheduleBuildOrder
import Planning.Plans.Macro.UnitCount.UnitCountAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Macro.BuildRequests.{BuildRequest, RequestUnitAnotherOne, RequestUpgrade}
import bwapi.{UnitType, UpgradeType}

class ProtossVsProtoss extends Parallel {
  
  description.set("Protoss vs Protoss")
  
  //http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
  val _fourGateGoons = List[BuildRequest] (
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //8
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //10
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //12
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //13
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Assimilator), //16
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Cybernetics_Core), //17
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Zealot), //18
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //22
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //23
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUpgrade(UpgradeType.Singularity_Charge), //26
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //27
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Probe),
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //31
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //31
    new RequestUnitAnotherOne(UnitType.Protoss_Gateway), //31
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //31
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //33
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //33
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Pylon), //33
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon), //33
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Dragoon),
    new RequestUnitAnotherOne(UnitType.Protoss_Nexus)
  )
  
  children.set(List(
    new ScheduleBuildOrder { buildables.set(_fourGateGoons) },
    new BuildPylonsContinuously,
    new BuildWorkersContinuously,
    new TrainContinuously(UnitType.Protoss_Scout),
    new TrainContinuously(UnitType.Protoss_Zealot),
    new ScheduleBuildOrder { buildables.set(MassScoutLateGame.build) },
    new ScoutAt(20),
    new IfThenElse {
      predicate.set(new UnitCountAtLeast { quantity.set(6); unitMatcher.set(UnitMatchWarriors) })
      whenFalse.set(new DefendChoke)
      whenTrue.set(new Attack)
    }
  ))
}