package example

import google.maps.LatLng
import org.scalajs.dom.{document, window}
import scala.scalajs.js

object ScalaJSExample extends js.JSApp {

  def main(): Unit = {
    val initialize = js.Function {
      val opts = google.maps.MapOptions(
        center = new LatLng(51.201203, -1.724370),
        zoom = 8,
        panControl = false,
        streetViewControl = false,
        mapTypeControl = false)
      new google.maps.Map(document.getElementById("map-canvas"), opts)
      "" // this function needs a String as return type?!
    }

    google.maps.event.addDomListener(window, "load", initialize)
  }

}
