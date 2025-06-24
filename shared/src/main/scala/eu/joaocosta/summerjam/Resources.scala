package eu.joaocosta.summerjam

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.image.*
import eu.joaocosta.minart.runtime.Resource
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.audio.sound.Sound

object Resources {
  lazy val diver =
    SpriteSheet(Image.loadBmpImage(Resource("assets/diver.bmp")).get, 32, 32)
  lazy val parachute = Image.loadBmpImage(Resource("assets/parachute.bmp")).get
  lazy val bizcat =
    BitmapFont(Image.loadBmpImage(Resource("assets/bizcat.bmp")).get, 8, 16)
  lazy val jamLogo = Image.loadBmpImage(Resource("assets/summerjam.bmp")).get
  lazy val jamLogoWa = WrapAround(jamLogo)
  lazy val logo = Image
    .loadBmpImage(Resource("assets/logo.bmp"))
    .get
    .view
    .scale(0.5)
    .toRamSurface()

  val startupSound = Sound.loadWavClip(Resource("assets/startup.wav")).get
  val introMusic = Sound.loadWavClip(Resource("assets/menu.wav")).get
  val shutterSound = Sound.loadWavClip(Resource("assets/shutter.wav")).get
}
