package eu.joaocosta.summerjam.engine

object Constants {
  val screenWidth = 320
  val screenHeight = 240
  val fov = Math.PI / 2.0
  val zMult = Math.tan(fov / 2.0)
  val nearPlane = 0.01
  val fps = 60
}
