package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Goal(
    val x: Double,
    val y: Double,
    val z: Double,
    val radius: Double,
    val subGoal: Boolean
) {
  val innerRadius =
    if (subGoal) radius * 2 / 3
    else radius / 2

  val point = Point(x, y)

  lazy val polygons = {
    val outerRim = Helpers.toPolygons(
      Vector(
        Shape.rectangle( // Top
          Point(x - radius, y - radius),
          Point(x + radius, y - innerRadius)
        ),
        Shape.rectangle( // Bottom
          Point(x - radius, y + innerRadius),
          Point(x + radius, y + radius)
        ),
        Shape.rectangle( // Left
          Point(x - radius, y - radius),
          Point(x - innerRadius, y + radius)
        ),
        Shape.rectangle( // Right
          Point(x + innerRadius, y - radius),
          Point(x + radius, y + radius)
        )
      ).flatMap(Helpers.triangulate),
      if (subGoal) Colors.gold else Colors.redLight,
      z
    )
    val innerRim =
      Helpers.toPolygons(
        Helpers.triangulate(
          Shape.rectangle(
            Point(x - innerRadius, y - innerRadius),
            Point(x + innerRadius, y + innerRadius)
          )
        ),
        Colors.white,
        z
      )
    if (subGoal) outerRim
    else outerRim ++ innerRim
  }
}
