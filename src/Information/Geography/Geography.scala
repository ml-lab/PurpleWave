package Information.Geography

import BWMirrorProxy.UnitInfo.{ForeignUnitInfo, UnitInfo}
import Geometry.Shapes.Circle
import Geometry._
import Performance.Caching.{Cache, CacheForever, Limiter}
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import Utilities.TypeEnrichment.EnrichUnitType._
import bwapi.{TilePosition, UnitType}
import bwta.BWTA

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class Geography {
  
  def zones:Iterable[Zone] = {
    _zoneLimiter.act()
    _zoneCache.get
  }
  val _zoneCache = new CacheForever[Iterable[Zone]](() => _zones)
  val _zoneLimiter = new Limiter(10, _updateZones)
  def _zones:Iterable[Zone] = {
    //Build zones
    val zonesByRegionCenter = BWTA.getRegions.asScala.map(region =>
      (region.getCenter,
      new Zone(
        region.getCenter.toTilePosition,
        region,
        new ListBuffer[Base],
        new ListBuffer[ZoneEdge])))
      .toMap
    
    //Build bases
    val allBases = townHallPositions.map(townHallPosition => {
      val townHallArea = UnitType.Terran_Command_Center.area.add(townHallPosition)
      new Base(
        zonesByRegionCenter.values
          .find(_.region.getPolygon.isInside(townHallPosition.toPosition))
          .getOrElse(zonesByRegionCenter.values.minBy(_.region.getCenter.pixelDistance(townHallPosition.centerPixel))),
        townHallArea,
        getHarvestingArea(townHallArea),
        With.game.getStartLocations.asScala.exists(_.tileDistance(townHallPosition) < 6))
    })
    
    //Populate zones with bases
    allBases.foreach(base => base.zone.bases += base)
    
    //Add edges
    val edges = BWTA.getChokepoints.asScala.map(choke =>
      new ZoneEdge(
        choke,
        List(choke.getRegions.first, choke.getRegions.second)
          .map(region => zonesByRegionCenter(region.getCenter))))
    
    edges.foreach(edge => edge.zones.foreach(zone => zone.edges += edge))
    
    return zonesByRegionCenter.values
  }
  def _updateZones() =
    _zoneCache.get.flatten(_.bases).foreach(base => {
      val townHall = With.units.buildings.filter(_.utype.isTownHall)
        .filter(townHall => base.zone.region.getPolygon.isInside(townHall.pixel))
        .toList
        .sortBy(_.distance(base.townHallArea.midpoint))
        .headOption
      base.townHall = townHall
      base.zone.owner = townHall.map(_.player).headOption.getOrElse(With.game.neutral)
    })
  
  def bases:Iterable[Base] = zones.flatten(_.bases)
  def ourBases:Iterable[Base] = bases.filter(_.zone.owner == With.self)
  def ourBaseHalls:Iterable[UnitInfo] = ourBases.filter(_.townHall.isDefined).map(_.townHall.get)
  def ourHarvestingAreas:Iterable[TileRectangle] = ourBases.map(_.harvestingArea)
  
  def home:TilePosition = _cacheHome.get
  val _cacheHome = new Cache[TilePosition](5, () => _calculateHome)
  def _calculateHome:TilePosition =
    ourBases.toList
      .sortBy( ! _.isStartLocation).map(_.centerTile)
      .headOption
      .getOrElse(With.units.ours.view.filter(_.utype.isBuilding).map(_.tileCenter).headOption
      .getOrElse(Positions.tileMiddle))
  
  def getHarvestingArea(townHallArea:TileRectangle):TileRectangle = {
    val resources = With.units
      .inRadius(townHallArea.midpoint.centerPixel, 32 * 10)
      .filter(_.isResource)
      .map(_.tileArea)
    
    (List(townHallArea) ++ resources).boundary
  }
  
  def townHallPositions:Iterable[TilePosition] = _basePositionsCache.get
  val _basePositionsCache = new Cache[Iterable[TilePosition]](5, () => _calculateBasePositions)
  val _resourceClusterCache = new CacheForever[Iterable[Iterable[ForeignUnitInfo]]](() => _getResourceClusters)
  val _resourceExclusionCache = new CacheForever[Iterable[TileRectangle]](() => _getExclusions)
  def _getResourceClusters:Iterable[Iterable[ForeignUnitInfo]] = Clustering.group[ForeignUnitInfo](_getResources, 32 * 12, true, (unit) => unit.pixel).values
  def _getExclusions:Iterable[TileRectangle] = _getResources.map(_getExclusion)
  def _getExclusion(unit:ForeignUnitInfo):TileRectangle = new TileRectangle(unit.tileTopLeft.subtract(3, 3), unit.tileTopLeft.add(unit.utype.tileSize).add(3, 3))
  def _getResources:Iterable[ForeignUnitInfo] = With.units.neutral.filter(unit => unit.isMinerals || unit.isGas).filter(_.baseUnit.getInitialResources > 0)
  def _calculateBasePositions:Iterable[TilePosition] = _resourceClusterCache.get.flatMap(_findBasePosition)
  def _findBasePosition(resources:Iterable[ForeignUnitInfo]):Option[TilePosition] = {
    val centroid = resources.map(_.pixel).centroid
    val centroidTile = centroid.toTilePosition
    val searchRadius = 10
    val candidates = Circle.points(searchRadius).map(centroidTile.add).filter(_isLegalBasePosition)
    if (candidates.isEmpty) return None
    Some(candidates.minBy(_.toPosition.add(64, 48).getDistance(centroid)))
  }
  def _isLegalBasePosition(position:TilePosition):Boolean = {
    val exclusions = _resourceExclusionCache.get
    val buildingArea = new TileRectangle(position, position.add(UnitType.Zerg_Hatchery.tileSize))
    buildingArea.tiles.forall(With.grids.buildableTerrain.get) && exclusions.forall( ! _.intersects(buildingArea))
  }
}