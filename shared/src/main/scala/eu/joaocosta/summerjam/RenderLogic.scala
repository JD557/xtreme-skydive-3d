package eu.joaocosta.summerjam

import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.geometry.*
import eu.joaocosta.minart.input.*
import eu.joaocosta.minart.input.KeyboardInput.Key

object RenderLogic {
  var frame: Int = 0
  val frameRatio: Int = (60 / Constants.fps).toInt
  def blinkFrame = ((frameRatio * frame / 8) & 1)

  val sea =
    RamSurface.tabulate(Constants.screenWidth, Constants.screenHeight)((x, y) =>
      val alpha = Color.grayscale(255 * y / Constants.screenHeight)
      Colors.seaDark * alpha + Colors.seaLight * alpha.invert
    )

  val afternoonSea =
    RamSurface.tabulate(Constants.screenWidth, Constants.screenHeight)((x, y) =>
      val alpha = Color.grayscale(255 * y / Constants.screenHeight)
      Colors.afternoonDark * alpha + Colors.afternoonLight * alpha.invert
    )

  def renderIsland(
      island: Island,
      position: Point,
      rotation: Double,
      height: Double,
      surface: MutableSurface
  ): Unit = {
    surface.blit(if (island.afternoon) afternoonSea else sea)(0, 0)
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

  def renderLoadingState(
      state: LoadingState,
      surface: MutableSurface
  ): Unit = {
    val progress =
      state.loaded.toDouble / (state.loaded + state.remainingResources.size)
    surface.fillRegion(
      10,
      surface.height - 20,
      surface.width - 20,
      10,
      Color(255, 255, 255)
    )
    surface.fillRegion(
      10 + 2,
      surface.height - 20 + 2,
      surface.width - 20 - 4,
      10 - 4,
      Color(0, 0, 0)
    )
    surface.fillRegion(
      10 + 3,
      surface.height - 20 + 3,
      (progress * (surface.width - 20 - 6)).toInt,
      10 - 6,
      Color(255, 255, 255)
    )
  }

  def renderIntroState(
      state: IntroState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {

    def renderText() = {
      Resources.bizcat.renderTextCenteredX(
        surface,
        "A JD557 game for",
        Constants.screenWidth,
        32,
        Color(255, 255, 255)
      )
    }

    surface.blit(Resources.jamLogo)(
      (Constants.screenWidth - Resources.jamLogo.width) / 2,
      (Constants.screenHeight - Resources.jamLogo.height) / 2
    )
    if (state.t / 15 > 0.3) {
      surface.blit(
        Resources.jamLogoWa
          .columnScroll(dy =>
            (8 * math.sin(dy / 8.0 + state.t / 15 * 16)).toInt
          )
          .flipV
          .scale(1.0, 0.5)
          .map(_ * Color.grayscale((255 * (state.t / 15 - 0.3)).toInt))
      )(
        (Constants.screenWidth - Resources.jamLogo.width) / 2,
        Resources.jamLogo.height + (Constants.screenHeight - Resources.jamLogo.height) / 2
      )
    }

    renderText()

    val lightFactor =
      Color.grayscale(
        Math.max(
          0,
          Math.min(255, (512 * math.sin(state.t / 15 * Math.PI)).toInt)
        )
      )
    surface.modify(_.map(_ * lightFactor))

    if (state.t < 0.5 * 15) renderText()
  }

  def renderMenuState(
      state: MenuState,
      input: KeyboardInput,
      showQuit: Boolean,
      surface: MutableSurface
  ): Unit = {

    val logo =
      if (state.t < 1.5) {
        Resources.logo.view.map(c =>
          if (c != Color(0, 0, 0)) Color.grayscale((state.t / 2 * 255).toInt)
          else c
        )
      } else Resources.logo

    if (state.t > 1.5) {
      surface.blit(Resources.background)(0, 0)
      Resources.bizcat.renderTextCenteredX(
        surface,
        state.t.toInt % 3 match {
          case 0 => if (showQuit) "Alt + Q: Quit" else ""
          case 1 => "Alt + Enter: Fullscreen"
          case 2 => "Alt + S: Scanlines"
        },
        Constants.screenWidth,
        Constants.screenHeight - 24,
        Color(64, 64, 64)
      )
    } else { surface.fill(Color(0, 0, 0)) }
    surface.blit(logo, BlendMode.ColorMask(Color(0, 0, 0)))(
      (Constants.screenWidth - Resources.logo.width) / 2,
      (Constants.screenHeight - Resources.logo.height) / 2
    )

    if (blinkFrame == 0) {
      Resources.bizcat.renderTextCenteredX(
        surface,
        "Press Enter to Start",
        Constants.screenWidth,
        Constants.screenHeight - 48,
        Color(255, 255, 255)
      )
    }

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
    surface.modify(_.map(_ + (lightFactor * lightFactor)))
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
          Resources.diver.getSprite(blinkFrame, if (moving) 1 else 0)
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
      val hudAabb = AxisAlignedBoundingBox(
        0,
        Constants.screenHeight - 40,
        Constants.screenWidth,
        40
      )
      surface.fillRegion(
        hudAabb,
        Color(0, 0, 0)
      )
      Resources.bizcat.renderText(
        surface,
        "Score:",
        4,
        hudAabb.y + 4,
        Color(255, 255, 255)
      )
      Resources.bizcat.renderText(
        surface,
        "%06d".format(state.score),
        4,
        hudAabb.y + 4 + 16,
        Color(255, 255, 255)
      )

      Resources.bizcat.renderText(
        surface,
        "Height:",
        128,
        hudAabb.y + 4,
        Color(255, 255, 255)
      )
      Resources.bizcat.renderText(
        surface,
        "%06.2f".format(state.height * 100).replace(",", "."),
        128,
        hudAabb.y + 4 + 16,
        Color(255, 255, 255)
      )
    }

    if (transition > 0) {
      val lightFactor = Color.grayscale((255 * transition).toInt)
      surface.modify(_.map(_ + lightFactor))
    }
  }

  def renderLevelResultState(
      state: LevelResultState,
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
          ) Colors.mask
          else if (
            x < 10 || x > lastGameSurface.width - 10 ||
            y < 10 || y > lastGameSurface.height - 10
          ) Color(255, 255, 255)
          else color
      )
    )

    surface.blit(Resources.background)(0, 0)

    surface.blitPlane(
      lastGameSurface.view.clamped
        .scale(1.0 - 0.5 * transition)
        .rotate(-0.5 * transition),
      BlendMode.ColorMask(Colors.mask)
    )(Constants.screenWidth / 3, 2 * Constants.screenHeight / 5)

    Resources.bizcat.renderText(
      surface,
      if (state.successfulLanding && state.goodRank) "Awesome!"
      else if (state.successfulLanding) "Nice landing!"
      else "Fail!",
      4,
      4,
      Color(255, 255, 255)
    )

    Resources.bizcat.renderText(
      surface,
      s"Score: ${state.totalScore - state.score} + ${state.score} = ${state.totalScore}",
      4,
      32,
      Color(255, 255, 255)
    )

    Resources.bizcat.renderText(surface, "Rank:", 4, 48, Color(255, 255, 255))
    surface.blit(
      if (transition < 0.5)
        Resources.ranks
          .getSprite(state.rank)
          .view
          .scale(1 / (1 + 2 * transition))
      else
        Resources.ranks.getSprite(state.rank).view.scale(0.5)
    )(48, 48)

    if (transition < 1.0) {
      val lightFactor = Color.grayscale((255 * (1 - transition)).toInt)
      surface.modify(_.map(_ + lightFactor))
    } else if (state.lastState.level + 1 < Island.islands.size) {
      if (blinkFrame == 0)
        Resources.bizcat.renderTextCenteredX(
          surface,
          if (state.goodRank) "Get ready for the next level!"
          else "Better luck next time!",
          Constants.screenWidth,
          Constants.screenHeight - 32,
          Color(255, 255, 255)
        )
    }
  }

  def renderGameOverState(
      state: GameOverState,
      input: KeyboardInput,
      surface: MutableSurface
  ): Unit = {

    val transition = Math.min(1.0, state.t)

    surface.blit(Resources.background)(0, 0)

    Resources.bizcat.renderTextCenteredX(
      surface,
      "GAME OVER",
      Constants.screenWidth,
      32,
      Color(255, 255, 255)
    )
    Resources.bizcat.renderTextCenteredX(
      surface,
      s"Score: ${state.totalScore}",
      Constants.screenWidth,
      64,
      Color(255, 255, 255)
    )
    Resources.bizcat.renderTextCenteredX(
      surface,
      s"Rank:",
      Constants.screenWidth,
      96,
      Color(255, 255, 255)
    )
    val rankSprite =
      if (transition <= 1)
        Resources.ranks.getSprite(state.rank).scale(2 - transition)
      else
        Resources.ranks.getSprite(state.rank)

    surface
      .blit(rankSprite)(
        (Constants.screenWidth - rankSprite.width) / 2,
        (Constants.screenHeight - rankSprite.height) / 2
      )
  }
}
