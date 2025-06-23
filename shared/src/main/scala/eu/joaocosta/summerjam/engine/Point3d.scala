package eu.joaocosta.summerjam.engine

import eu.joaocosta.minart.geometry.Point

final case class Point3d(x: Double, y: Double, z: Double) {
  lazy val project: Point = {
    val minSize = Math.min(Constants.screenWidth, Constants.screenHeight)
    Point(
      Constants.screenWidth / 2 + (minSize * x / (z * Constants.zMult)).toInt,
      Constants.screenHeight / 2 + (minSize * y / (z * Constants.zMult)).toInt
    )
  }
}

object Point3d {
  def fromPoint(point: Point, z: Double): Point3d = Point3d(point.x, point.y, z)
}
