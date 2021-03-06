package Planning.Plans.Predicates

import Lifecycle.With
import Planning.Plans.Compound.Check

class StartPositionsAtLeast(count: Int) extends Check(() => With.geography.startLocations.size >= count)
