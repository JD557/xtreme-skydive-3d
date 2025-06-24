package eu.joaocosta.summerjam.engine

import scala.io.Source
import eu.joaocosta.minart.graphics.Color

object ObjLoader {

  def loadMtl(file: String): Map[String, Color] = {
    Source
      .fromFile(file)
      .getLines()
      .foldLeft((Map.empty[String, Color], "default")) {
        case ((acc, material), line) =>
          line match {
            case s"newmtl $id" => (acc, id)
            case s"Kd $r $g $b" =>
              val color = Color(
                (r.toDouble * 255).toInt,
                (g.toDouble * 255).toInt,
                (b.toDouble * 255).toInt
              )
              (acc.updated(material, color), material)
            case _ => (acc, material)
          }
      }
      ._1
  }

  def loadObj(file: String, mtlFile: Option[String] = None): Vector[Polygon] = {
    val materials =
      mtlFile
        .map(loadMtl)
        .getOrElse(Map.empty)
        .withDefaultValue(Color(255, 255, 255))

    Source
      .fromFile(file)
      .getLines()
      .foldLeft(
        (
          Vector.empty[Point3d],
          Vector.empty[Double],
          Vector.empty[Polygon],
          Color(255, 255, 255)
        )
      ) { case ((vertices, lights, faces, material), line) =>
        line match {
          case s"v $x $y $z" =>
            val point = Point3d(x.toDouble, y.toDouble, z.toDouble)
            (vertices :+ point, lights, faces, material)
          case s"vn $x $y $z" =>
            val cos45 = 0.7071
            (
              vertices,
              lights :+ (-1 * y.toDouble * cos45 - z.toDouble * cos45),
              faces,
              material
            )
          case s"f $rawFaces" =>
            val parsedFaces =
              rawFaces
                .split(" ")
                .map { case s"$v/$t/$n" =>
                  (
                    v.toInt - 1,
                    t.toIntOption.getOrElse(1) - 1,
                    n.toIntOption.getOrElse(1) - 1
                  )
                }
            val values = parsedFaces.map(_._1)
            val lightValues = parsedFaces.map(_._3).map(lights)
            val color = material
            val polygon =
              if (values.size == 3)
                Polygon.Triangle3d(
                  vertices(values(0)),
                  vertices(values(2)),
                  vertices(values(1)),
                  color,
                  lightValues(0),
                  lightValues(2),
                  lightValues(1)
                )
              else
                throw new Exception("Invalid polygon: " + values.toVector)
            (vertices, lights, faces :+ polygon, material)
          case s"usemtl $id" =>
            (vertices, lights, faces, materials(id))
          case _ =>
            (vertices, lights, faces, material)
        }
      }._3
  }

}
