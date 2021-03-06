package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.DefendEntrance
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.{IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Predicates.Reactive.EnemyBio
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.{PvT2BaseCarrier, PvT2BaseReaverCarrier}

class PvT2BaseCarriers extends GameplanModeTemplate {
  
  override val activationCriteria   = new Or(new Employing(PvT2BaseCarrier), new Employing(PvT2BaseReaverCarrier))
  override val emergencyPlans       = Vector(new PvTIdeas.Require2BaseTech, new PvTIdeas.GetObserversForCloakedWraiths)
  
  override def priorityAttackPlan = new Parallel(
    new DefendEntrance {
      defenders.get.unitMatcher.set(Protoss.Dragoon)
      defenders.get.unitCounter.set(UnitCountExactly(2))
    },
    new PvTIdeas.PriorityAttacks)
  
  override def defaultAttackPlan: Plan = new PvTIdeas.AttackRespectingMines
  
  override val buildPlans = Vector(
    new PvTIdeas.Require2BaseTech,
    new If(
      new UnitsAtLeast(2, Protoss.Carrier, complete = true),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new IfOnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(3),
    
    new FlipIf(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Observatory)),
        new If(
          new Or(
            new Employing(PvT2BaseReaverCarrier),
            new EnemyBio),
          new Build(RequestAtLeast(1, Protoss.RoboticsSupportBay))),
        new If(
          new Or(
            new Employing(PvT2BaseCarrier),
            new UnitsAtLeast(1, Protoss.Reaver),
            new UnitsAtLeast(1, Protoss.Stargate)),
          new Parallel(
            new BuildOrder(
              RequestAtLeast(1, Protoss.Stargate),
              RequestAtLeast(1, Protoss.FleetBeacon),
              RequestAtLeast(2, Protoss.Stargate),
              RequestAtLeast(2, Protoss.Carrier),
              RequestUpgrade(Protoss.AirDamage)))),
        new If(
          new And(
            new EnemyBio,
            new Employing(PvT2BaseCarrier)),
          new UpgradeContinuously(Protoss.AirArmor),
          new UpgradeContinuously(Protoss.AirDamage)))),
    
    new Build(
      RequestAtLeast(5, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(3, Protoss.Stargate),
      RequestAtLeast(1, Protoss.Forge)),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(1,   Protoss.TemplarArchives),
      RequestAtLeast(12,  Protoss.Gateway),
      RequestAtLeast(4,   Protoss.Stargate)),
    new UpgradeContinuously(Protoss.GroundArmor))
}

