package ProxyBwapi.UnitClass

import Mathematics.Positions.TileRectangle
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import bwapi.{DamageType, Race, TilePosition, UnitType}

import scala.collection.mutable.ListBuffer

case class UnitClass(base:UnitType) extends UnitClassProxy(base) {
  
  lazy val asStringNeat = asString
    .replace("Terran_", "")
    .replace("Zerg_", "")
    .replace("Protoss_", "")
    .replace("Neutral_", "")
    .replace("Resource_", "")
    .replace("Critter_", "")
    .replace("Special_", "")
    .replaceAll("_", " ")
  
  override def toString:String = asStringNeat
  
  //////////////
  // Geometry //
  //////////////
  
  def radialHypotenuse = Math.sqrt(width.toDouble * width.toDouble + height.toDouble * height.toDouble)/2.0
  
  ////////////
  // Combat //
  ////////////
  
  //TODO: Explosive is 50/75/100
  //But Concussive is 25/50/100, not 50/75/100 !!!
  private val concussiveOrExplosive = List(DamageType.Concussive, DamageType.Explosive)
  
  lazy val effectiveAirDamage:Double =
    if      (this == Protoss.Carrier)      Protoss.Interceptor.effectiveAirDamage * 8
    else if (this == Protoss.Interceptor)  0
    else if (this == Terran.Bunker)        Terran.Marine.effectiveAirDamage * 4
    else (if (concussiveOrExplosive.contains(rawAirDamageType)) 0.75 else 1.0) *
      maxAirHits *
      rawAirDamageFactor *
      rawAirDamage
  
  lazy val effectiveGroundDamage:Double =
    if      (this == Protoss.Carrier)       (Protoss.Interceptor.effectiveGroundDamage * 8)
    else if (this == Protoss.Interceptor)   0
    else if (this == Protoss.Reaver)        Protoss.Scarab.effectiveGroundDamage
    else if (this == Terran.Bunker)         Terran.Marine.effectiveGroundDamage * 4
    else (if (concussiveOrExplosive.contains(rawGroundDamageType)) 0.75 else 1.0) *
      maxGroundHits *
      rawGroundDamageFactor *
      rawGroundDamage
    
    val typeMultiplier = if (concussiveOrExplosive.contains(rawGroundDamageType)) 0.75 else 1.0
    typeMultiplier *
      maxGroundHits *
      rawGroundDamageFactor *
      rawGroundDamage
  
  lazy val airDamageCooldown:Int = rawAirDamageCooldown
  lazy val groundDamageCooldown:Int =
    //Necessary according to Skynet: https://github.com/Laccolith/skynet/blob/399018f41b49fbb55a0ea32142117e97e9d2f9ae/Skynet/Unit.cpp#L1092
    if (this == Protoss.Reaver) 60 else rawGroundDamageCooldown
  
  //The extra 2+ is to account for the 1-3 frame random variation in cooldown
  lazy val airDps    : Double = effectiveAirDamage    * 24 / (2 + airDamageCooldown).toDouble
  lazy val groundDps : Double = effectiveGroundDamage * 24 / (2 + groundDamageCooldown).toDouble
  
  lazy val attacksGround : Boolean = effectiveGroundDamage > 0
  lazy val attacksAir    : Boolean = effectiveAirDamage    > 0
  
  lazy val helpsInCombat:Boolean = canAttack || isSpellcaster || Set(Terran.Bunker, Terran.Medic).contains(this)
  
  lazy val groundRange        : Int = if (this == Terran.Bunker) Terran.Marine.groundRange  + 32 else rawGroundRange
  lazy val airRange           : Int = if (this == Terran.Bunker) Terran.Marine.airRange     + 32 else rawAirRange
  lazy val maxAirGroundRange  : Int = Math.max(groundRange, airRange)
  
  lazy val isResource:Boolean = isMinerals || isGas
  lazy val maxTotalHealth:Int = maxHitPoints + maxShields
  lazy val totalCost: Int = mineralPrice + gasPrice
  lazy val orderable:Boolean = ! isSpell && ! Set(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine).contains(this)
  lazy val isMinerals:Boolean = isMineralField
  lazy val isGas:Boolean = List(Neutral.Geyser, Terran.Refinery, Protoss.Assimilator, Zerg.Extractor).contains(this)
  lazy val tileArea:TileRectangle = new TileRectangle(new TilePosition(0, 0), tileSize)
  lazy val isTownHall:Boolean = List(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery, Zerg.Lair, Zerg.Hive).contains(this)
  
  ///////////
  // Macro //
  ///////////
  
  lazy val isProtoss : Boolean = race == Race.Protoss
  lazy val isTerran  : Boolean = race == Race.Terran
  lazy val isZerg    : Boolean = race == Race.Zerg
  
  lazy val buildTechEnabling     : Tech            = requiredTech
  lazy val buildUnitsEnabling    : List[UnitClass] = buildUnitsEnablingCalculate
  lazy val buildUnitsBorrowed    : List[UnitClass] = buildUnitsBorrowedCalculate
  lazy val buildUnitsSpent       : List[UnitClass] = buildUnitsSpentCalculate
  
  private def buildUnitsEnablingCalculate: List[UnitClass] = {
    
    val output = new ListBuffer[UnitClass]
    
    //Pylon (Protoss buildings except Nexus/Pylon/Assimilator)
    addBuildUnitIf(output, requiresPsi, Protoss.Pylon)
    
    //Obvious prerequisites
    addBuildUnitIf(output, Terran.Firebat,              Terran.Academy)
    addBuildUnitIf(output, Terran.Medic,                Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost,                Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost,                Terran.CovertOps)
    addBuildUnitIf(output, Terran.Goliath,              Terran.Armory)
    addBuildUnitIf(output, Terran.SiegeTankUnsieged,    Terran.MachineShop)
    addBuildUnitIf(output, Terran.Valkyrie,             Terran.Armory)
    addBuildUnitIf(output, Terran.ScienceVessel,        Terran.ScienceFacility)
    addBuildUnitIf(output, Terran.Dropship,             Terran.ControlTower)
    addBuildUnitIf(output, Terran.ScienceVessel,        Terran.ControlTower)
    addBuildUnitIf(output, Terran.Battlecruiser,        Terran.ControlTower)
    addBuildUnitIf(output, Terran.Battlecruiser,        Terran.PhysicsLab)
    addBuildUnitIf(output, Terran.MissileTurret,        Terran.EngineeringBay)
    addBuildUnitIf(output, Terran.Bunker,               Terran.Barracks)
    addBuildUnitIf(output, Terran.Factory,              Terran.Barracks)
    addBuildUnitIf(output, Terran.Academy,              Terran.Barracks)
    addBuildUnitIf(output, Terran.Comsat,               Terran.Barracks)
    addBuildUnitIf(output, Terran.Armory,               Terran.Factory)
    addBuildUnitIf(output, Terran.Starport,             Terran.Factory)
    addBuildUnitIf(output, Terran.ScienceFacility,      Terran.Starport)
    addBuildUnitIf(output, Terran.NuclearSilo,          Terran.CovertOps)
    addBuildUnitIf(output, Protoss.Dragoon,             Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.HighTemplar,         Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.DarkTemplar,         Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.Arbiter,             Protoss.TemplarArchives)
    addBuildUnitIf(output, Protoss.Reaver,              Protoss.RoboticsSupportBay)
    addBuildUnitIf(output, Protoss.Observer,            Protoss.Observatory)
    addBuildUnitIf(output, Protoss.Carrier,             Protoss.FleetBeacon)
    addBuildUnitIf(output, Protoss.Arbiter,             Protoss.ArbiterTribunal)
    addBuildUnitIf(output, Protoss.PhotonCannon,        Protoss.Forge)
    addBuildUnitIf(output, Protoss.ShieldBattery,       Protoss.Gateway)
    addBuildUnitIf(output, Protoss.CyberneticsCore,     Protoss.Gateway)
    addBuildUnitIf(output, Protoss.RoboticsFacility,    Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.Stargate,            Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.CitadelOfAdun,       Protoss.CyberneticsCore)
    addBuildUnitIf(output, Protoss.TemplarArchives,     Protoss.CitadelOfAdun)
    addBuildUnitIf(output, Protoss.RoboticsSupportBay,  Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Observatory,         Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.FleetBeacon,         Protoss.Stargate)
    addBuildUnitIf(output, Protoss.ArbiterTribunal,     Protoss.Stargate)
    addBuildUnitIf(output, Protoss.ArbiterTribunal,     Protoss.TemplarArchives)
    addBuildUnitIf(output, Zerg.Zergling,               Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Hydralisk,              Zerg.HydraliskDen)
    addBuildUnitIf(output, Zerg.Mutalisk,               Zerg.Spire)
    addBuildUnitIf(output, Zerg.Queen,                  Zerg.QueensNest)
    addBuildUnitIf(output, Zerg.Ultralisk,              Zerg.UltraliskCavern)
    addBuildUnitIf(output, Zerg.Defiler,                Zerg.DefilerMound)
    addBuildUnitIf(output, Zerg.Guardian,               Zerg.GreaterSpire)
    addBuildUnitIf(output, Zerg.Devourer,               Zerg.GreaterSpire)
    addBuildUnitIf(output, Zerg.SporeColony,            Zerg.EvolutionChamber)
    addBuildUnitIf(output, Zerg.SunkenColony,           Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.HydraliskDen,           Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Lair,                   Zerg.SpawningPool)
    addBuildUnitIf(output, Zerg.Spire,                  Zerg.Lair)
    addBuildUnitIf(output, Zerg.QueensNest,             Zerg.Lair)
    addBuildUnitIf(output, Zerg.Hive,                   Zerg.QueensNest)
    addBuildUnitIf(output, Zerg.UltraliskCavern,        Zerg.Hive)
    addBuildUnitIf(output, Zerg.DefilerMound,           Zerg.Hive)
    addBuildUnitIf(output, Zerg.GreaterSpire,           Zerg.Hive)
    addBuildUnitIf(output, Zerg.NydusCanal,             Zerg.Hive)
    
    output.toList
  }
  
  private def buildUnitsBorrowedCalculate: List[UnitClass] = {
    
    val output = new ListBuffer[UnitClass]
  
    //All Terran units that train from buildings
    addBuildUnitIf(output, Terran.SCV,                  Terran.CommandCenter)
    addBuildUnitIf(output, Terran.Marine,               Terran.Barracks)
    addBuildUnitIf(output, Terran.Firebat,              Terran.Barracks)
    addBuildUnitIf(output, Terran.Medic,                Terran.Barracks)
    addBuildUnitIf(output, Terran.Ghost,                Terran.Barracks)
    addBuildUnitIf(output, Terran.Vulture,              Terran.Factory)
    addBuildUnitIf(output, Terran.Goliath,              Terran.Factory)
    addBuildUnitIf(output, Terran.SiegeTankUnsieged,    Terran.Factory)
    addBuildUnitIf(output, Terran.Dropship,             Terran.Starport)
    addBuildUnitIf(output, Terran.Wraith,               Terran.Starport)
    addBuildUnitIf(output, Terran.Valkyrie,             Terran.Starport)
    addBuildUnitIf(output, Terran.ScienceVessel,        Terran.Starport)
    addBuildUnitIf(output, Terran.Battlecruiser,        Terran.Starport)
    addBuildUnitIf(output, Terran.NuclearMissile,       Terran.NuclearSilo)
  
    //SCV (for all Terran building except add-ons)
    addBuildUnitIf(output, isBuilding && race == Race.Terran && ! isAddon, Terran.SCV)
  
    //Factory (for Machine Shop)
    //Starport (for Control Tower)
    //Science Facility (for Covert Ops/Physics Lab)
    //Command Center (for Comsat/Nuke Silo)
    addBuildUnitIf(output, isAddon, whatBuilds._1)
    
    //All Protoss units that train from buildings
    addBuildUnitIf(output, Protoss.Probe,               Protoss.Nexus)
    addBuildUnitIf(output, Protoss.Zealot,              Protoss.Gateway)
    addBuildUnitIf(output, Protoss.Dragoon,             Protoss.Gateway)
    addBuildUnitIf(output, Protoss.HighTemplar,         Protoss.Gateway)
    addBuildUnitIf(output, Protoss.DarkTemplar,         Protoss.Gateway)
    addBuildUnitIf(output, Protoss.Shuttle,             Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Reaver,              Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Observer,            Protoss.RoboticsFacility)
    addBuildUnitIf(output, Protoss.Scout,               Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Corsair,             Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Carrier,             Protoss.Stargate)
    addBuildUnitIf(output, Protoss.Arbiter,             Protoss.Stargate)
  
    // Pop quiz: What's the only Zerg unit that trains from a building?
    addBuildUnitIf(output, Zerg.InfestedTerran,         Zerg.InfestedCommandCenter)
    addBuildUnitIf(output, Zerg.InfestedCommandCenter,  Zerg.Queen)
    
    output.toList
  }
  
  private def buildUnitsSpentCalculate: List[UnitClass] = {
  
    val output = new ListBuffer[UnitClass]
  
    // Archons
    addBuildUnitIf(output, Protoss.HighTemplar,         Protoss.Archon)
    addBuildUnitIf(output, Protoss.HighTemplar,         Protoss.Archon)
    addBuildUnitIf(output, Protoss.DarkTemplar,         Protoss.DarkArchon)
    addBuildUnitIf(output, Protoss.DarkTemplar,         Protoss.DarkArchon)
    
    // Larva (All Zerg non-building units except Lurker/Guardian/Devourer)
    addBuildUnitIf(output, Zerg.Drone,                  Zerg.Larva)
    addBuildUnitIf(output, Zerg.Overlord,               Zerg.Larva)
    addBuildUnitIf(output, Zerg.Zergling,               Zerg.Larva)
    addBuildUnitIf(output, Zerg.Hydralisk,              Zerg.Larva)
    addBuildUnitIf(output, Zerg.Mutalisk,               Zerg.Larva)
    addBuildUnitIf(output, Zerg.Ultralisk,              Zerg.Larva)
    addBuildUnitIf(output, Zerg.Queen,                  Zerg.Larva)
    addBuildUnitIf(output, Zerg.Defiler,                Zerg.Larva)
    
    // Drone (Most Zerg buildings)
    addBuildUnitIf(output, Zerg.Hatchery,               Zerg.Drone)
    addBuildUnitIf(output, Zerg.Extractor,              Zerg.Drone)
    addBuildUnitIf(output, Zerg.CreepColony,            Zerg.Drone)
    addBuildUnitIf(output, Zerg.SpawningPool,           Zerg.Drone)
    addBuildUnitIf(output, Zerg.EvolutionChamber,       Zerg.Drone)
    addBuildUnitIf(output, Zerg.HydraliskDen,           Zerg.Drone)
    addBuildUnitIf(output, Zerg.Spire,                  Zerg.Drone)
    addBuildUnitIf(output, Zerg.QueensNest,             Zerg.Drone)
    addBuildUnitIf(output, Zerg.UltraliskCavern,        Zerg.Drone)
    addBuildUnitIf(output, Zerg.DefilerMound,           Zerg.Drone)
    addBuildUnitIf(output, Zerg.NydusCanal,             Zerg.Drone)
    
    // Zerg morphs
    addBuildUnitIf(output, Zerg.Lurker,                 Zerg.Hydralisk)
    addBuildUnitIf(output, Zerg.Guardian,               Zerg.Mutalisk)
    addBuildUnitIf(output, Zerg.Defiler,                Zerg.Mutalisk)
    addBuildUnitIf(output, Zerg.Lair,                   Zerg.Hatchery)
    addBuildUnitIf(output, Zerg.Hive,                   Zerg.Lair)
    addBuildUnitIf(output, Zerg.SunkenColony,           Zerg.CreepColony)
    addBuildUnitIf(output, Zerg.SporeColony,            Zerg.SporeColony)
  
    output.toList
  }
  
  private def addBuildUnitIf(classes:ListBuffer[UnitClass], predicate:Boolean, thenAddThatClass:UnitClass) {
    if (predicate) classes.append(thenAddThatClass)
  }
  
  private def addBuildUnitIf(classes:ListBuffer[UnitClass], ifThisClass:UnitClass, thenAddThatClass:UnitClass) {
    addBuildUnitIf(classes, this == ifThisClass, thenAddThatClass)
  }
}
