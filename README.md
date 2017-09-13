# Play Framework with Scala.js, Binding.scala

See the general setup on the original: [Full-Stack-Scala-Starter](https://github.com/Algomancer/Full-Stack-Scala-Starter)
This project is inspired [Binding.scala with Semantic-UI](http://sadhen.com/blog/2017/01/02/binding-with-semantic.html) to get a step by step tutorial.

On top of the Full-Stack-Scala-Starter project you will get an integration with [Google Maps](https://developers.google.com/maps) and its [Scala JS implementation](https://github.com/coreyauger/scalajs-google-maps).
Here a screenshot of how the result will look like:
![result screenshot](https://user-images.githubusercontent.com/3437927/30396523-362dd11a-98ca-11e7-915f-a94dd32e8a4b.gif)
## Dependencies
[Git Commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/298ddbcc23d204b6753dd69fb74b5ace19d17f7c)

I upgraded to new versions:
- Scala: 2.12
- Play: 2.6
- Bindings: 11.0.0-M4

Verify the setup with `sbt run`: On `http://localhost:9000` you should get a working page.

## adding Google Maps
[Git Commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/4508a6ebc1a86e4ddc95bd6361840db931703471)

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

## Add Bindings.scala
[Git Commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/115dbff7f149b0eb9f4f262daa5a4ca884901fd6)

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

## Putting everything together
[Git Commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/1134210d1a0941cd4ac4459843449cb022b1906f)

We would like to search for an address and see it on the map.
So first let us **prepare the needed Google map** code.
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
1. takes the address (String) from the input
2. gets a GeocoderResult (Position) from the Google map API
3. centers the the map to the position
4. sets a marker to the position
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
**Extend the Bindings.scala code**
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
## Dive a bit deeper
[Git Commit](https://github.com/pme123/Binding.scala-Google-Maps/commit/34b9e095d1e9d9632f011e7a95889b9e05e50225)

Ok lets add a list that shows possible Addresses for our input, from where we can select one, or just take the first.
First we need another datatype where we can pass around the possible addresses:
```Scala
 private val possibleAddrs: Var[Seq[GeocoderResult]] = Var(Seq())
 ```
We need to redo our Address fetching function a bit:
```Scala
  private def possibleAddresses(address: String) {

    val callback = (results: js.Array[GeocoderResult], status: GeocoderStatus) =>
      if (status == GeocoderStatus.OK) {
        possibleAddrs.value = results.to[Seq].take(5)
      } else {
        window.alert("Geocode was not successful for the following reason: " + status)
      }

    new Geocoder().geocode(GeocoderRequest(address), callback)
  }
  ```
We provide two helper function that will display the Address on the map
```Scala
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
  ```
We adjust our render function:
```Scala
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
          {for (addr <- possibleAddrs.bind) yield
          <li>
            {addr.formatted_address}<button onclick={event: Event =>
            selectAddress(addr)}>select</button>
          </li>}
        </ol>
      </div>
    </div>
  }
```
Now it gets tricky. If you refresh `localhost:9000` you will get a Compile Exception: `'each' instructions must be inside a SDE block`.
Ok, that suggest to extract the `<li>` part:
```Scala
 ...
    <ol>
      {for (addr <- possibleAddrs.bind) yield
      renderPosAddr(addr: GeocoderResult).bind}
    </ol>
 ...

  @dom private def renderPosAddr(addr: GeocoderResult): Binding[HTMLElement] = {
    <li>
      {addr.formatted_address}<button onclick={event: Event =>
      selectAddress(addr)}>select</button>
    </li>
  }
 ```
That was not enough - still the same exception!
Now we need to do it like this (explained here: [Stackoverflow](https://stackoverflow.com/questions/42498968/when-i-use-binding-scala-i-got-the-error-each-instructions-must-be-inside-a-sd/42498969#42498969) )
```Scala
 ...
    <ol>
      {Constants(possibleAddrs.bind.map(addr =>
                renderPosAddr(addr)): _*).map(_.bind)}
    </ol>
 ...
 ```
Now everything compiles and we can search our Addresses!

## Conclusion
It's quite interesting to see a stream based Framework (Binding.scala) next to the callback based API (Google maps).
 - The Binding.scala solution is really elegant.
 - I had, still have some problems that there are compile time exceptions shown by the IDE (Intellij). Some I could get rid of by adding implicit conversions.
 - The usage of scala XML to declare the HTML-DOM is really nice. You literally can copy your HTML code directly, just adding the dynamic parts.
    - Only drawback: for parameters that expect other types than String, you need again implicit conversions.

## Improvements
Please let me know if there are things:
 - that could be done better
 - there are errors
 - you like to extend

Just create an issue on that repo.
