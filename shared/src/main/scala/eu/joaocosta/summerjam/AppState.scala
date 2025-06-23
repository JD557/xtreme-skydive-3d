package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.Point

sealed trait AppState

final case class LevelIntroState(
    island: Island,
    height: Double
) extends AppState {
  def rise = copy(height = height + GameConstants.levelIntroSpeed)

  def initialGameState = GameState(
    island = island,
    position = island.goal,
    parachute = false,
    rotation = 0.0,
    height = GameConstants.startHeight
  )
}

final case class GameState(
    island: Island,
    position: Point,
    parachute: Boolean,
    rotation: Double,
    height: Double
) extends AppState {

  val moveSpeed = if (parachute) 0.0025 else 0.01
  val fallSpeed = if (parachute) 0.0025 else 0.01

  def rotateLeft = copy(rotation = rotation + 0.05)
  def rotateRight = copy(rotation = rotation - 0.05)
  def move = copy(position =
    Point(
      position.x - moveSpeed * math.sin(rotation),
      position.y - moveSpeed * math.cos(rotation)
    )
  )
  def fall = copy(height = height - fallSpeed)
  def openParachute = copy(parachute = true)
}
