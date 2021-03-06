package Planning.Plans.Macro.BuildOrders

import Macro.BuildRequests.BuildRequest
import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With

class Build(initialRequests: BuildRequest*) extends Plan {
  
  val requests = new Property[Seq[BuildRequest]](initialRequests)
  
  override def onUpdate() {
    
    description.set(
      "Build " +
      requests.get.take(3).map(_.toString).mkString(", ") +
      (if (requests.get.size > 3) "..." else ""))
    
    With.scheduler.request(this, requests.get)
  }
}
