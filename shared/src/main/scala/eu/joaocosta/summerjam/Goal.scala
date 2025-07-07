package eu.joaocosta.summerjam

import upickle.default.*

import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

final case class Goal(
    x: Double,
    y: Double,
    z: Double,
    radius: Double,
    subGoal: Boolean,
    score: Int
) derives ReadWriter {
  val innerRadius =
    if (subGoal) radius * 2 / 3
    else radius / 2

  val point = Point(x, y)

  def isHit(playerPos: Point, playerZ: Double): Boolean =
    math.abs(playerZ - z) <= GameConstants.tolerance &&
      math.abs(playerPos.x - x) <= radius &&
      math.abs(playerPos.y - y) <= radius

  def canBeDiscarded(playerPos: Point, playerZ: Double): Boolean =
    playerZ - z >= GameConstants.tolerance

  private lazy val outerRimShape = Vector(
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
  ).flatMap(Helpers.triangulate)

  private lazy val innerRimShape =
    Helpers.triangulate(
      Shape.rectangle(
        Point(x - innerRadius, y - innerRadius),
        Point(x + innerRadius, y + innerRadius)
      )
    )

  def polygons(innerColor: Color, outerColor: Color) = {
    val outerRim = Helpers.toPolygons(
      outerRimShape,
      outerColor,
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
        innerColor,
        z
      )
    outerRim ++ innerRim
  }
}
