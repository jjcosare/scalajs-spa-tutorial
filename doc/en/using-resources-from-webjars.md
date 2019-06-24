# Using resources from WebJars

[WebJars](http://www.webjars.org) are a wonderful way to simplify inclusion of external resources such as JavaScript libraries and CSS definitions into
your own project. Instead of downloading JS/CSS packages like Bootstrap and extracting them within your project (or referring to external CDN
served resources), you can just add a dependency to the appropriate WebJar and you're all set!

## WebJar JavaScript

Scala.js SBT plugin offers a [nice and convenient way](http://www.scala-js.org/doc/sbt/depending.html) for extracting JavaScript sources from various
WebJars and concatenating them into a single JavaScript file that you can then refer to in your `index.html`. In the tutorial project this means following
configuration in the `build.scala` file:

```scala
/** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
val jsDependencies = Def.setting(Seq(
  "org.webjars.npm" % "react" % v.react / "umd/react.development.js" minified "umd/react.production.min.js" commonJSName "React",
  "org.webjars.npm" % "react-dom" % v.react / "umd/react-dom.development.js" minified "umd/react-dom.production.min.js" dependsOn "umd/react.development.js" commonJSName "ReactDOM",
  "org.webjars.npm" % "react-dom" % v.react / "umd/react-dom-server.browser.development.js" minified "umd/react-dom-server.browser.production.min.js" dependsOn "umd/react-dom.development.js" commonJSName "ReactDOMServer",
  "org.webjars.npm" % "jquery" % v.jQuery / "dist/jquery.js" minified "jquery.min.js",
  "org.webjars.npm" % "bootstrap" % v.bootstrap / "bootstrap.js" minified "bootstrap.min.js" dependsOn "dist/jquery.js",
  "org.webjars.bower" % "chartjs" % v.chartjs / "Chart.js" minified "Chart.min.js",
  "org.webjars" % "log4javascript" % v.log4js / "js/log4javascript_uncompressed.js" minified "js/log4javascript.js"
))
```

This will produce a file named `client-jsdeps.js` containing all those JavaScript files combined. In production build, a minimized version of each
JavaScript file is selected.

## WebJar CSS/LESS

For extracting CSS files from WebJars you could use the method described below, but there is bit more convenient method that gives you [SASS](https://sass-lang.com/)
processing as a bonus. First we'll need to add the [sbt-sassify](https://github.com/irundaia/sbt-sassify) plugin into our `plugins.sbt`

```scala
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.13")
```

The server project automatically enables the `sbt-web` and `sbt-sassify` plugins because it uses the `PlayScala` plugin.

We'll be storing SASS files under `src/main/assets/stylesheets` to keep them separated from directly copied resources.

```scala
SassKeys.cssStyle in Assets := Minified,
```
This tells the LESS compiler to minify the produced CSS.

Next step is to create a `main.less` (yes, it has to be named exactly that) with references to CSS/LESS files inside the WebJars.

```css
@import "lib/bootstrap/scss/bootstrap.scss";
@import "lib/fontawesome/scss/font-awesome.scss";
```

In this case we just import Bootstrap and Font Awesome SASS files because all other CSS styles are defined using ScalaCSS. Depending on the WebJar, 
it may or may not contain SASS files in addition to the CSS file. With the SASS files you can easily 
[configure the library](https://getbootstrap.com/docs/4.0/getting-started/theming/) to your liking by defining CSS variables in your `main.scss` file.

```css
@import "lib/bootstrap/scss/bootstrap.scss";
@import "lib/fontawesome/scss/font-awesome.scss";
@brand-danger:  #00534f;
```

## WebJar resource files

Sometimes WebJars contain other useful resources, such as the font files for Font Awesome in our case. Just including the WebJar as a dependency will provide
us the extracted contents and it can be used directly.
