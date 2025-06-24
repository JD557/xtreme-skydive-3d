package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Island(
    model: Vector[Polygon],
    subgoals: Vector[Goal],
    goal: Goal
) {
  lazy val polygons =
    goal.polygons ++ model

  lazy val specialPolygons = subgoals.reverse.flatMap(_.polygons)
}

object Island {
  val islands = Vector(
    Island(
      model =
        ObjLoader.loadObj("assets/island1.obj", Some("assets/island1.mtl")),
      subgoals = Vector(
        Goal(-0.5, 0.5, -15.0, 0.20, true, 100),
        Goal(-0.5, -0.5, -10.0, 0.20, true, 100),
        Goal(0.5, 0.5, -5.0, 0.20, true, 100),
        Goal(0.5, 0.5, -2.5, 0.10, true, 100)
      ),
      goal = Goal(-0.5, 0.5, 0.20, 0.10, false, 1000)
    ),
    Island(
      model =
        ObjLoader.loadObj("assets/island2.obj", Some("assets/island2.mtl")),
      subgoals = Vector(
        Goal(0, -1, -15.0, 0.1, true, 100),
        Goal(0.75, -0.75, -12.5, 0.1, true, 100),
        Goal(1, 0, -10, 0.1, true, 100),
        Goal(0, 1, -7.5, 0.1, true, 100),
        Goal(-1, 0, -5, 0.1, true, 100)
      ),
      goal = Goal(0.0, 0.0, 0.0, 0.05, false, 1000)
    ),
    Island(
      model =
        ObjLoader.loadObj("assets/island3.obj", Some("assets/island3.mtl")),
      subgoals = Vector(
        Goal(0.25, 0.25, -15.0, 0.1, true, 100),
        Goal(-0.5, -0.5, -13, 0.1, true, 100),
        Goal(0, 0, -11.5, 0.1, true, 100),
        Goal(0.5, 0.5, -10, 0.1, true, 100),
        Goal(0, 0, -7.5, 0.1, true, 100),
        Goal(-1, -1, -5, 0.1, true, 100),
        Goal(-0.9, -0.9, -2.5, 0.1, true, 100)
      ),
      goal = Goal(0.70, 0.70, 0.1, 0.05, false, 1000)
    )
  )
}
