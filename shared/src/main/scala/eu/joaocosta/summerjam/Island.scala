package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Island(
    sand: Vector[ConvexPolygon],
    grass: Vector[ConvexPolygon],
    goal: Point
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
      Helpers.toPolygons(grass, Colors.grassLight, 0.13) ++
      Helpers.toPolygons(sand, Colors.beachLight, 0.14)
}

object Island {
  val basicIsland = Island(
    sand = Vector(
      ConvexPolygon(
        Vector(
          Point(-0.6, -0.42),
          Point(-0.3, -0.22),
          Point(-0.7, 0.02),
          Point(-0.84, -0.26)
        )
      ),
      ConvexPolygon(
        Vector(
          Point(-0.7, 0.02),
          Point(-0.3, -0.22),
          Point(-0.1, -0.28),
          Point(0.14, -0.28),
          Point(0.56, 0),
          Point(0.3, 0.3),
          Point(-0.16, 0.28)
        )
      )
    ).flatMap(Helpers.triangulate),
    grass = Vector(
      ConvexPolygon(
        Vector(
          Point(-0.4, 0.02),
          Point(-0.3, -0.1),
          Point(-0.1, -0.18),
          Point(0.14, -0.18),
          Point(0.46, 0),
          Point(0.3, 0.2),
          Point(-0.16, 0.2)
        )
      )
    ).flatMap(Helpers.triangulate),
    goal = Point(-0.55, -0.2)
  )
}
