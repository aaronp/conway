# Conway's Game of Life

An implementation of [Conway's game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) using Scala3.

You can [play with this implementation here](https://aaronp.github.io/conway)

# Implementation

The code is in [model.scala](src/main/scala/life/model.scala).

It makes liberal use of scala3 features such as `opaque types`.

This helps catch a lot of silly errors at compile time (e.g. confusing a Row with a Col, both of which are just Ints).

The `Board` itself is an opaque type as well, which models the board as a map of rows by their row index.
This was chosen as the active cells are typically quite sparse (that is, the alive cells vastly outnumber the dead cells).
This has some interesting implications:
 * the rule-checking significantly faster
 * the board doesn't have to be a 2x2 matrix, but can easily support differently-sized rows, or rows which are thousands of columns at no additional cost
 * it makes some of the boundary checking easier, and the code (hopefully) more easy to reason about

## Run Locally
Using [sbt](https://www.scala-sbt.org/):
```sbt fullOptJS``` and open [life.html](life.html)

or (if you're in a hurry):
```sbt fastOptJS``` and open [life-fastopt.html](life-fastopt.html)

## Building / Testing
This is a Scala3 ScalaJS project built using sbt:

#### Testing
The `model` is the ke part for this project. The JS UI is just considered a test harness (and actually could be moved to test scope).

Therefor the test coverage is perhaps sparse, but the UI provides a nice way to manually test this
```
sbt test
```

#### Dev Setup
You probably already have java, scala, sbt, etc, but otherwise just:
```
brew install sbt
```
Or whatever you fancy depending on your OS


## Related Resources
 * See [hands-on-scala](http://www.lihaoyi.com/hands-on-scala-js/)
 * [conwaylife.com](conwaylife.com)
 * Conways Models [https://conwaylife.com/patterns/hwss.cells](https://conwaylife.com/patterns/hwss.cells)