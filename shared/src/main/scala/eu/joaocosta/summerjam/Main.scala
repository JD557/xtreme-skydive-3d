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

  var scanLines = true

  def toggleFullScreen(canvas: Canvas): Unit = {
    if (canvas.canvasSettings.fullScreen) canvas.changeSettings(canvasSettings)
    else canvas.changeSettings(fullScreenSettings)
  }

  def toggleScanlines() = scanLines = !scanLines

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

  val surface = new RamSurface(
    Constants.screenWidth,
    Constants.screenHeight,
    Color(0, 0, 0)
  )

  def main(args: Array[String]): Unit = {
    val initialCanvasSettings =
      if (args.contains("--fullscreen")) fullScreenSettings else canvasSettings
    if (args.contains("--no-scanlines")) toggleScanlines()
    AppLoop
      .statefulAppLoop[AppState](
        { (state: AppState) => system =>
          {
            val dt = (System.currentTimeMillis() - lastT) / 1000.0
            lastT = System.currentTimeMillis()
            import system.*
            //frameCounter()
            RenderLogic.frame += 1
            val input = canvas.getKeyboardInput()
            val quit = if (input.isDown(Key.Alt)) {
              if (input.keysPressed(Key.Enter)) toggleFullScreen(canvas)
              if (input.keysPressed(Key.S)) toggleScanlines()

              Platform() != Platform.JS && input.keysPressed(Key.Q)
            } else false
            canvas.clear()

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
                RenderLogic
                  .renderMenuState(m, input, Platform() != Platform.JS, surface)
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
              case Quit =>
                Quit
            }
            // Optimized scanline rendering
            val dimmingFactor =
              if (scanLines) Color.grayscale(200) else Color.grayscale(255)
            var y = 0
            while (y < surface.height) {
              var x = 0
              while (x < surface.width) {
                val c1 = surface.unsafeGetPixel(x, y)
                val c2 = c1 * dimmingFactor
                canvas.unsafePutPixel(2 * x, 2 * y, c1)
                canvas.unsafePutPixel(2 * x + 1, 2 * y, c1)
                canvas.unsafePutPixel(2 * x, 2 * y + 1, c2)
                canvas.unsafePutPixel(2 * x + 1, 2 * y + 1, c2)
                x = x + 1
              }
              y = y + 1
            }

            canvas.redraw()
            if (quit) Quit else newState
          }
        },
        terminateWhen = (state: AppState) => state == Quit
      )
      .configure(
        (initialCanvasSettings, AudioPlayer.Settings()),
        LoopFrequency.fromHz(Constants.fps),
        StateTransitions.initialState
      )
      .run()
  }
}
