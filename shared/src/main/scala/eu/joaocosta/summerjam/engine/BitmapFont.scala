package eu.joaocosta.summerjam.engine

import eu.joaocosta.minart.geometry.*
import eu.joaocosta.minart.graphics.*
import eu.joaocosta.minart.graphics.image.*
import eu.joaocosta.minart.runtime.*
import eu.joaocosta.minart.backend.defaults.given

case class BitmapFont(
    surface: Surface,
    width: Int,
    height: Int,
    fontFirstChar: Char = '\u0000'
) {
  private val spriteSheet = SpriteSheet(surface, width, height)
  def coloredChar(char: Char, color: Color): SurfaceView =
    spriteSheet.getSprite(char.toInt - fontFirstChar.toInt).map {
      case Color(255, 255, 255) => color
      case c                    => Color(255, 0, 255)
    }

  def renderText(
      target: MutableSurface,
      text: String,
      x: Int,
      y: Int,
      color: Color = Color(0, 0, 0)
  ): Unit =
    text.zipWithIndex.foreach { case (char, i) =>
      target.blit(
        coloredChar(char, color),
        BlendMode.ColorMask(Color(255, 0, 255))
      )(x + width * i, y)
    }

  def textBoundingBox(text: String, x: Int, y: Int): AxisAlignedBoundingBox = {
    AxisAlignedBoundingBox(
      x = x,
      y = y,
      width = text.size * width,
      height = height
    )
  }

}
