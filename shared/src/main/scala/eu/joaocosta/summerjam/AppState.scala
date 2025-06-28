package eu.joaocosta.summerjam

import eu.joaocosta.minart.geometry.Point

sealed trait AppState

final case class IntroState(t: Double) extends AppState

final case class MenuState(t: Double) extends AppState

final case class LevelIntroState(
    level: Int,
    height: Double,
    totalScore: Int
) extends AppState {

  def island = Island.islands(level)

  def rise(dt: Double) =
    copy(height = height + GameConstants.levelIntroSpeed * dt)

  def initialGameState = GameState(
    level = level,
    island = island,
    position = island.goal.point,
    parachute = false,
    rotation = 0.0,
    height = GameConstants.startHeight,
    score = 0,
    totalScore = totalScore
  )
}

final case class GameState(
    level: Int,
    island: Island,
    position: Point,
    parachute: Boolean,
    rotation: Double,
    height: Double,
    score: Int,
    totalScore: Int
) extends AppState {

  def rotateLeft(dt: Double) =
    copy(rotation = rotation + GameConstants.rotateSpeed * dt)
  def rotateRight(dt: Double) =
    copy(rotation = rotation - GameConstants.rotateSpeed * dt)
  def move(dt: Double) = {
    val moveSpeed =
      if (parachute) GameConstants.parachuteMoveSpeed * dt
      else GameConstants.moveSpeed * dt
    copy(position =
      Point(
        position.x - moveSpeed * math.sin(rotation),
        position.y - moveSpeed * math.cos(rotation)
      )
    )
  }
  def fall(dt: Double) = {
    val fallSpeed =
      if (parachute) GameConstants.parachuteFallSpeed * dt
      else GameConstants.fallSpeed * dt
    copy(height = height - fallSpeed)
  }

  def openParachute = copy(parachute = true)
  def updateGoals = {
    lazy val (hitGoal, otherGoals) = island.subgoals.partition(
      _.isHit(position, -1 * height)
    )
    if (isDone && island.goal.isHit(position, island.goal.z)) {
      copy(
        score = score + island.goal.score,
        totalScore = totalScore + island.goal.score
      )
    } else if (hitGoal.nonEmpty) {
      copy(
        island = island.copy(subgoals = otherGoals),
        score = score + hitGoal.map(_.score).sum,
        totalScore = totalScore + hitGoal.map(_.score).sum
      )
    } else this
  }

  val isDone = height <= 0
}

final case class LevelResultState(t: Double, lastState: GameState)
    extends AppState {
  val success =
    lastState.island.goal.isHit(lastState.position, lastState.island.goal.z)

  val score = lastState.score
  val totalScore = lastState.totalScore

  def rank: Int = {
    val island = Island.islands(lastState.level)
    val maxScore = island.subgoals.map(_.score).sum + island.goal.score
    if (score == maxScore) 6 // S Rank
    else (6 * score / maxScore)
  }
}

final case class GameOverState(t: Double, totalScore: Int) extends AppState {
  def rank: Int = {
    val maxScore = Island.islands
      .map(island => island.subgoals.map(_.score).sum + island.goal.score)
      .sum
    if (totalScore == maxScore) 6 // S Rank
    else (6 * totalScore / maxScore)
  }
}
