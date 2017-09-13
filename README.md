# Play Framework with Scala.js, Binding.scala
# Work in Progress!
See the general setup on the original: [Full-Stack-Scala-Starter](https://github.com/Algomancer/Full-Stack-Scala-Starter)
This project is inspired [Binding.scala with Semantic-UI](http://sadhen.com/blog/2017/01/02/binding-with-semantic.html) to get a step by step tutorial.

On top of the Full-Stack-Scala-Starter project you will get an integration with [Google Maps](https://developers.google.com/maps) and its [Scala JS implementation](https://github.com/coreyauger/scalajs-google-maps).

## Dependencies
[>> commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/7d17f430ef24eca972befa9ea78d3d72655c018b)

I upgraded to new versions:
- Scala: 2.12
- Play: 2.6
- Bindings: 11.0.0-M4

Verify the setup with `sbt run`: On `http://localhost:9000` you should get a working page.

## adding Google Maps
Next we add the ScalaJS facade for the Google Map API. We use this [Scala JS implementation](https://github.com/coreyauger/scalajs-google-maps).
Here the important steps from that project:
### Include google maps on your page
```html
 <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=API_KEY"></script>
```
This goes to the head section of `server/app/view/main.scala.html`.
### Build.sbt
Add the following dependency to your porject.

`resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"`

`"io.surfkit" %%% "scalajs-google-maps" % "0.0.3-SNAPSHOT",`

I had troubles with the resolvers. It only worked when I added the resolver to `USER_HOME/.sbt/repositories`.

### Add the map to the page
Add a div to `main.scala.html`: `<div id="map-canvas"></div>`

Add the style to `public/stylesheets/main.css` (otherwise you don't see the map):
```css
#map-canvas {
       width: 100%;
       height: 800px;
   }
   ```

And replace the `main` method from the ScalaJSExample:
```Scala
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
```
Now you should see the map on `localhost:9000`

### Add Bindings.scala
The dependency is already there, so no work there.
So first we add a textfield and a button:
```Scala
  @dom def render(): Binding[HTMLElement] = {
    <div>
      <input class="prompt" type="text" placeholder="Address..." />
      <button class="ui primary button">
        Search Address
      </button>
    </div>
  }
  ```

In Intellij you will get a compile exception even though you imported all needed dependencies. This is because the macro will transform the XML elements to `Binding[HTMLElement]`.
You can fix that by adding this line: `implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???`.

next we need to add this to the page. Add the following line to the `main` method:
`dom.render(document.getElementById("map-control"), render)`
And in the `index.scala.html` add `<div id="map-control"></div>` as first div.

Now check `localhost:9000` if everything works as expected.

### Putting everything together
We would like to search for an address and see it on the map.
So first let us <b>prepare the needed Google map</b> code.
To make the map available, provide it and its options as a variables:
```Scala
  private lazy val opts = google.maps.MapOptions(
    center = new LatLng(51.201203, -1.724370),
    zoom = 8,
    panControl = false,
    streetViewControl = false,
    mapTypeControl = false)

  private lazy val gmap = new google.maps.Map(document.getElementById("map-canvas"), opts)
```

Provide a function that:
  1) takes the address (String) from the input
  2) gets a GeocoderResult (Position) from the Google map API
  3) centers the the map to the position
  4) sets a marker to the position
```Scala
   private def geocodeAddress(address: String) { // 1
     val geocoder = new Geocoder()
     val callback = (results: js.Array[GeocoderResult], status: GeocoderStatus) =>
       if (status == GeocoderStatus.OK) {
         gmap.setCenter(results(0).geometry.location) // 3
         val marker = new google.maps.Marker(
           google.maps.MarkerOptions(map = gmap
             , position = results(0).geometry.location)) // 4
       } else {
         window.alert("Geocode was not successful for the following reason: " + status)
       }

     geocoder.geocode(GeocoderRequest(address), callback) // 2
   }
 ```
The initialize function is now as simple as:
```Scala
  private lazy val initialize = js.Function {
    gmap // the map must be initialized in this function
    "" // this function needs a String as return type?!
  }
```
<b>Extend the Bindings.scala code</b>
 - `oninput` sets the value of the 'search-Var' on each input character (`searchInput` is a compile exception on Intellij)
 - `onclick` calls the `geocodeAddress` function with the current 'search-Var' value
 - for demonstration only I added the 'search-Var' bind example that automatically displayes the search value
```Scala
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
```
Now the `main` function looks as simple as:
```Scala
  def main(): Unit = {
    dom.render(document.getElementById("map-control"), render)
    google.maps.event.addDomListener(window, "load", initialize)
  }
```
## Conclusion
It's quite interesting to see a stream based Framework (Binding.scala) next to the callback based API (Google maps).
 - The Binding.scala solution is really elegant.
 - I had, still have some problems that there are compile time exceptions shown by the IDE (Intellij). Some I could get rid of by adding implicit conversions.
 - The usage of scala XML to declare the HTML-DOM is really nice. You literally can copy your HTML code directly, just adding the dynamic parts.
    - However for parameters that expect other types than String, you need again implicit conversions.

## Improvements
Please let me know if there are things:
 - that could be done better
 - there are errors
 - you like to extend

Just create an issue on that repo.
