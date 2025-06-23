package eu.joaocosta.summerjam

import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.image.*
import eu.joaocosta.minart.runtime.Resource
import eu.joaocosta.summerjam.engine.*


object Resources {
  lazy val diver = SpriteSheet(Image.loadBmpImage(Resource("assets/diver.bmp")).get, 32, 32)
  lazy val parachute = Image.loadBmpImage(Resource("assets/parachute.bmp")).get
  lazy val bizcat = BitmapFont(Image.loadBmpImage(Resource("assets/bizcat.bmp")).get, 8, 16)
}
