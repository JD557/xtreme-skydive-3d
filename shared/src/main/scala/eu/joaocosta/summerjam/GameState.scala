package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.Point

final case class GameState(
    position: Point,
    parachute: Boolean,
    rotation: Double,
    height: Double
) {

  val moveSpeed = if (parachute) 0.005 else 0.01
  val fallSpeed = if (parachute) 0.005 else 0.01

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
