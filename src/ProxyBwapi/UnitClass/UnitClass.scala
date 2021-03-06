package ProxyBwapi.UnitClass

import Mathematics.Points.{Tile, TileRectangle}
import Mathematics.PurpleMath
import Planning.Composition.UnitMatchers.UnitMatcher
import ProxyBwapi.Races.{Neutral, Protoss, Terran, Zerg}
import ProxyBwapi.Techs.{Tech, Techs}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Race, UnitType}

import scala.collection.mutable.ListBuffer

case class UnitClass(base: UnitType) extends UnitClassProxy(base) with UnitMatcher {
  
  lazy val asStringNeat: String = asString
    .replace("Terran_", "")
    .replace("Zerg_", "")
    .replace("Protoss_", "")
    .replace("Neutral_", "")
    .replace("Resource_", "")
    .replace("Critter_", "")
    .replace("Special_", "")
    .replaceAll("_", " ")
  
  override def toString: String = asStringNeat
  
  //////////////
  // Geometry //
  //////////////
  
  lazy val dimensionMin: Int = Math.min(width, height)
  lazy val dimensionMax: Int = Math.max(width, height)
  lazy val radialHypotenuse: Double = Math.sqrt(width.toDouble * width.toDouble + height.toDouble * height.toDouble)/2.0
  
  //////////////
  // Movement //
  //////////////
  
  lazy val accelerationFrames: Int =
    if (acceleration > 1)
      Math.ceil(256.0 * topSpeed / acceleration).toInt
    else
      8 // Arbitrary
  
  lazy val framesToTurn180: Int = framesToTurn(Math.PI)
  
  def framesToTurn(radians: Double): Int = Math.abs(PurpleMath.nanToZero(Math.ceil(128.0 * radians / Math.PI / turnRadius))).toInt
  
  lazy val framesToTurnAndShootAndTurnBackAndAccelerate: Int = stopFrames + accelerationFrames + framesToTurn180
  
  ////////////
  // Combat //
  ////////////
  
  lazy val ranged  : Boolean = rawCanAttack && maxAirGroundRangePixels > 32 * 2
  lazy val melee   : Boolean = rawCanAttack && ! ranged
  
  lazy val suicides: Boolean = Array(
    Terran.SpiderMine,
    Protoss.Scarab,
    Zerg.InfestedTerran,
    Zerg.Scourge)
    .contains(this)
  
  lazy val floats: Boolean = Array(
      Terran.SCV,
      Terran.Vulture,
      Terran.SpiderMine,
      Protoss.Probe,
      Protoss.Archon,
      Protoss.DarkArchon,
      Zerg.Drone)
    .contains(this)
  
  lazy val triggersSpiderMines: Boolean = (
    ! floats
    && ! isFlyer
    && ! isBuilding)
  
  lazy val splashesFriendly: Boolean = Array(
      Terran.SiegeTankSieged,
      Terran.SpiderMine,
      Zerg.InfestedTerran)
    .contains(this)
  
  lazy val dealsRadialSplashDamage: Boolean = Array(
      Terran.SiegeTankSieged,
      Terran.Valkyrie,
      Terran.SpiderMine,
      Protoss.Archon,
      Protoss.Reaver,
      Protoss.Scarab,
      Zerg.InfestedTerran)
    .contains(this)
  
  lazy val maxTotalHealth: Int = maxHitPoints + maxShields
  
  // Via http://www.starcraftai.com/wiki/Regeneration
  lazy val repairHpPerFrame: Double = if (isMechanical) 0.9 * maxHitPoints / Math.max(1.0, buildFrames) else 0.0
  
  lazy val effectiveAirDamage: Int =
    if      (this == Terran.Bunker)   Terran.Marine.airDamageRaw
    else if (this == Protoss.Carrier) Protoss.Interceptor.airDamageRaw
    else                              airDamageRaw
  
  lazy val effectiveGroundDamage: Int =
    if      (this == Terran.Bunker)   Terran.Marine.groundDamageRaw
    else if (this == Protoss.Carrier) Protoss.Interceptor.groundDamageRaw
    else if (this == Protoss.Reaver)  Protoss.Scarab.groundDamageRaw
    else                              groundDamageRaw
  
  private def cooldownZeroBecomesInfinity(cooldown: Int): Int = if (cooldown == 0) Int.MaxValue else cooldown
  
  lazy val airDamageCooldown: Int =
    if      (this == Terran.Bunker)   Terran.Marine.airDamageCooldown
    else if (this == Protoss.Carrier) Protoss.Interceptor.airDamageCooldown
    else                              cooldownZeroBecomesInfinity(airDamageCooldownRaw)
    
  lazy val groundDamageCooldown: Int =
    if      (this == Terran.Bunker)   Terran.Marine.groundDamageCooldown
    else if (this == Protoss.Carrier) Protoss.Interceptor.groundDamageCooldown
    else if (this == Protoss.Reaver)  60
    else                              cooldownZeroBecomesInfinity(groundDamageCooldownRaw)
  
  lazy val attacks        : Boolean = attacksGround || attacksAir
  lazy val attacksGround  : Boolean = effectiveGroundDamage > 0
  lazy val attacksAir     : Boolean = effectiveAirDamage    > 0
  def attacks(enemy: UnitClass): Boolean = if (enemy.isFlyer) attacksAir else attacksGround
  
  lazy val helpsInCombat: Boolean = rawCanAttack || isSpellcaster || Set(Terran.Bunker).contains(this) || this == Zerg.Lurker
  
  lazy val groundRangePixels: Double =
    if      (this == Terran.Bunker)   Terran.Marine.groundRangePixels  + 32.0
    else if (this == Protoss.Carrier) 32.0 * 8.0
    else if (this == Protoss.Reaver)  32.0 * 8.0
    else                              groundRangeRaw
  lazy val airRangePixels: Double =
    if      (this == Terran.Bunker)   Terran.Marine.groundRangePixels  + 32.0
    else if (this == Protoss.Carrier) 32.0 * 8.0
    else                              airRangeRaw
  lazy val maxAirGroundRangePixels  : Double = Math.max(groundRangePixels, airRangePixels)
  lazy val effectiveRangePixels     : Double =
    if (isDetector)                         32.0 * 11.0
    else if (this == Terran.Battlecruiser)  32.0 * 10.0
    else if (this == Protoss.HighTemplar)   32.0 * 9.0
    else if (this == Protoss.Arbiter)       32.0 * 9.0
    else maxAirGroundRangePixels
  
  lazy val tileArea               : TileRectangle = TileRectangle(Tile(0, 0), tileSize)
  lazy val targetsMatter          : Boolean = this != Protoss.Interceptor
  lazy val targetPositionsMatter  : Boolean = this != Protoss.Interceptor
  lazy val orderable              : Boolean = ! isSpell && ! Set(Protoss.Interceptor, Protoss.Scarab, Terran.SpiderMine).contains(this)
  lazy val isResource             : Boolean = isMinerals || isGas
  lazy val isMinerals             : Boolean = isMineralField
  lazy val isGas                  : Boolean = Vector(Neutral.Geyser, Terran.Refinery, Protoss.Assimilator, Zerg.Extractor).contains(this)
  lazy val isTownHall             : Boolean = Vector(Terran.CommandCenter, Protoss.Nexus, Zerg.Hatchery, Zerg.Lair, Zerg.Hive).contains(this)
  lazy val isSiegeTank            : Boolean = this == Terran.SiegeTankSieged || this == Terran.SiegeTankUnsieged
  lazy val isStaticDefense        : Boolean = (isBuilding && attacks || this == Terran.Bunker || this == Protoss.ShieldBattery) && this != Terran.SiegeTankSieged
  lazy val isTransport            : Boolean = spaceProvided > 0 && isFlyer && this != Protoss.Carrier
  lazy val shootsScarabs          : Boolean = this == Protoss.Reaver // Performance shortcut
  
  lazy val unaffectedByDarkSwarm: Boolean = Vector(
    Terran.SiegeTankSieged,
    Terran.Firebat,
    Protoss.Zealot,
    Protoss.DarkTemplar,
    Protoss.Reaver,
    Protoss.Scarab,
    Protoss.Archon,
    Zerg.Zergling,
    Zerg.Lurker,
    Zerg.Ultralisk
  ).contains(this)
  
  ///////////
  // Macro //
  ///////////
  
  lazy val framesToFinishCompletion: Int =
    if      (isProtoss) 75
    else if (isZerg)    12
    else                0
  
  lazy val unitsTrained: Array[UnitClass] = UnitClasses.all.filter(_.whatBuilds._1 == this).toArray
  
  lazy val trainsUnits        : Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this)
  lazy val trainsAirUnits     : Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this && unit.isFlyer)
  lazy val trainsGroundUnits  : Boolean = UnitClasses.all.exists(unit => unit.whatBuilds._1 == this && ! unit.isFlyer)
  
  
  lazy val isProtoss : Boolean = race == Race.Protoss
  lazy val isTerran  : Boolean = race == Race.Terran
  lazy val isZerg    : Boolean = race == Race.Zerg
  
  lazy val buildTechEnabling     : Option[Tech]       = buildTechEnablingCalculate
  lazy val buildUnitsEnabling    : Vector[UnitClass]  = buildUnitsEnablingCalculate
  lazy val buildUnitsBorrowed    : Vector[UnitClass]  = buildUnitsBorrowedCalculate
  lazy val buildUnitsSpent       : Vector[UnitClass]  = buildUnitsSpentCalculate
  
  private def buildTechEnablingCalculate: Option[Tech] = {
    if (requiredTechRaw == Techs.None || requiredTechRaw == Techs.Unknown)
      None
    else
      Some(requiredTechRaw)
  }
  
  private def buildUnitsEnablingCalculate: Vector[UnitClass] = {
    
    val output = new ListBuffer[UnitClass]
  
    //Probes (Protoss buildings)
    addBuildUnitIf(output, isProtoss && isBuilding, Protoss.Probe)
    
    //Pylon (Protoss buildings except Nexus/Pylon/Assimilator)
    addBuildUnitIf(output, requiresPsi, Protoss.Pylon)
    
    //Obvious prerequisites
    addBuildUnitIf(output, Terran.Firebat,              Terran.Academy)
    addBuildUnitIf(output, Terran.Medic,                Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost,                Terran.Academy)
    addBuildUnitIf(output, Terran.Ghost,                Terran.CovertOps)
    addBuildUnitIf(output, Terran.Goliath,              Terran.Armory)
    addBuildUnitIf(output, Terran.SiegeTankUnsieged,    Terran.MachineShop)
    addBuildUnitIf(output, Terran.Dropship,             Terran.ControlTower)
    addBuildUnitIf(output, Terran.Valkyrie,             Terran.Armory)
    addBuildUnitIf(output, Terran.Valkyrie,             Terran.ControlTower)
    addBuildUnitIf(output, Terran.ScienceVessel,        Terran.ScienceFacility)
    addBuildUnitIf(output, Terran.ScienceVessel,        Terran.ControlTower)
    addBuildUnitIf(output, Terran.Battlecruiser,        Terran.PhysicsLab)
    addBuildUnitIf(output, Terran.Battlecruiser,        Terran.ControlTower)
    addBuildUnitIf(output, Terran.MissileTurret,        Terran.EngineeringBay)
    addBuildUnitIf(output, Terran.Bunker,               Terran.Barracks)
    addBuildUnitIf(output, Terran.Factory,              Terran.Barracks)
    addBuildUnitIf(output, Terran.Academy,              Terran.Barracks)
    addBuildUnitIf(output, Terran.Comsat,               Terran.Academy)
    addBuildUnitIf(output, Terran.Armory,               Terran.Factory)
    addBuildUnitIf(output, Terran.Starport,             Terran.Factory)
    addBuildUnitIf(output, Terran.ScienceFacility,      Terran.Factory)
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
    
    output.toVector
  }
  
  private def buildUnitsBorrowedCalculate: Vector[UnitClass] = {
    
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
    
    output.toVector
  }
  
  private def buildUnitsSpentCalculate: Vector[UnitClass] = {
  
    val output = new ListBuffer[UnitClass]
  
    // Archons
    addBuildUnitIf(output, Protoss.Archon,              Protoss.HighTemplar)
    addBuildUnitIf(output, Protoss.Archon,              Protoss.HighTemplar)
    addBuildUnitIf(output, Protoss.DarkArchon,          Protoss.DarkTemplar)
    addBuildUnitIf(output, Protoss.DarkArchon,          Protoss.DarkTemplar)
    
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
    addBuildUnitIf(output, Zerg.SporeColony,            Zerg.CreepColony)
  
    output.toVector
  }
  
  private def addBuildUnitIf(classes:ListBuffer[UnitClass], predicate:Boolean, thenAddThatClass:UnitClass) {
    if (predicate) classes.append(thenAddThatClass)
  }
  
  private def addBuildUnitIf(classes: ListBuffer[UnitClass], ifThisClass: UnitClass, thenAddThatClass: UnitClass) {
    addBuildUnitIf(classes, this == ifThisClass, thenAddThatClass)
  }
  
  lazy val mineralValue     : Int = mineralPrice  + buildUnitsSpent.map(_.mineralValue).sum
  lazy val gasValue         : Int = gasPrice      + buildUnitsSpent.map(_.gasValue).sum
  lazy val subjectiveValue  : Int = (
    (
        1 * mineralValue
      + 2 * gasValue
    )
    * (if(isWorker) 4 else 3)
    * (if(isZerg) 4 else 3)
    * (if (this == Protoss.Carrier)     2 else 1)
    / (if (this == Protoss.Interceptor) 4 else 1)
    / (if (isTwoUnitsInOneEgg) 2 else 1)
    )
  
  //////////////////////
  // Micro frame data //
  //////////////////////
  
  // Largely from https://docs.google.com/spreadsheets/d/1bsvPvFil-kpvEUfSG74U3E5PLSTC02JxSkiR8QdLMuw/edit#gid=0
  //
  // According to professor jaj22:
  // Well, the stop frames are the main guide (to how many frames of movement are lost by attacking)
  // But that won't strictly tell you how many frames you lose.
  //
  lazy val stopFrames: Int = {
    if (this == Terran.SCV                ) 2
    if (this == Terran.Marine             ) 8   else
    if (this == Terran.Firebat            ) 8   else
    if (this == Terran.Ghost              ) 3   else
    if (this == Terran.Vulture            ) 2   else
    if (this == Terran.Goliath            ) 1   else
    if (this == Terran.SiegeTankUnsieged  ) 1   else
    if (this == Terran.SiegeTankSieged    ) 1   else
    if (this == Terran.Wraith             ) 2   else
    if (this == Terran.Battlecruiser      ) 2   else
    if (this == Terran.Valkyrie           ) 40  else
    if (this == Protoss.Probe             ) 2   else
    if (this == Protoss.Zealot            ) 7   else
    if (this == Protoss.Dragoon           ) 2   else
    if (this == Protoss.DarkTemplar       ) 9   else
    if (this == Protoss.Archon            ) 15  else
    if (this == Protoss.Reaver            ) 1   else
    if (this == Protoss.Scout             ) 2   else
    if (this == Protoss.Corsair           ) 8   else
    if (this == Protoss.Arbiter           ) 4   else
    if (this == Zerg.Drone                ) 2   else
    if (this == Zerg.Zergling             ) 4   else
    if (this == Zerg.Hydralisk            ) 3   else
    if (this == Zerg.Lurker               ) 2   else
    if (this == Zerg.Ultralisk            ) 14  else
    if (this == Zerg.Mutalisk             ) 2   else
    if (this == Zerg.Devourer             ) 9   else
    2 // Arbitrary.
  }
  
  // These numbers are taken from Dave Churchill's table,
  // but the Dragoon number at least doesn't seem to correlate to the required delay to prevent attack cancelling.
  lazy val minStop: Int = {
    if (this == Protoss.Dragoon           ) 5   else
    if (this == Zerg.Devourer             ) 7   else
    if (this == Protoss.Carrier           ) 48  else
    0
  }
  
  lazy val attackAnimationFrames: Int = {
    if (this == Terran.SCV                ) 2   else
    if (this == Terran.Marine             ) 8   else
    if (this == Terran.Firebat            ) 8   else
    if (this == Terran.Ghost              ) 4   else
    if (this == Terran.Goliath            ) 1   else
    if (this == Terran.SiegeTankUnsieged  ) 1   else
    if (this == Terran.SiegeTankSieged    ) 1   else
    if (this == Protoss.Zealot            ) 8   else
    if (this == Protoss.Dragoon           ) 9   else
    if (this == Protoss.DarkTemplar       ) 9   else
    if (this == Protoss.Reaver            ) 1   else
    if (this == Protoss.Corsair           ) 8   else
    if (this == Protoss.Arbiter           ) 5   else
    if (this == Zerg.Zergling             ) 5   else
    if (this == Zerg.Hydralisk            ) 3   else
    if (this == Zerg.Ultralisk            ) 15  else
    0
  }
  
  //////////////////
  // Capabilities //
  //////////////////
  
  // These largely exist for performance reasons;
  // We want to avoid calling isTHING methods for units where it will always be true
  
  lazy val canFly               : Boolean = isFlyer || isFlyingBuilding
  lazy val canBurrow            : Boolean = Vector(Terran.SpiderMine, Zerg.Drone, Zerg.Zergling, Zerg.Hydralisk, Zerg.Lurker, Zerg.Defiler).contains(this)
  lazy val canStim              : Boolean = this == Terran.Marine || this == Terran.Firebat
  lazy val canSiege             : Boolean = this == Terran.SiegeTankUnsieged || this == Terran.SiegeTankSieged
  lazy val canBeIrradiated      : Boolean = ! isBuilding
  lazy val canBeIrradiateBurned : Boolean = ! isBuilding && isOrganic
  lazy val canBeLockedDown      : Boolean = ! isBuilding && isMechanical
  lazy val canBeMaelstrommed    : Boolean = ! isBuilding && isOrganic
  lazy val canBeEnsnared        : Boolean = ! isBuilding
  lazy val canBeStasised        : Boolean = ! isBuilding
  lazy val canBeTransported     : Boolean = ! isBuilding && spaceRequired <= 8 // BWAPI gives 255 for unloadable units
  
  /////////////////
  // Convenience //
  /////////////////
  
  // UnitMatcher
  def accept(unit: UnitInfo): Boolean = unit.unitClass == this
}