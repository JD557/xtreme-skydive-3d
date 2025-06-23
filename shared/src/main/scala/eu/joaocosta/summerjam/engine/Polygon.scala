package eu.joaocosta.summerjam.engine

import eu.joaocosta.minart.graphics.Color
import eu.joaocosta.minart.geometry.ConvexPolygon

sealed trait Polygon {
  def project: ConvexPolygon
  def depth: Double
  def light: Double
  def transform(f: Point3d => Point3d): Polygon
  val color: Color
}

object Polygon {
  final case class Triangle3d(
      a: Point3d,
      b: Point3d,
      c: Point3d,
      color: Color,
      lightA: Double = 1.0,
      lightB: Double = 1.0,
      lightC: Double = 1.0
  ) extends Polygon {
    lazy val project: ConvexPolygon = ConvexPolygon(
      Vector(a.project, b.project, c.project)
    )
    lazy val depth: Double = (a.z + b.z + c.z) / 3
    lazy val light: Double = (lightA + lightB + lightC) / 3
    def transform(f: Point3d => Point3d) =
      copy(f(a), f(b), f(c))
  }

  final case class Quad3d(
      a: Point3d,
      b: Point3d,
      c: Point3d,
      d: Point3d,
      color: Color,
      lightA: Double = 1.0,
      lightB: Double = 1.0,
      lightC: Double = 1.0,
      lightD: Double = 1.0
  ) extends Polygon {
    lazy val project: ConvexPolygon = ConvexPolygon(
      Vector(a.project, b.project, c.project, d.project)
    )
    lazy val depth: Double = (a.z + b.z + c.z + d.z) / 4
    lazy val light: Double = (lightA + lightB + lightC + lightD) / 4
    def transform(f: Point3d => Point3d) =
      copy(f(a), f(b), f(c), f(d))
  }
}
