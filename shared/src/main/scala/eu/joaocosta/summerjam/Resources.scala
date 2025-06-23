package eu.joaocosta.summerjam

import eu.joaocosta.minart.graphics.image.Image
import eu.joaocosta.minart.runtime.Resource
import eu.joaocosta.minart.backend.defaults.given
import eu.joaocosta.minart.graphics.image.SpriteSheet


object Resources {
  lazy val diver = SpriteSheet(Image.loadBmpImage(Resource("assets/diver.bmp")).get, 32, 32)
  lazy val parachute = Image.loadBmpImage(Resource("assets/parachute.bmp")).get
}
