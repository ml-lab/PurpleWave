package Planning.Plans.GamePlans.Terran.Standard.TvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors, UnitMatchWorkers}
import Planning.Plan
import Planning.Plans.Army._
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.MineralsAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{IfOnMiningBases, OnGasPumps, UnitsAtLeast}
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvT.TvTStandard

class TerranVsTerranOld extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(TvTStandard)
  
  
  
  override def defaultPlacementPlan: Plan = new BunkersAtNatural(1)
  
  override def defaultAttackPlan: Plan = new Trigger(
    new UnitsAtLeast(1, Terran.Wraith, complete = true),
    new Parallel(
      new ConsiderAttacking,
      new ConsiderAttacking {
        attack.attackers.get.unitMatcher.set(UnitMatchWorkers)
        attack.attackers.get.unitCounter.set(UnitCountOne)
      }))
  
  override val buildOrder = Vector(
    RequestAtLeast(1,   Terran.CommandCenter),
    RequestAtLeast(9,   Terran.SCV),
    RequestAtLeast(1,   Terran.SupplyDepot),
    RequestAtLeast(11,  Terran.SCV),
    RequestAtLeast(1,   Terran.Barracks),
    RequestAtLeast(12,  Terran.SCV),
    RequestAtLeast(1,   Terran.Refinery),
    RequestAtLeast(15,  Terran.SCV),
    RequestAtLeast(2,   Terran.SupplyDepot),
    RequestAtLeast(16,  Terran.SCV),
    RequestAtLeast(1,   Terran.Factory),
    RequestAtLeast(20,  Terran.SCV))
  
  override def buildPlans: Seq[Plan] = Vector(
    new Build(
      RequestAtLeast(1, Terran.Bunker),
      RequestAtLeast(1, Terran.MachineShop),
      RequestAtLeast(1, Terran.Starport)),
    
    new If(
      new MineralsAtLeast(800),
      new Parallel(
        new IfOnMiningBases(2, new Build(RequestAtLeast(5, Terran.Factory)), new Build(RequestAtLeast(2, Terran.Barracks))),
        new IfOnMiningBases(3, new Build(RequestAtLeast(8, Terran.Factory)), new Build(RequestAtLeast(3, Terran.Barracks)))
      )),
    
    new If(new UnitsAtLeast(1, UnitMatchSiegeTank, complete = true), new RequireMiningBases(2)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors), new RequireMiningBases(3)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors), new RequireMiningBases(4)),
    new BuildGasPumps,
    new If(new UnitsAtLeast(2,  Terran.SiegeTankUnsieged),  new Build(RequestTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(3,  Terran.Wraith),             new Build(RequestAtLeast(1, Terran.ControlTower), RequestTech(Terran.WraithCloak))),
    new If(new UnitsAtLeast(2,  Terran.Goliath),            new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new Build(RequestTech(Terran.Yamato))),
    new If(new UnitsAtLeast(3,  Terran.Vulture),            new Build(RequestTech(Terran.SpiderMinePlant))),
    new If(new UnitsAtLeast(5,  Terran.Vulture),            new Build(RequestUpgrade(Terran.VultureSpeed))),
    new If(new UnitsAtLeast(10, Terran.Marine),             new Build(RequestUpgrade(Terran.MarineRange))),
    new If(new UnitsAtLeast(4,  Terran.Goliath),            new Build(RequestUpgrade(Terran.GoliathAirRange))),
    new If(new UnitsAtLeast(1,  Terran.PhysicsLab),         new TrainContinuously(Terran.ControlTower)),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new UpgradeContinuously(Terran.AirDamage)),
    new If(new UnitsAtLeast(3,  Terran.Battlecruiser),      new UpgradeContinuously(Terran.AirArmor)),
    new If(new UnitsAtLeast(20, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechDamage)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new Build(RequestAtLeast(1, Terran.ScienceFacility), RequestAtLeast(2, Terran.Armory))),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),         new UpgradeContinuously(Terran.MechArmor)),
  
    new TrainContinuously(Terran.Comsat),
    new TrainMatchingRatio(Terran.Goliath, 1, Int.MaxValue, Seq(
        MatchingRatio(Terran.Battlecruiser, 3.0),
        MatchingRatio(Terran.Wraith,        0.75),
        MatchingRatio(Terran.Vulture,       0.5))),
    
    new TrainContinuously(Terran.Battlecruiser),
    new TrainMatchingRatio(Terran.SiegeTankUnsieged, 3, Int.MaxValue, Seq(
      MatchingRatio(UnitMatchSiegeTank, 1.25),
      MatchingRatio(Terran.Goliath,     0.75),
      MatchingRatio(Terran.Wraith,      0.75),
      MatchingRatio(Terran.Vulture,     0.5))),
  
    new TrainMatchingRatio(Terran.Wraith, 3, Int.MaxValue, Seq(
      MatchingRatio(Terran.Wraith,      1.5),
      MatchingRatio(Terran.Vulture,     0.25))),
    
    new TrainContinuously(Terran.Marine),
    new TrainContinuously(Terran.Vulture),
  
    new IfOnMiningBases(2, new Build(RequestAtLeast(1, Terran.Starport),  RequestAtLeast(3, Terran.Factory), RequestAtLeast(2, Terran.Starport), RequestAtLeast(1, Terran.Armory), RequestAtLeast(1, Terran.Academy), RequestAtLeast(5, Terran.Factory))),
    new IfOnMiningBases(3, new Build(RequestAtLeast(5, Terran.Factory),   RequestAtLeast(1, Terran.Academy), RequestAtLeast(8, Terran.Factory))),
    new OnGasPumps(2, new Build(RequestAtLeast(3, Terran.MachineShop))),
    new OnGasPumps(3, new Build(
      RequestAtLeast(5, Terran.MachineShop),
      RequestAtLeast(1, Terran.ScienceFacility),
      RequestAtLeast(2, Terran.Starport),
      RequestAtLeast(1, Terran.PhysicsLab),
      RequestUpgrade(Terran.BattlecruiserEnergy))),
    new RequireMiningBases(2),
    new Build(
      RequestAtLeast(3, Terran.Factory),
      RequestAtLeast(2, Terran.Starport),
      RequestAtLeast(1, Terran.Armory),
      RequestAtLeast(5, Terran.Factory),
      RequestAtLeast(1, Terran.Academy),
      RequestAtLeast(8, Terran.Factory)),
    new RequireMiningBases(3),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(12, Terran.Factory)),
    new RequireMiningBases(5),
    new Build(RequestAtLeast(16, Terran.Factory))
  )
}