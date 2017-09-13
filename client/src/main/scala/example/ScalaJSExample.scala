package example

import com.thoughtworks.binding.Binding.{Constants, Var}
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

  private val possibleAddrs: Var[Seq[GeocoderResult]] = Var(Seq())

  @dom private lazy val render: Binding[HTMLElement] = {

    <div>
      <input id="searchInput" class="prompt" type="text" placeholder="Address..." oninput={event: Event =>
      val value: String = searchInput.value
      if (value.length > 2)
        possibleAddresses(value)}/>
      <button class="ui primary button" onclick={event: Event =>
        selectAddress()}>
        Search Address
      </button>
      <div>
        <ol>
          {Constants(possibleAddrs.bind.map(addr =>
          renderPosAddr(addr)): _*).map(_.bind)}
        </ol>
      </div>
    </div>
  }

  @dom private def renderPosAddr(addr: GeocoderResult): Binding[HTMLElement] = {
    <li>
      {addr.formatted_address}<button onclick={event: Event =>
      selectAddress(addr)}>select</button>
    </li>
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

  private def selectAddress() {
    val value = possibleAddrs.value
    if(value.nonEmpty)
      selectAddress(value.head)
    else
      window.alert("There is no Address for your input")
  }

  private def selectAddress(address: GeocoderResult) {
    gmap.setCenter(address.geometry.location)
    val marker = new google.maps.Marker(
      google.maps.MarkerOptions(map = gmap
        , position = address.geometry.location))
  }

  private def possibleAddresses(address: String) {

    val callback = (results: js.Array[GeocoderResult], status: GeocoderStatus) =>
      if (status == GeocoderStatus.OK) {
        possibleAddrs.value = results.to[Seq]
          .take(5)
      } else {
        window.alert("Geocode was not successful for the following reason: " + status)
      }

    new Geocoder().geocode(GeocoderRequest(address), callback)
  }


}
