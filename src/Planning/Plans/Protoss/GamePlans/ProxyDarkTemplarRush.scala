package Planning.Plans.Protoss.GamePlans

import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchMobileDetectors, UnitMatchType}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildAssimilators, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.{FoundEnemyBase, ScoutAt}
import Planning.ProxyPlanner
import ProxyBwapi.Races.Protoss

class ProxyDarkTemplarRush extends Parallel {
  
  children.set(Vector(
    new ProposePlacement {
      override lazy val blueprints: Iterable[Blueprint] = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon)),
        new Blueprint(this, building = Some(Protoss.Gateway)),
        new Blueprint(this, building = Some(Protoss.Pylon),   placementProfile = Some(PlacementProfiles.proxyPylon),    preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placementProfile = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky),
        new Blueprint(this, building = Some(Protoss.Gateway), placementProfile = Some(PlacementProfiles.proxyBuilding), preferZone = ProxyPlanner.proxyAutomaticSneaky))
    },
    
    // Might be the fastest possible DT rush.
    // An example: https://youtu.be/ca40eQ1s7iw
    
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.TemplarArchives), complete = false),
      new If(
        new EnemyUnitsAtLeast(1, UnitMatchMobileDetectors),
        new Parallel(
          new RequireMiningBases(2),
          new Build(
            RequestUpgrade(Protoss.DragoonRange),
            RequestAtLeast(2, Protoss.DarkTemplar))),
        new TrainContinuously(Protoss.DarkTemplar))),
    
    new Build(
      RequestAtLeast(1, Protoss.Nexus),
      RequestAtLeast(8, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon),
      RequestAtLeast(10, Protoss.Probe),
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(11, Protoss.Probe),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(13, Protoss.Probe),
      RequestAtLeast(1, Protoss.CyberneticsCore),
      RequestAtLeast(1, Protoss.Zealot),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(2, Protoss.Zealot),
      RequestAtLeast(2, Protoss.Pylon),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(15, Protoss.Probe),
      RequestAtLeast(4, Protoss.Gateway)),
    
    new RequireSufficientSupply,
    new TrainContinuously(Protoss.Dragoon),
    new TrainWorkersContinuously,
    new Build(RequestAtLeast(4, Protoss.Gateway)),
    new RequireMiningBases(2),
    new BuildAssimilators,
    new Build(RequestAtLeast(8, Protoss.Gateway)),
    
    new If(new Not(new FoundEnemyBase), new ScoutAt(11)),
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}
