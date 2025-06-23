package eu.joaocosta.summerjam.engine

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.minart.graphics.Color

object Helpers {
  def transformModel(model: Vector[Polygon])(f: Point3d => Point3d) =
    model.view.map(_.transform(f))

  def triangulate(points: Vector[Point]): Vector[ConvexPolygon] =
    if (points.size < 3) Vector()
    else triangulate(ConvexPolygon(points))

  def triangulate(polygon: ConvexPolygon): Vector[ConvexPolygon] =
    if (polygon.size <= 3) Vector(polygon)
    else {
      val centerX = polygon.vertices.map(_.x).sum / polygon.vertices.size
      val centerY = polygon.vertices.map(_.y).sum / polygon.vertices.size
      val centroid = Point(centerX, centerY)
      (polygon.vertices :+ polygon.vertices.head)
        .sliding(2)
        .map(_ :+ centroid)
        .map(ConvexPolygon.apply)
        .toVector
    }

  def toPolygons(
      shape: Vector[ConvexPolygon],
      color: Color,
      z: Double
  ): Vector[Polygon] =
    shape.map(cp =>
      val v = cp.vertices.map(point => Point3d.fromPoint(point, z))
      Polygon.Triangle3d(v(0), v(1), v(2), color)
    )
}
