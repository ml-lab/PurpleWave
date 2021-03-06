package Planning.Plans.GamePlans.Protoss.Standard.PvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Compound.{Or, _}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Situational.PlaceGatewaysProxied
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Predicates.Economy.GasAtLeast
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Scouting.Scout
import Planning.{Plan, ProxyPlanner}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Strategery.Strategies.Protoss.PvP.PvPOpenProxy2Gate
import Strategery.Strategies.Protoss.PvR.PvROpenProxy2Gate
import Strategery.Strategies.Protoss.PvT.PvTProxy2Gate
import Strategery.Strategies.Protoss.PvZ.PvZProxy2Gate

class Proxy2Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvROpenProxy2Gate, PvTProxy2Gate, PvPOpenProxy2Gate, PvZProxy2Gate)
  override val completionCriteria = new UpgradeComplete(Protoss.DragoonRange)
  override def defaultScoutPlan   = new If(new UnitsAtLeast(2, Protoss.Gateway), new Scout)
  override val aggression         = 1.2
  override def defaultSupplyPlan: Plan = NoPlan()
  override def defaultWorkerPlan: Plan = NoPlan()
  
  private class BeforeProxy extends Parallel(
    new PlaceGatewaysProxied(2, () => ProxyPlanner.proxyAutomaticSneaky),
    new BuildOrder(
      RequestAtLeast(8, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(9, Protoss.Probe)),
    new If(new UnitsAtLeast(1, Protoss.Pylon),    new Build(RequestAtLeast(1, Protoss.Gateway))),
    new If(new UnitsAtLeast(1, Protoss.Gateway),  new Build(RequestAtLeast(2, Protoss.Gateway))))
  
  private class MustSwitchToDragoons extends Or(
    new UnitsAtLeast(15, Protoss.Probe),
    new EnemyHasShown(Terran.Vulture),
    new EnemyHasShown(Protoss.Dragoon),
    new EnemyUnitsAtLeast(1, Zerg.Spire, complete = true),
    new EnemyUnitsAtLeast(1, Terran.Factory, complete = true),
    new EnemyWalledIn)
  
  private class AfterProxy extends Parallel(
    new RequireSufficientSupply,
    new BuildOrder(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Zealot)),
    new TrainContinuously(Protoss.Observer, 2),
    new If(
      new And(
        new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeFrames(1)),
        new GasAtLeast(50)),
      new TrainContinuously(Protoss.Dragoon),
      new If(
        new Not(new MustSwitchToDragoons),
        new TrainContinuously(Protoss.Zealot, 8))),
    new TrainWorkersContinuously,
    new Trigger(
      new Or(
        new UnitsAtLeast(15, Protoss.Probe),
        new MustSwitchToDragoons),
      initialAfter = new Parallel(
        new BuildGasPumps,
        new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
        new If(
          new EnemyHasShownCloakedThreat,
          new Parallel(
            new Build(
              RequestAtLeast(1, Protoss.RoboticsFacility),
              RequestAtLeast(1, Protoss.Observatory)))),
        new Build(
          RequestUpgrade(Protoss.DragoonRange),
          RequestAtLeast(4, Protoss.Gateway)))))
  
  override def buildPlans = Vector(
    new Do(() =>  With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue),
    new Trigger(new UnitsAtLeast(2, Protoss.Gateway),
      initialBefore = new BeforeProxy,
      initialAfter  = new AfterProxy)
  )
}