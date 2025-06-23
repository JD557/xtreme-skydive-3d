package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Island(
    model: Vector[Polygon],
    subgoals: Vector[Goal],
    goal: Goal
) {
  lazy val polygons =
    subgoals.flatMap(_.polygons) ++ goal.polygons ++ model
}

object Island {
  val basicIsland = Island(
    model = ObjLoader.loadObj("assets/island1.obj", Some("assets/island1.mtl")),
    subgoals = Vector(
      Goal(-0.5, 0.5, -5.0, 0.2, true),
      Goal(-0.5, 0.5, -2.5, 0.1, true)
    ),
    goal = Goal(-0.5, 0.5, 0.10, 0.05, false)
  )
}
