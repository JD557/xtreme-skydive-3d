package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Goal(
    x: Double,
    y: Double,
    z: Double,
    radius: Double,
    subGoal: Boolean,
    score: Int
) {
  val innerRadius =
    if (subGoal) radius * 2 / 3
    else radius / 2

  val point = Point(x, y)

  def isHit(playerPos: Point, playerZ: Double): Boolean =
    math.abs(playerZ - z) <= GameConstants.tolerance &&
      math.abs(playerPos.x - x) <= radius &&
      math.abs(playerPos.y - y) <= radius

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
      if (subGoal) Colors.gold.copy(a = 200).premultiplyAlpha
      else Colors.redLight,
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
        if (subGoal) Colors.white.copy(a = 128).premultiplyAlpha
        else Colors.white,
        z
      )
    outerRim ++ innerRim
  }
}
