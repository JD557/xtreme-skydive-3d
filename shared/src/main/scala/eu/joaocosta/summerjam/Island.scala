package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Island(
  model: Vector[Polygon],
  goal: Point,
) {

  lazy val goalPolygons =
    Helpers.toPolygons(
      Helpers.triangulate(
        Shape.rectangle(
          Point(goal.x - 0.025, goal.y - 0.025),
          Point(goal.x + 0.025, goal.y + 0.025)
        )
      ),
      Colors.white,
      0.11
    ) ++ Helpers.toPolygons(
      Helpers.triangulate(
        Shape.rectangle(
          Point(goal.x - 0.05, goal.y - 0.05),
          Point(goal.x + 0.05, goal.y + 0.05)
        )
      ),
      Colors.redLight,
      0.12
    )

  lazy val toPolygons =
    goalPolygons ++
      model
}

object Island {
  val basicIsland = Island(
    model = ObjLoader.loadObj("assets/island1.obj", Some("assets/island1.mtl")),
    goal = Point(-0.5, 0.5),
  )
}
