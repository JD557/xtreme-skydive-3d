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
      width = Constants.screenWidth,
      height = Constants.screenHeight,
      scale = Some(2)
    )

  val sea =
    RamSurface.tabulate(Constants.screenWidth, Constants.screenHeight)((x, y) =>
      val alpha = Color.grayscale(255 * y / Constants.screenHeight)
      Colors.seaDark * alpha + Colors.seaLight * alpha.invert
    )

  val model = Island.basicIsland.toPolygons
  val initialState =
    GameState(
      position = Island.basicIsland.goal,
      parachute = false,
      rotation = 0.0,
      height = 10.0
    )

  def updateState(state: GameState, input: KeyboardInput): GameState = {
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

  var frame: Int = 0

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulRenderLoop[GameState]((state: GameState) =>
        (canvas: Canvas) => {
          frame += 1
          val input = canvas.getKeyboardInput()
          canvas.clear()

          canvas.blit(sea)(0, 0)
          val transformation = Matrix
            .rotation(state.rotation)
            .multiply(
              Matrix.translation(
                -state.position.x,
                -state.position.y
              )
            )
          val polys = Helpers
            .transformModel(model)(point =>
              point.copy(
                x = transformation.applyX(point.x, point.y),
                y = transformation.applyY(point.x, point.y),
                z = point.z + Math.max(0, state.height)
              )
            )
          Renderer.render(canvas, polys)

          val diverSurface =
            if (state.parachute) Resources.parachute
            else {
              val moving = input.isDown(Key.Up)
              Resources.diver.getSprite((frame / 8) % 2, if (moving) 1 else 0)
            }

          canvas
            .blit(diverSurface, BlendMode.ColorMask(Colors.mask))(
              (canvas.width - diverSurface.width) / 2,
              (canvas.height - diverSurface.height) / 2
            )

          canvas.redraw()
          updateState(state, input)
        }
      )
      .configure(canvasSettings, LoopFrequency.hz60, initialState)
      .run()
  }
}
