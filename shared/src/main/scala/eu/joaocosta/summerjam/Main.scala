package eu.joaocosta.summerjam

import scala.util.chaining.*
import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.geometry.*
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.runtime.*
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.input.KeyboardInput
import eu.joaocosta.minart.input.KeyboardInput.Key

object Main {
  val canvasSettings =
    Canvas.Settings(
      width = Constants.screenWidth * 2,
      height = Constants.screenHeight * 2
    )

  val sea =
    RamSurface.tabulate(Constants.screenWidth, Constants.screenHeight)((x, y) =>
      val alpha = Color.grayscale(255 * y / Constants.screenHeight)
      Colors.seaDark * alpha + Colors.seaLight * alpha.invert
    )

  val initialState = LevelIntroState(
    island = Island.basicIsland,
    height = 0.0
  )

  var frame: Int = 0

  def updateLevelIntroState(
      state: LevelIntroState,
      input: KeyboardInput
  ): AppState = {
    if (state.height >= GameConstants.startHeight)
      state.initialGameState
    else state.rise
  }

  def renderLevelIntroState(
      state: LevelIntroState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {
    surface.blit(sea)(0, 0)
    val transformation = Matrix
      .rotation(state.height)
      .multiply(
        Matrix.translation(
          -state.island.goal.x,
          -state.island.goal.y
        )
      )
    val polys = Helpers
      .transformModel(state.island.polygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, state.height)
        )
      )
    Renderer.render(surface, polys)
    val lightFactor =
      Color.grayscale(
        Math.min(255, (255 * state.height / GameConstants.startHeight).toInt)
      )
    surface.modify(_.map(_ + lightFactor))
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
  }

  def renderGameState(
      state: GameState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {
    val transition = Math.max(0.0, state.height - GameConstants.startHeight + 1)

    surface.blit(sea)(0, 0)
    val transformation = Matrix
      .rotation(state.rotation)
      .multiply(
        Matrix.translation(
          -state.position.x,
          -state.position.y
        )
      )
    val polys = Helpers
      .transformModel(state.island.polygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, state.height)
        )
      )
    Renderer.render(surface, polys)

    val diverSurface =
      if (state.parachute) Resources.parachute
      else {
        val moving = input.isDown(Key.Up)
        val sprite =
          Resources.diver.getSprite((frame / 8) % 2, if (moving) 1 else 0)
        if (transition > 0) {
          val darkFactor = Color.grayscale((255 * (1 - transition)).toInt)
          sprite.map(color =>
            if (color != Colors.mask) color * darkFactor
            else color
          )
        } else sprite
      }

    surface
      .blit(diverSurface, BlendMode.ColorMask(Colors.mask))(
        (surface.width - diverSurface.width) / 2,
        (surface.height - diverSurface.height) / 2
      )

    if (transition > 0) {
      val lightFactor = Color.grayscale((255 * transition).toInt)
      surface.modify(_.map(_ + lightFactor))
    }
  }

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulRenderLoop[AppState]((state: AppState) =>
        (canvas: Canvas) => {
          frame += 1
          val input = canvas.getKeyboardInput()
          canvas.clear()
          val surface = new RamSurface(
            Constants.screenWidth,
            Constants.screenHeight,
            Color(0, 0, 0)
          )
          val newState = state match {
            case i: LevelIntroState =>
              renderLevelIntroState(i, input, surface)
              updateLevelIntroState(i, input)
            case gs: GameState =>
              renderGameState(gs, input, surface)
              updateGameState(gs, input)
          }
          canvas.blit(
            surface.view
              .scale(2)
              .flatMap(color =>
                (x: Int, y: Int) =>
                  if ((y & 0x01) == 0) color
                  else color * Color.grayscale(200)
              )
          )(0, 0)
          canvas.redraw()
          newState
        }
      )
      .configure(canvasSettings, LoopFrequency.hz60, initialState)
      .run()
  }
}
