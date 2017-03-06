package Strategies

import Strategies.UnitCounters.UnitCountBetween

object UnitCountEverything extends UnitCountBetween {
  minimum.set(0)
  maximum.set(Int.MaxValue)
}
