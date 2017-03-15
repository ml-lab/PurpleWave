package BWMirrorProxy.UnitInfo
import Startup.With
import Performance.Caching.CacheFrame
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi._

import scala.collection.JavaConverters._

class FriendlyUnitInfo(_baseUnit:bwapi.Unit) extends UnitInfo(_baseUnit) {
  val _cacheUnitType  = new CacheFrame[UnitType]      (() =>  _baseUnit.getType)
  val _cachePlayer    = new CacheFrame[Player]        (() =>  _baseUnit.getPlayer)
  val _cachePosition  = new CacheFrame[Position]      (() =>  _baseUnit.getPosition)
  val _cacheTile      = new CacheFrame[TilePosition]  (() =>  _baseUnit.getTilePosition)
  val _cacheCompleted = new CacheFrame[Boolean]       (() =>  _baseUnit.isCompleted)
  val _cacheExists    = new CacheFrame[Boolean]       (() =>  _baseUnit.exists)
  val _cacheId        = new CacheFrame[Int]           (() =>  _baseUnit.getID)
  
  override def friendly                   : Option[FriendlyUnitInfo]  = Some(this)
  override def lastSeen                   : Int                       = With.frame
  override def possiblyStillThere         : Boolean                   = true
  override def alive                      : Boolean                   = _cacheExists.get
  override def player                     : Player                    = _cachePlayer.get
  override def pixel                   : Position                  = _cachePosition.get
  override def walkPosition               : WalkPosition              = pixel.toWalkPosition
  override def tileTopLeft                : TilePosition              = _cacheTile.get
  override def hitPoints                  : Int                       = baseUnit.getHitPoints
  override def shieldPoints               : Int                       = baseUnit.getShields
  override def utype                      : UnitType                  = _cacheUnitType.get
  override def complete                   : Boolean                   = _cacheCompleted.get
  override def flying                     : Boolean                   = baseUnit.isFlying
  override def visible                    : Boolean                   = baseUnit.isVisible
  override def cloaked                    : Boolean                   = baseUnit.isCloaked
  override def detected                   : Boolean                   = baseUnit.isDetected
  override def morphing                   : Boolean                   = baseUnit.isMorphing
  override def burrowed                   : Boolean                   = baseUnit.isBurrowed
  override def invincible                 : Boolean                   = baseUnit.isInvincible
  override def top                        : Int                       = baseUnit.getTop
  override def left                       : Int                       = baseUnit.getLeft
  override def right                      : Int                       = baseUnit.getRight
  override def bottom                     : Int                       = baseUnit.getBottom
  def isCarryingMinerals                  : Boolean                   = baseUnit.isCarryingMinerals
  def isCarryingGas                       : Boolean                   = baseUnit.isCarryingGas
  def isGatheringMinerals                 : Boolean                   = baseUnit.isGatheringMinerals
  def isGatheringGas                      : Boolean                   = baseUnit.isGatheringGas
  def isMoving                            : Boolean                   = baseUnit.isMoving
  def command                             : UnitCommand               = baseUnit.getLastCommand
  def cooldownRemaining                   : Int                       = Math.max(baseUnit.getGroundWeaponCooldown, baseUnit.getAirWeaponCooldown)
  def onCooldown                          : Boolean                   = cooldownRemaining > 0 || ! canFight
  def energy                              : Int                       = baseUnit.getEnergy
  def scarabs                             : Int                       = baseUnit.getScarabCount
  def interceptors                        : Int                       = baseUnit.getInterceptorCount
  def getBuildUnit                        : Option[UnitInfo]          = With.units.getUnit(baseUnit.getBuildUnit)
  def trainingQueue                       : Iterable[UnitType]        = baseUnit.getTrainingQueue.asScala
  def teching                             : TechType                  = baseUnit.getTech
  def upgrading                           : UpgradeType               = baseUnit.getUpgrade
  def order                               : Order                     = baseUnit.getOrder
  def framesBeforeBecomingComplete        : Int                       = baseUnit.getRemainingBuildTime
  def framesBeforeBuildeeComplete         : Int                       = baseUnit.getRemainingTrainTime
  def framesBeforeTechComplete            : Int                       = baseUnit.getRemainingResearchTime
  def framesBeforeUpgradeComplete         : Int                       = baseUnit.getRemainingUpgradeTime
}