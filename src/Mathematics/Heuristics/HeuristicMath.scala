package Mathematics.Heuristics


object HeuristicMath {
  
  val heuristicMaximum = 100000.0
  val heuristicMinimum = 1.0
  val default = heuristicMinimum
  
  def fromBoolean(value:Boolean):Double = if (value) 2.0 else 1.0
  def normalize(value:Double):Double = Math.min(heuristicMaximum, Math.max(heuristicMinimum, value))
  
  def calculateBest[TContext, TCandidate](
    context       : TContext,
    heuristics    : Iterable[HeuristicWeight[TContext, TCandidate]],
    candidates    : Iterable[TCandidate]):TCandidate = {
    
    candidates.maxBy(candidate =>
      heuristics
        .map(_.weigh(context, candidate))
        .product)
  }
}
