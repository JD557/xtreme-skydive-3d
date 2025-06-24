package eu.joaocosta.summerjam

import scala.util.chaining.*
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.minart.input.KeyboardInput.Key

object StateTransitions {
  val initialState = LevelIntroState(
    island = Island.basicIsland,
    height = 0.0,
    currentScore = 0
  )

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
        if (input.keysPressed.contains(Key.Space)) st.openParachute
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
        if (st.isDone) GameOverState(st.score)
        else st
      )
  }
}
