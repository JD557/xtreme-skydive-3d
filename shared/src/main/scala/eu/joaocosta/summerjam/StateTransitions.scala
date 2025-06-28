package eu.joaocosta.summerjam

import scala.util.chaining.*
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.minart.input.KeyboardInput.Key

object StateTransitions {
  // val initialState = IntroState(0)
  val initialState = MenuState(0)

  // State transitions
  def updateIntroState(
      state: IntroState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    if (state.t >= 15.0) MenuState(0.0)
    else state.copy(t = state.t + dt)
  }

  def updateMenuState(
      state: MenuState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    if (input.keysPressed.contains(Key.Enter))
      LevelIntroState(
        level = GameConstants.initialLevel,
        height = 0.0,
        totalScore = 0
      )
    else state.copy(t = state.t + dt)
  }

  def updateLevelIntroState(
      state: LevelIntroState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    if (state.height >= GameConstants.startHeight)
      state.initialGameState
    else state.rise(dt)
  }

  def updateGameState(
      state: GameState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    state
      .pipe(st =>
        if (st.height <= 1.0) st.openParachute
        else st
      )
      .pipe(st =>
        if (input.isDown(Key.Left)) st.rotateLeft(dt)
        else if (input.isDown(Key.Right)) st.rotateRight(dt)
        else st
      )
      .pipe(st =>
        if (input.isDown(Key.Up) || st.parachute) st.move(dt)
        else st
      )
      .pipe(_.fall(dt))
      .pipe(_.updateGoals)
      .pipe(st =>
        if (st.isDone) LevelResultState(0.0, st)
        else st
      )
  }

  def updateLevelResultState(
      state: LevelResultState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    if (input.keysPressed.contains(Key.Enter) || state.t >= 5) {
      val nextLevel = state.lastState.level + 1
      if (nextLevel < Island.islands.size)
        LevelIntroState(
          level = nextLevel,
          height = 0.0,
          totalScore = state.lastState.totalScore
        )
      else GameOverState(0, state.lastState.totalScore)
    } else state.copy(t = state.t + 1.0 / Constants.fps)
  }

  def updateGameOverState(
      state: GameOverState,
      input: KeyboardInput,
      dt: Double
  ): AppState = {
    if (input.keysPressed.contains(Key.Enter)) {
      MenuState(0)
    } else state.copy(t = state.t + 1.0 / Constants.fps)
  }
}
