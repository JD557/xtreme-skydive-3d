package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.Point

sealed trait AppState

final case class IntroState(t: Double) extends AppState

final case class MenuState(t: Double) extends AppState

final case class LevelIntroState(
    level: Int,
    height: Double,
    currentScore: Int
) extends AppState {

  def island = Island.islands(level)

  def rise = copy(height = height + GameConstants.levelIntroSpeed)

  def initialGameState = GameState(
    level = level,
    island = island,
    position = island.goal.point,
    parachute = false,
    rotation = 0.0,
    height = GameConstants.startHeight,
    score = currentScore
  )
}

final case class GameState(
    level: Int,
    island: Island,
    position: Point,
    parachute: Boolean,
    rotation: Double,
    height: Double,
    score: Int
) extends AppState {

  val moveSpeed =
    if (parachute) GameConstants.parachuteMoveSpeed else GameConstants.moveSpeed
  val fallSpeed =
    if (parachute) GameConstants.parachuteFallSpeed else GameConstants.fallSpeed

  def rotateLeft = copy(rotation = rotation + GameConstants.rotateSpeed)
  def rotateRight = copy(rotation = rotation - GameConstants.rotateSpeed)
  def move = copy(position =
    Point(
      position.x - moveSpeed * math.sin(rotation),
      position.y - moveSpeed * math.cos(rotation)
    )
  )
  def fall = copy(height = height - fallSpeed)
  def openParachute = copy(parachute = true)
  def updateGoals = {
    lazy val (hitGoal, otherGoals) = island.subgoals.partition(
      _.isHit(position, -1 * height)
    )
    if (isDone && island.goal.isHit(position, island.goal.z)) {
      copy(
        score = score + island.goal.score
      )
    } else if (hitGoal.nonEmpty) {
      copy(
        island = island.copy(subgoals = otherGoals),
        score = score + hitGoal.map(_.score).sum
      )
    } else this
  }

  val isDone = height <= 0
}

final case class LevelResultState(t: Double, lastState: GameState)
    extends AppState {
  val success =
    lastState.island.goal.isHit(lastState.position, lastState.island.goal.z)
}

final case class GameOverState(t: Double, score: Int) extends AppState
