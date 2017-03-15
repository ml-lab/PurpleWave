package Planning.Composition.UnitMatchers
import BWMirrorProxy.UnitInfo.FriendlyUnitInfo

class UnitMatchAnd(matches: Iterable[UnitMatcher]) extends UnitMatcher {
  override def accept(unit: FriendlyUnitInfo): Boolean = matches.forall(_.accept(unit))
}