package eu.joaocosta.summerjam

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.runtime.*
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.audio.AudioPlayer

object Main {
  val canvasSettings =
    Canvas.Settings(
      width = Constants.screenWidth * 2,
      height = Constants.screenHeight * 2,
      scale = Some(2)
    )

  val frameCounter = {
    var frameNumber: Int = 0
    var timer = System.currentTimeMillis
    () => {
      frameNumber += 1
      if (frameNumber % 10 == 0) {
        val currTime = System.currentTimeMillis()
        val fps = 10.0 / ((currTime - timer) / 1000.0)
        println("FPS:" + fps)
        timer = System.currentTimeMillis()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulAppLoop[AppState]((state: AppState) =>
        system => {
          import system.*
          frameCounter()
          RenderLogic.frame += 1
          val input = canvas.getKeyboardInput()
          canvas.clear()
          val surface = new RamSurface(
            Constants.screenWidth,
            Constants.screenHeight,
            Color(0, 0, 0)
          )
          val newState = state match {
            case i: IntroState =>
              if (i.t <= 0) audioPlayer.playNow(Resources.startupSound, 0)
              RenderLogic.renderIntroState(i, input, surface)
              StateTransitions.updateIntroState(i, input)
            case i: LevelIntroState =>
              RenderLogic.renderLevelIntroState(i, input, surface)
              StateTransitions.updateLevelIntroState(i, input)
            case g: GameState =>
              RenderLogic.renderGameState(g, input, surface)
              StateTransitions.updateGameState(g, input)
            case go: GameOverState =>
              RenderLogic.renderGameOverState(go, input, surface)
              state
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
      .configure(
        (canvasSettings, AudioPlayer.Settings()),
        LoopFrequency.hz60,
        StateTransitions.initialState
      )
      .run()
  }
}
