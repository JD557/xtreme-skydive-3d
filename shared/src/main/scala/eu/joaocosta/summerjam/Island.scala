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

  val islands = Vector(
    Island(
      model = Resources.island1,
      subgoals = Vector(
        Goal(-0.5, 0.5, -15.0, 0.20, true, 100),
        Goal(-0.5, -0.5, -10.0, 0.20, true, 100),
        Goal(0.5, 0.5, -5.0, 0.20, true, 100),
        Goal(0.5, 0.5, -2.5, 0.10, true, 100)
      ),
      goal = Goal(-0.5, 0.5, 0.20, 0.10, false, 1000)
    ),
    Island(
      model = Resources.island2,
      subgoals = Vector(
        Goal(0, -1, -15.0, 0.1, true, 100),
        Goal(0.75, -0.75, -12.5, 0.1, true, 100),
        Goal(1, 0, -10, 0.1, true, 100),
        Goal(0, 1, -7.5, 0.1, true, 100),
        Goal(-1, 0, -5, 0.1, true, 100)
      ),
      goal = Goal(0.0, 0.0, 0.0, 0.05, false, 1000)
    ),
    Island(
      model = Resources.island3,
      subgoals = Vector(
        Goal(0.25, 0.25, -15.0, 0.1, true, 100),
        Goal(-0.5, -0.5, -13, 0.1, true, 100),
        Goal(0, 0, -11.5, 0.1, true, 100),
        Goal(0.5, 0.5, -10, 0.1, true, 100),
        Goal(0, 0, -7.5, 0.1, true, 100),
        Goal(-1, -1, -5, 0.1, true, 100),
        Goal(-0.9, -0.9, -2.5, 0.1, true, 100)
      ),
      goal = Goal(0.70, 0.70, 0.1, 0.05, false, 1000)
    ),
    Island(
      model = Resources.island1,
      subgoals = Vector(
        Goal(0.0, 0.0, -15.0, 0.10, true, 100),
        Goal(0.3, -0.3, -13.5, 0.05, true, 100),
        Goal(0.0, 0.0, -12.0, 0.10, true, 100),
        Goal(0.3, 0.3, -10.5, 0.05, true, 100),
        Goal(0.0, 0.0, -9.0, 0.10, true, 100),
        Goal(-0.3, -0.3, -6.5, 0.05, true, 100),
        Goal(0.0, 0.0, -5.0, 0.10, true, 100),
        Goal(-0.3, 0.3, -3.5, 0.05, true, 100)
      ),
      goal = Goal(-0.75, 0.4, 0.20, 0.05, false, 1000),
      afternoon = true
    ),
    Island(
      model = Resources.island2,
      subgoals = Vector(
        Goal(-1, -0.8, -16.0, 0.1, true, 100),
        Goal(-0.5, -1, -15.0, 0.1, true, 100),
        Goal(0.0, -1, -14.0, 0.1, true, 100),
        Goal(0.5, -1, -13.0, 0.1, true, 100),
        Goal(1, -0.8, -12.0, 0.1, true, 100),
        Goal(1, 0.8, -9.0, 0.1, true, 100),
        Goal(0.0, 1, -7.5, 0.1, true, 100),
        Goal(-1, 0.8, -6.0, 0.1, true, 100),
        Goal(0.0, 0.0, -3.0, 0.1, true, 100),
        Goal(0.0, 0.0, -2.0, 0.075, true, 100),
        Goal(0.0, 0.0, -1.0, 0.05, true, 100)
      ),
      goal = Goal(0.5, 0.5, 0.25, 0.05, false, 1000),
      afternoon = true
    ),
    Island(
      model = Resources.island3,
      subgoals = Vector(
        Goal(0.0, 0.0, -16.0, 0.1, true, 100),
        Goal(0.0, 0.0, -15.0, 0.05, true, 100),
        Goal(0.5, 0.0, -14.0, 0.1, true, 100),
        Goal(0.6, 0.2, -13.25, 0.05, true, 100),
        Goal(0.7, 0.3, -12.5, 0.05, true, 100),
        Goal(0.8, 0.35, -11.75, 0.05, true, 100),
        Goal(0.9, 0.375, -11, 0.05, true, 100),
        Goal(1.0, 0.5, -10.0, 0.05, true, 100),
        Goal(1.0, 0.6, -9.5, 0.1, true, 100),
        Goal(1.0, 0.7, -9, 0.05, true, 100),
        Goal(1.0, 0.8, -8.5, 0.05, true, 100),
        Goal(0.5, 0.5, -7.0, 0.1, true, 100),
        Goal(0.5, 0.5, -6.0, 0.05, true, 100),
        Goal(0.5, 0.5, -5.0, 0.05, true, 100),
        Goal(-0.5, 0.2, -1.5, 0.1, true, 100)
      ),
      goal = Goal(-0.6, -0.2, 0.1, 0.025, false, 1000),
      afternoon = true
    )
  )
}
