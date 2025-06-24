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
      input: KeyboardInput
  ): AppState = {
    if (state.t >= 1.0) MenuState(0.0)
    else state.copy(t = state.t + 1.0 / (60 * 15)) // 15 seconds at 60FPS
  }

  def updateMenuState(
      state: MenuState,
      input: KeyboardInput
  ): AppState = {
    if (input.keysPressed.contains(Key.Enter))
      LevelIntroState(
        level = 0,
        height = 0.0,
        currentScore = 0
      )
    else state.copy(t = state.t + 1.0 / 60)
  }

  def updateLevelIntroState(
      state: LevelIntroState,
      input: KeyboardInput
  ): AppState = {
    if (state.height >= GameConstants.startHeight)
      state.initialGameState
    else state.rise
  }

  def updateGameState(state: GameState, input: KeyboardInput): AppState = {
    state
      .pipe(st =>
        if (st.height <= 1.0) st.openParachute
        else st
      )
      .pipe(st =>
        if (input.isDown(Key.Left)) st.rotateLeft
        else if (input.isDown(Key.Right)) st.rotateRight
        else st
      )
      .pipe(st =>
        if (input.isDown(Key.Up) || st.parachute) st.move
        else st
      )
      .pipe(_.fall)
      .pipe(_.updateGoals)
      .pipe(st =>
        if (st.isDone) LevelResultState(0.0, st)
        else st
      )
  }

  def updateLevelResultState(
      state: LevelResultState,
      input: KeyboardInput
  ): AppState = {
    if (input.keysPressed.contains(Key.Enter)) {
      val nextLevel = state.lastState.level + 1
      if (nextLevel < Island.islands.size)
        LevelIntroState(
          level = nextLevel,
          height = 0.0,
          currentScore = state.lastState.score
        )
      else MenuState(0)
    } else state.copy(t = state.t + 1.0 / 60)
  }
}
