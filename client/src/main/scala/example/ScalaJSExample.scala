package example

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import google.maps._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{Event, document, window}

import scala.language.implicitConversions
import scala.scalajs.js

object ScalaJSExample extends js.JSApp {

  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???

  def main(): Unit = {

    dom.render(document.getElementById("map-control"), render)

    google.maps.event.addDomListener(window, "load", initialize)

  }

  @dom private lazy val render: Binding[HTMLElement] = {
    val search: Var[String] = Var("")

    <div>
      <input id="searchInput" class="prompt" type="text" placeholder="Address..." oninput={event: Event => search.value = searchInput.value}/>
      <button class="ui primary button" onclick={event: Event =>
        geocodeAddress(search.value)}>
        Search Address
      </button>
      <div>Your input is {search.bind}</div>
    </div>
  }

  private lazy val opts = google.maps.MapOptions(
    center = new LatLng(51.201203, -1.724370),
    zoom = 8,
    panControl = false,
    streetViewControl = false,
    mapTypeControl = false)

  private lazy val gmap = new google.maps.Map(document.getElementById("map-canvas"), opts)

  private lazy val initialize = js.Function {
    gmap // the map must be initialized in this function
    "" // this function needs a String as return type?!
  }

  private def geocodeAddress(address: String) {
    val geocoder = new Geocoder()
    val callback = (results: js.Array[GeocoderResult], status: GeocoderStatus) =>
      if (status == GeocoderStatus.OK) {
        gmap.setCenter(results(0).geometry.location)
        val marker = new google.maps.Marker(
          google.maps.MarkerOptions(map = gmap
            , position = results(0).geometry.location))
      } else {
        window.alert("Geocode was not successful for the following reason: " + status)
      }

    geocoder.geocode(GeocoderRequest(address), callback)
  }


}
