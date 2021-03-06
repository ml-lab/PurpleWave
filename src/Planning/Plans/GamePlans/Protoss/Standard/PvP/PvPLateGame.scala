package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.{ReactToDarkTemplarExisting, ReactToDarkTemplarPossible}
import Planning.Plans.Macro.Protoss.MeldArchons
import ProxyBwapi.Races.Protoss

class PvPLateGame extends GameplanModeTemplate {
  
  override val aggression = 0.82
  override val scoutExpansionsAt = 90
  
  override val emergencyPlans: Vector[Plan] = Vector(
    new ReactToDarkTemplarPossible,
    new ReactToDarkTemplarExisting)
  
  override val defaultAttackPlan = new Parallel(
    new Attack { attackers.get.unitMatcher.set(Protoss.DarkTemplar) },
    new PvPIdeas.AttackSafely
  )
  
  class BuildTechPartOne extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Forge)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestTech(Protoss.PsionicStorm)))
      
  class BuildTechPartTwo extends Parallel(
    new Build(
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestAtLeast(1, Protoss.RoboticsSupportBay),
      RequestUpgrade(Protoss.ShuttleSpeed)))
  
  override def defaultArchonPlan: Plan = new If(
    new TechComplete(Protoss.PsionicStorm),
    new MeldArchons(meldArchonsAt),
    new MeldArchons(120)
  )
  override val buildPlans = Vector(
    new BuildGasPumps,
    new TrainMatchingRatio(Protoss.Observer, 1, 3, Seq(MatchingRatio(Protoss.DarkTemplar, 2.0))),
    new If(new UnitsAtLeast(2,  Protoss.Dragoon),         new Build(RequestUpgrade(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1,  Protoss.HighTemplar),     new Build(RequestTech(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2,  Protoss.Reaver),          new Build(RequestUpgrade(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(3,  Protoss.Reaver),          new If(new EnemyBasesAtLeast(3), new Build(RequestUpgrade(Protoss.ShuttleSpeed)))),
    new If(new UnitsAtLeast(8,  UnitMatchWarriors),       new RequireMiningBases(2)),
    new If(new UnitsAtLeast(40, UnitMatchWarriors),       new RequireMiningBases(3)),
    new If(new UnitsAtLeast(17, UnitMatchWarriors),       new IfOnMiningBases(2, new BuildTechPartOne)),
    new If(new UnitsAtLeast(45, UnitMatchWarriors),       new IfOnMiningBases(3, new BuildTechPartTwo)),
    new If(new EnemyUnitsAtLeast(1, Protoss.DarkTemplar), new Build(RequestUpgrade(Protoss.ObserverSpeed))),
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.PhotonCannon),
        new EnemyUnitsAtMost(0, Protoss.Observer)),
      new TrainContinuously(Protoss.DarkTemplar, 3),
      new If(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new TrainContinuously(Protoss.DarkTemplar, 1))),
    new If(
      new And(
        new UnitsAtMost(0, Protoss.Shuttle),
        new UpgradeComplete(Protoss.ShuttleSpeed, 1, Protoss.Shuttle.buildFrames)),
      new Build(RequestAtLeast(1, Protoss.Shuttle)),
      new If(
        new And(
          new Not(new IfOnMiningBases(3)),
          new Not(new TechComplete(Protoss.PsionicStorm)),
          new UnitsAtMost(3, Protoss.Reaver)
        ),
        new TrainContinuously(Protoss.Reaver, 4),
        new TrainContinuously(Protoss.Observer, 3))),
    new If(
      new And(
        new UpgradeComplete(Protoss.ZealotSpeed, 1, Protoss.ZealotSpeed.upgradeFrames(1)),
        new UnitsAtMost(4, Protoss.HighTemplar),
        new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true)),
      new Build(RequestAnother(2, Protoss.HighTemplar))),
    
    new PvPIdeas.BuildDragoonsOrZealots,
    new IfOnMiningBases(1,
      new Build(
        RequestAtLeast(1, Protoss.Gateway),
        RequestAtLeast(1, Protoss.Assimilator),
        RequestAtLeast(1, Protoss.CyberneticsCore),
        RequestAtLeast(2, Protoss.Gateway),
        RequestAtLeast(1, Protoss.RoboticsFacility),
        RequestUpgrade(Protoss.DragoonRange),
        RequestAtLeast(1, Protoss.Observatory),
        RequestAtLeast(3, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new IfOnMiningBases(2,
      new If(
        new UnitsAtLeast(1, Protoss.CitadelOfAdun),
        new Build(RequestUpgrade(Protoss.ZealotSpeed)))),
    new IfOnMiningBases(2,
      new Build(
        RequestAtLeast(5, Protoss.Gateway),
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestAtLeast(1, Protoss.TemplarArchives),
        RequestAtLeast(7, Protoss.Gateway))),
    new RequireMiningBases(3),
    new IfOnMiningBases(1, new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
    new IfOnMiningBases(3, new Build(RequestAtLeast(12, Protoss.Gateway))),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.GroundDamage),
    new IfOnMiningBases(4, new Build(RequestAtLeast(15, Protoss.Gateway))),
    new UpgradeContinuously(Protoss.GroundArmor))
}
