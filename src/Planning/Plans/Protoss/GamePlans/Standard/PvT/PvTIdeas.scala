package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchCustom, UnitMatchWarriors}
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, _}
import ProxyBwapi.Races.{Protoss, Terran}

object PvTIdeas {
  
  class Require2BaseTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestUpgrade(Protoss.DragoonRange)),
    new RequireMiningBases(2),
    new BuildGasPumps)
  
  class Require3BaseTech extends Parallel(
    new Require2BaseTech,
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(4, Protoss.Gateway),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(5, Protoss.Gateway),
      RequestTech(Protoss.PsionicStorm),
      RequestAtLeast(1, Protoss.Forge)))
  
  class AttackWithDarkTemplar extends Attack {
    attackers.get.unitMatcher.set(Protoss.DarkTemplar)
  }
  
  class ContainSafely extends If(
    new And(
      new UnitsAtLeast(10, UnitMatchWarriors, complete = true),
      new Or(
        new UnitsAtLeast(1, Protoss.Observer, complete = true),
        new Not(new EnemyHasShown(Terran.SpiderMine)))),
    new Attack,
    new ConsiderAttacking)
  
  private class TrainDarkTemplar extends If(
    new And(
      new UnitsAtMost(0, Protoss.Arbiter),
      new EnemyUnitsNone(Terran.ScienceVessel),
      new EnemyUnitsNone(UnitMatchCustom((unit) => unit.is(Terran.MissileTurret) && unit.zone.owner.isNeutral))),
    new TrainContinuously(Protoss.DarkTemplar, 3),
    new TrainContinuously(Protoss.DarkTemplar, 1))
  
  private class IfCloakedThreats_Observers extends If(
    new Or(
      new EnemyHasShown(Terran.Vulture),
      new EnemyHasShown(Terran.SpiderMine),
      new EnemyHasShownWraithCloak),
    new Build(
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory)))
  
  class TrainZealotsOrDragoons extends If(
    new And(
      new UpgradeComplete(Protoss.ZealotSpeed, withinFrames = Protoss.Zealot.buildFrames),
      new Or(
        new And(
          new UnitsAtLeast(18, Protoss.Dragoon),
          new Check(() => With.units.ours.count(_.is(Protoss.Dragoon)) >= With.units.enemy.count(_.is(Terran.Vulture)))),
        new Check(() => With.self.minerals > 800 && With.self.gas < 500))),
    new TrainContinuously(Protoss.Zealot),
    new TrainContinuously(Protoss.Dragoon))
  
  class TrainArbiters extends If(
    new UnitsAtLeast(40, UnitMatchWarriors),
    new TrainContinuously(Protoss.Arbiter, 3),
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.Arbiter, 2),
      new TrainContinuously(Protoss.Arbiter, 10)))
  
  class TrainObservers extends If(
    new EnemyHasShownWraithCloak,
    new TrainContinuously(Protoss.Observer, 3),
    new If(
      new EnemyHasShown(Terran.SpiderMine),
      new TrainContinuously(Protoss.Observer, 2),
      new TrainContinuously(Protoss.Observer, 1)))
  
  class TrainHighTemplar extends OnGasBases(3,
    new If(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new TrainContinuously(Protoss.HighTemplar, 6, 2),
      new TrainContinuously(Protoss.HighTemplar, 6, 1)))
    
  class TrainArmy extends Parallel(
    new TrainArbiters,
    new TrainContinuously(Protoss.Carrier),
    new TrainDarkTemplar,
    new TrainHighTemplar,
    new TrainObservers,
    new TrainZealotsOrDragoons)
  
  class GetObserversForCloakedWraiths extends If(
    new EnemyHasShownWraithCloak,
    new Parallel(
      new Build(
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestAtLeast(1, Protoss.Observatory)),
      new PvTIdeas.TrainObservers))
}

