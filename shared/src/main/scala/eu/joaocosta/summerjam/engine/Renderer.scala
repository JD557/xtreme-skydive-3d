package eu.joaocosta.summerjam.engine

import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.geometry.*

object Renderer {
  def render(buffer: MutableSurface, polys: IterableOnce[Polygon]): Unit = {
    val depthBuffer =
      Array.fill(buffer.height)(
        Array.fill(buffer.width)(Double.PositiveInfinity)
      )

    val screenArea =
      AxisAlignedBoundingBox(0, 0, buffer.width, buffer.height)

    polys.iterator
      .filter { poly =>
        poly.project.knownFace != Some(Shape.Face.Back) &&
        poly.depth >= Constants.nearPlane &&
        screenArea.collides(poly.project.aabb)
      }
      .foreach { poly =>
        val shape = poly.project
        lazy val maxDet = ConvexPolygon.determinant(
          shape.vertices(0),
          shape.vertices(1),
          shape.vertices(2)
        )
        shape.aabb.intersect(screenArea).foreach { (x, y) =>
          if (shape.contains(x, y)) {
            val (z, light) = poly match {
              case Polygon.Triangle3d(a, b, c, _, la, lb, lc) =>
                var _z = 0.0
                var _light = 0.25
                shape.foreachDeterminant(x, y) { (v, d) =>
                  val ratio = if (maxDet == 0) 1.0 else d / maxDet
                  _z += (v match {
                    case 0 => c.z * ratio
                    case 1 => a.z * ratio
                    case 2 => b.z * ratio
                    case _ => 0
                  })
                  _light += (v match {
                    case 0 => lc * ratio
                    case 1 => la * ratio
                    case 2 => lb * ratio
                    case _ => 0
                  })
                }
                (_z, Math.min(_light, 1.0))
              case _ => (poly.depth, poly.light)
            }
            if (depthBuffer(y)(x) > z) {
              depthBuffer(y)(x) = z
              val color = (Color.grayscale(
                25 + math.max(0, (light * 230).toInt)
              ) * poly.color)
              buffer.putPixel(x, y, color)
            }
          }
        }
      }
  }

  def renderSpecial(
      buffer: MutableSurface,
      polys: IterableOnce[Polygon]
  ): Unit = {
    val screenArea =
      AxisAlignedBoundingBox(0, 0, buffer.width, buffer.height)

    polys.iterator
      .filter { poly =>
        poly.project.knownFace != Some(
          Shape.Face.Back
        ) && poly.depth >= Constants.nearPlane
      }
      .foreach { poly =>
        val shape = poly.project
        buffer.rasterizeShape(
          shape,
          Some(poly.color),
          None,
          BlendMode.AlphaAdd
        )(0, 0)
      }
  }
}
