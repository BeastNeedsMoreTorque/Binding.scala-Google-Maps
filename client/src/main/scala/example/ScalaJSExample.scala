package example

import com.thoughtworks.binding.{Binding, dom}
import google.maps.LatLng
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{document, window}

import scala.language.implicitConversions
import scala.scalajs.js

object ScalaJSExample extends js.JSApp {

  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???

  def main(): Unit = {

    dom.render(document.getElementById("map-control"), render)

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

  @dom lazy val render: Binding[HTMLElement] = {
    <div>
      <input class="prompt" type="text" placeholder="Address..." />
      <button class="ui primary button">
        Search Address
      </button>
    </div>
  }

}
