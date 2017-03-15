package Planning.Plans.Macro.Automatic

import Planning.Plans.Allocation.LockUnits
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorker
import BWMirrorProxy.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Performance.Caching.Limiter
import bwapi.UnitCommandType

import scala.collection.mutable

class GatherMinerals extends Plan {

  val miners = new LockUnits
  miners.unitMatcher.set(UnitMatchWorker)
  miners.unitCounter.set(UnitCountEverything)
  
  override def getChildren: Iterable[Plan] = List(miners)
  
  var _minerals:List[UnitInfo] = List.empty
  val _workersByMineral = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]] {
    override def default(key: UnitInfo): mutable.HashSet[FriendlyUnitInfo] = { put(key, mutable.HashSet.empty); this(key)}}
  val _mineralByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  val _lastOrderFrame = new mutable.HashMap[FriendlyUnitInfo, Int] {
    override def default(key: FriendlyUnitInfo): Int = { put(key, Int.MinValue); this(key) }}
  
  val _limitResetAssignments = new Limiter(4, _resetAssignments)
  override def onFrame() {
    miners.onFrame()
    _limitResetAssignments.act()
    _assignWorkers()
    miners.units.foreach(_orderWorker)
  }
  
  def _resetAssignments() {
    val allMinerals = With.units.neutral.filter(_.isMinerals)
    val completeBases = With.geography.ourBases.filter(_.townHall.exists(_.complete))
    _mineralByWorker.clear()
    _workersByMineral.clear()
    _minerals = allMinerals.filter(mineral => completeBases.exists(_.harvestingArea.contains(mineral.tileCenter))).toList
    
    //Long-distance mining
    if (_minerals.isEmpty) {
      _minerals = allMinerals.toList.sortBy(_.distance(With.geography.home))
    }
  }
  
  def _assignWorkers() {
    val unassignedWorkers = new mutable.HashSet[FriendlyUnitInfo]
    unassignedWorkers ++= miners.units.diff(_mineralByWorker.keySet)
    _sortMinerals()
    if (_minerals.isEmpty) { return }
    while (unassignedWorkers.nonEmpty) {
      _minerals.foreach(mineral => {
        if (unassignedWorkers.nonEmpty) {
          val worker = unassignedWorkers.minBy(_.pixel.getDistance(mineral.pixel))
          _workersByMineral(mineral).add(worker)
          _mineralByWorker.put(worker, mineral)
          unassignedWorkers.remove(worker)
          _orderWorker(worker)
        }
      })
    }
  }
  
  def _assignWorker(worker:FriendlyUnitInfo) {
    if (_minerals.isEmpty) return
    _sortMinerals()
    val mineral = _minerals.head
    _workersByMineral(mineral).add(worker)
    _mineralByWorker.put(worker, mineral)
  }
  
  def _sortMinerals() {
    val townHalls = With.geography.bases.filter(_.townHall.exists(_.complete)).map(_.centerTile)
    if (townHalls.isEmpty) return
    _minerals
      .sortBy(mineral => -mineral.mineralsLeft)
      .sortBy(mineral => townHalls.map(_.getDistance(mineral.tileCenter)).min)
      .sortBy(mineral => _workersByMineral(mineral).size)
  }
  
  def _orderWorker(worker:FriendlyUnitInfo) {
    if (worker.isGatheringMinerals) {
      _mineralByWorker.get(worker).foreach(mineral => {
        if (mineral.pixel.getDistance(worker.pixel) > 32 * 12) {
          _gather(worker, mineral)
        }
      })
    } else {
      if (worker.isCarryingMinerals || worker.isCarryingGas) {
        //Can't spam return cargo
        if (worker.command.getUnitCommandType != UnitCommandType.Return_Cargo || ! worker.isMoving) {
          //TODO: Krasi0 recommends right clicking CC instead; may stop them from getting stuck
          worker.baseUnit.returnCargo()
        }
      }
      else {
        _mineralByWorker.get(worker).foreach(mineral => _gather(worker, mineral))
      }
    }
  }
  
  def _gather(worker:FriendlyUnitInfo, mineral:UnitInfo) = worker.baseUnit.gather(mineral.baseUnit)
}