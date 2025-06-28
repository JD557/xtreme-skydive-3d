package eu.joaocosta.summerjam

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.runtime.*
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.audio.AudioPlayer
import eu.joaocosta.minart.input.KeyboardInput.Key

object Main {
  val canvasSettings =
    Canvas.Settings(
      width = Constants.screenWidth * 2,
      height = Constants.screenHeight * 2,
      scale = None,
      clearColor = Color(0, 0, 0),
      title = "Xtreme Skydive 3D"
    )

  val fullScreenSettings = canvasSettings.copy(fullScreen = true, scale = None)

  def toggleFullScreen(canvas: Canvas): Unit = {
    if (canvas.canvasSettings.fullScreen) canvas.changeSettings(canvasSettings)
    else canvas.changeSettings(fullScreenSettings)
  }

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

  var lastT = System.currentTimeMillis()

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulAppLoop[AppState]((state: AppState) =>
        system => {
          val dt = (System.currentTimeMillis() - lastT) / 1000.0
          lastT = System.currentTimeMillis()
          import system.*
          frameCounter()
          RenderLogic.frame += 1
          val input = canvas.getKeyboardInput()
          if (input.keysPressed(Key.F)) toggleFullScreen(canvas)
          canvas.clear()
          val surface = new RamSurface(
            Constants.screenWidth,
            Constants.screenHeight,
            Color(0, 0, 0)
          )

          val newState = state match {
            case l: LoadingState =>
              RenderLogic.renderLoadingState(l, surface)
              StateTransitions.updateLoadingState(l)
            case i: IntroState =>
              if (i.t <= 0) audioPlayer.playNow(Resources.startupSound, 0)
              RenderLogic.renderIntroState(i, input, surface)
              StateTransitions.updateIntroState(i, input, dt)
            case m: MenuState =>
              if (m.t <= 0) audioPlayer.playNow(Resources.introMusic, 0)
              RenderLogic.renderMenuState(m, input, surface)
              StateTransitions.updateMenuState(m, input, dt)
            case i: LevelIntroState =>
              RenderLogic.renderLevelIntroState(i, input, surface)
              StateTransitions.updateLevelIntroState(i, input, dt)
            case g: GameState =>
              if (g.height == GameConstants.startHeight)
                audioPlayer.playNow(Resources.ingameMusic, 0)
              RenderLogic.renderGameState(g, input, surface)
              StateTransitions.updateGameState(g, input, audioPlayer, dt)
            case lr: LevelResultState =>
              if (lr.t <= 0) audioPlayer.playNow(Resources.shutterSound, 1)
              RenderLogic.renderLevelResultState(lr, input, surface)
              StateTransitions.updateLevelResultState(lr, input, dt)
            case go: GameOverState =>
              RenderLogic.renderGameOverState(go, input, surface)
              StateTransitions.updateGameOverState(go, input, dt)
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
        LoopFrequency.fromHz(Constants.fps),
        StateTransitions.initialState
      )
      .run()
  }
}
