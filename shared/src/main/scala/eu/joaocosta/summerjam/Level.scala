package eu.joaocosta.summerjam

import upickle.default.*

import eu.joaocosta.summerjam.engine.Polygon
import eu.joaocosta.minart.runtime.Resource
import scala.util.Try

final case class Level(
    modelName: String,
    subgoals: Vector[Goal],
    goal: Goal,
    afternoon: Boolean = false
) derives ReadWriter {
  def model: Vector[Polygon] =
    modelName match {
      case "island1" => Resources.island1
      case "island2" => Resources.island2
      case "island3" => Resources.island3
      case _         => Vector.empty
    }

  def toIsland = Island(
    model,
    subgoals,
    goal,
    afternoon
  )
}

object Level {
  def loadLevels(resource: Resource): Try[Vector[Level]] = {
    resource.withInputStream(is => read[Vector[Level]](is))
  }
}
