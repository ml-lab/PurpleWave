package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{Attack, ConsiderAttacking}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Predicates.Milestones._
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen3GateSpeedlots

class PvPOpen3GateSpeedlots extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen3GateSpeedlots)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def defaultWorkerPlan  : Plan      = NoPlan()
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  override val aggression         : Double    = 1.2
  override val scoutAt            : Int       = 14
  override def defaultAttackPlan  : Plan =
    new If(
      new EnemyUnitsAtMost(0, Protoss.Dragoon),
      new ConsiderAttacking,
      new Trigger(
        new And(
          new UpgradeComplete(Protoss.ZealotSpeed),
          new UnitsAtLeast(14, Protoss.Zealot)),
        initialAfter = new Attack))
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/3_Gate_Speedzeal_(vs._Protoss)
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),       // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),     // 10
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),     // 12
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),      // 13
    RequestAtLeast(2,   Protoss.Pylon),       // 15
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Zealot),      // 17
    RequestAtLeast(3,   Protoss.Pylon),       // 21
    RequestAtLeast(17,  Protoss.Probe),
    RequestAtLeast(5,   Protoss.Zealot),      // 23
    RequestAtLeast(1,   Protoss.Assimilator)  // 27
  )
  
  override val buildPlans = Vector(
    new FlipIf(
      new UnitsAtLeast(6, Protoss.Zealot),
      new TrainContinuously(Protoss.Zealot),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(3, Protoss.Gateway)),
        new TrainWorkersContinuously)),
    new BuildCannonsAtNatural(2),
    new RequireMiningBases(2))
}
