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
        Math.min(255, (512 * math.sin(state.t * Math.PI)).toInt)
      )
    surface.modify(_.map(_ * lightFactor))

    if (state.t < 0.5) renderText()
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

    val specialPolys = Helpers
      .transformModel(state.island.specialPolygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, state.height)
        )
      )
    Renderer.renderSpecial(surface, specialPolys)
    val lightFactor =
      Color.grayscale(
        Math.min(255, (255 * state.height / GameConstants.startHeight).toInt)
      )
    surface.modify(_.map(_ + lightFactor))
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

    val specialPolys = Helpers
      .transformModel(state.island.specialPolygons)(point =>
        point.copy(
          x = transformation.applyX(point.x, point.y),
          y = transformation.applyY(point.x, point.y),
          z = point.z + Math.max(0, state.height)
        )
      )
    Renderer.renderSpecial(surface, specialPolys)

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
    Resources.bizcat.renderText(surface, "Score:", 4, 4, Color(255, 255, 255))
    Resources.bizcat.renderText(
      surface,
      "%06d".format(state.finalScore),
      4,
      20,
      Color(255, 255, 255)
    )
  }
}
