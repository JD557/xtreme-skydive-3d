package eu.joaocosta.summerjam

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.image.*
import eu.joaocosta.minart.runtime.Resource
import eu.joaocosta.summerjam.engine.*
import eu.joaocosta.minart.audio.sound.Sound

object Resources {
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

  lazy val background = Image
    .loadBmpImage(Resource("assets/background.bmp"))
    .get

  lazy val diver =
    SpriteSheet(Image.loadBmpImage(Resource("assets/diver.bmp")).get, 32, 32)
  lazy val parachute = Image.loadBmpImage(Resource("assets/parachute.bmp")).get

  lazy val ranks =
    SpriteSheet(
      Image
        .loadBmpImage(Resource("assets/ranks.bmp"))
        .get,
      64,
      64
    )

  lazy val island1 = 
    ObjLoader.loadObj(Resource("assets/island1.obj"), Some(Resource("assets/island1.mtl")))
  lazy val island2 = 
    ObjLoader.loadObj(Resource("assets/island2.obj"), Some(Resource("assets/island2.mtl")))
  lazy val island3 = 
    ObjLoader.loadObj(Resource("assets/island3.obj"), Some(Resource("assets/island3.mtl")))
    

  lazy val startupSound = Sound.loadWavClip(Resource("assets/startup.wav")).get
  lazy val introMusic = Sound.loadWavClip(Resource("assets/menu.wav")).get
  lazy val ingameMusic = Sound.loadWavClip(Resource("assets/ingame.wav")).get
  lazy val shutterSound = Sound.loadWavClip(Resource("assets/shutter.wav")).get
  lazy val scoreSound = Sound.loadWavClip(Resource("assets/score.wav")).get

  lazy val levels = Level.loadLevels(Resource("assets/levels.json")).get

  val allResources: List[() => Any] = List(
    () => bizcat,
    () => jamLogo,
    () => jamLogoWa,
    () => logo,
    () => background,
    () => diver,
    () => parachute,
    () => ranks,
    () => island1,
    () => island2,
    () => island3,
    () => startupSound,
    () => introMusic,
    () => ingameMusic,
    () => shutterSound,
    () => scoreSound,
    () => levels
  )
}
