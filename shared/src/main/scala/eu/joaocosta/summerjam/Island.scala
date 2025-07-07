package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.summerjam.engine.*

import eu.joaocosta.minart.graphics.Color

final case class Island(
    model: Vector[Polygon],
    subgoals: Vector[Goal],
    goal: Goal,
    afternoon: Boolean = false
) {
  lazy val polygons =
    goal.polygons(Colors.white, Colors.redLight) ++
      (if (!afternoon) model else Island.withAfternoonColors(model))

  lazy val specialPolygons =
    subgoals.reverse.zipWithIndex.flatMap((g, idx) =>
      g.polygons(
        Colors.white.copy(a = 128).premultiplyAlpha,
        if (idx == subgoals.size - 1) Colors.gold.copy(a = 200).premultiplyAlpha
        else Colors.silver.copy(a = 200).premultiplyAlpha
      )
    )
}

object Island {

  def withAfternoonColors(model: Vector[Polygon]): Vector[Polygon] =
    model.collect { case tri: Polygon.Triangle3d =>
      tri.copy(color = tri.color * Colors.afternoonDark)
    }

  lazy val islands = Resources.levels.map(_.toIsland)
}
