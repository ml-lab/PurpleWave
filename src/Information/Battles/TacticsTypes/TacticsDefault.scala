package Information.Battles.TacticsTypes

object TacticsDefault {
  def get:TacticsOptions = {
    val output = new TacticsOptions()
    output.add(Tactics.Movement.Advance)
    output.add(Tactics.Focus.Neither)
    output.add(Tactics.Wounded.Fight)
    output.add(Tactics.Workers.Ignore)
    output
  }
}
