package eu.joaocosta.summerjam

import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.geometry.*
import eu.joaocosta.minart.input.*
import eu.joaocosta.minart.input.KeyboardInput.Key

object RenderLogic {
  var frame: Int = 0

  val sea =
    RamSurface.tabulate(Constants.screenWidth, Constants.screenHeight)((x, y) =>
      val alpha = Color.grayscale(255 * y / Constants.screenHeight)
      Colors.seaDark * alpha + Colors.seaLight * alpha.invert
    )

  def renderIsland(
      island: Island,
      position: Point,
      rotation: Double,
      height: Double,
      surface: MutableSurface
  ): Unit = {
    surface.blit(sea)(0, 0)
    val transformation = Matrix
      .rotation(rotation)
      .multiply(
        Matrix.translation(
          -position.x,
          -position.y
        )
      )
    val polys = Helpers
      .transformModel(island.polygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, height)
        )
      )
    Renderer.render(surface, polys)

    val specialPolys = Helpers
      .transformModel(island.specialPolygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, height)
        )
      )
    Renderer.renderSpecial(surface, specialPolys)
  }

  // State logic

  def renderIntroState(
      state: IntroState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {

    def renderText() = {
      val text = "A JD557 game for"
      val aabb = Resources.bizcat.textBoundingBox(text, 0, 0)
      Resources.bizcat.renderText(
        surface,
        text,
        (Constants.screenWidth - aabb.width) / 2,
        32,
        Color(255, 255, 255)
      )
    }

    surface.blit(Resources.jamLogo)(
      (Constants.screenWidth - Resources.jamLogo.width) / 2,
      (Constants.screenHeight - Resources.jamLogo.height) / 2
    )
    if (state.t > 0.3) {
      surface.blit(
        Resources.jamLogoWa
          .columnScroll(dy => (8 * math.sin(dy / 8.0 + state.t * 16)).toInt)
          .flipV
          .scale(1.0, 0.5)
          .map(_ * Color.grayscale((255 * (state.t - 0.3)).toInt))
      )(
        (Constants.screenWidth - Resources.jamLogo.width) / 2,
        Resources.jamLogo.height + (Constants.screenHeight - Resources.jamLogo.height) / 2
      )
    }

    renderText()

    val lightFactor =
      Color.grayscale(
        Math.max(0, Math.min(255, (512 * math.sin(state.t * Math.PI)).toInt))
      )
    surface.modify(_.map(_ * lightFactor))

    if (state.t < 0.5) renderText()
  }

  def renderMenuState(
      state: MenuState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {
    def renderText() = {
      val text = "Press Enter to Start"
      val aabb = Resources.bizcat.textBoundingBox(text, 0, 0)
      Resources.bizcat.renderText(
        surface,
        text,
        (Constants.screenWidth - aabb.width) / 2,
        Constants.screenHeight - 32,
        Color(255, 255, 255)
      )
    }

    val logo =
      if (state.t < 1.5) {
        Resources.logo.view.map(c =>
          if (c != Color(0, 0, 0)) Color.grayscale((state.t / 2 * 255).toInt)
          else c
        )
      } else Resources.logo

    surface.blit(logo)(
      (Constants.screenWidth - Resources.logo.width) / 2,
      (Constants.screenHeight - Resources.logo.height) / 2
    )

    if (((frame / 16) & 1) == 0) renderText()

    if (state.t < 4) {
      val lightFactor = Color.grayscale(
        Math.max(
          0,
          Math.min(255, (255 * math.sin((state.t - 0.5) * Math.PI / 2)).toInt)
        )
      )
      surface.modify(_.map(_ + lightFactor))
    }
  }

  def renderLevelIntroState(
      state: LevelIntroState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {
    renderIsland(
      state.island,
      state.island.goal.point,
      state.height,
      state.height,
      surface
    )
    val lightFactor =
      Color.grayscale(
        Math.min(255, (255 * state.height / GameConstants.startHeight).toInt)
      )
    surface.modify(_.map(_ + lightFactor))
  }

  def renderGameState(
      state: GameState,
      input: KeyboardInput,
      surface: MutableSurface,
      showHud: Boolean = true
  ): Unit = {
    val transition = Math.max(0.0, state.height - GameConstants.startHeight + 1)

    renderIsland(
      state.island,
      state.position,
      state.rotation,
      state.height,
      surface
    )

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

    if (showHud) {
      surface.fillRegion(
        AxisAlignedBoundingBox(0, 0, Constants.screenWidth, 40),
        Color(0, 0, 0)
      )
      Resources.bizcat.renderText(surface, "Score:", 4, 4, Color(255, 255, 255))
      Resources.bizcat.renderText(
        surface,
        "%06d".format(state.score),
        4,
        20,
        Color(255, 255, 255)
      )

      Resources.bizcat.renderText(
        surface,
        "Height:",
        128,
        4,
        Color(255, 255, 255)
      )
      Resources.bizcat.renderText(
        surface,
        "%06.2f".format(state.height * 1000).replace(",", "."),
        128,
        20,
        Color(255, 255, 255)
      )
    }

    if (transition > 0) {
      val lightFactor = Color.grayscale((255 * transition).toInt)
      surface.modify(_.map(_ + lightFactor))
    }
  }

  def renderGameOverState(
      state: GameOverState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {
    val transition = Math.min(1.0, state.t)

    val lastGameSurface =
      RamSurface(surface.width, surface.height, Color(0, 0, 0))
    renderGameState(
      state.lastState,
      KeyboardInput.empty,
      lastGameSurface,
      showHud = false
    )
    lastGameSurface.modify(
      _.flatMap((color) =>
        (x, y) =>
          if (
            x < 5 || x > lastGameSurface.width - 5 ||
            y < 5 || y > lastGameSurface.height - 5
          ) Color(0, 0, 0)
          else if (
            x < 10 || x > lastGameSurface.width - 10 ||
            y < 10 || y > lastGameSurface.height - 10
          ) Color(255, 255, 255)
          else Color.grayscale((color.r + color.g + color.b) / 3)
      )
    )

    surface.blitPlane(
      lastGameSurface.view.clamped
        .scale(1.0 - 0.4 * transition)
        .rotate(-0.5 * transition)
    )(Constants.screenWidth / 4, Constants.screenHeight / 2)

    Resources.bizcat.renderText(surface, "Score:", 4, 4, Color(255, 255, 255))
    Resources.bizcat.renderText(
      surface,
      "%06d".format(state.lastState.score),
      4,
      20,
      Color(255, 255, 255)
    )

    if (transition < 1.0) {
      val lightFactor = Color.grayscale((255 * (1 - transition)).toInt)
      surface.modify(_.map(_ + lightFactor))
    }
  }
}
