# Conway's Game of Life
![https://github.com/aaronp/conway/actions](https://github.com/aaronp/conway/actions/workflows/test.yml/badge.svg)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

An implementation of [Conway's game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) using Scala3.

You can play a live version of this code [here](https://aaronp.github.io/conway).

# Implementation

The main code is in [model.scala](src/main/scala/life/model.scala), where the test harness is under the [ui package](src/main/scala/life/ui).

The implementation makes liberal use of scala3 features - particularly `opaque types` and `extension methods`.

Opaque types help catch a number of silly errors at compile time (e.g. confusing a Row with a Col, both of which are just Ints), and really help in refactoring. I've found them very useful in most applications for dealing with e.g. "stringly-typed" data.

The `Board` itself is an opaque type as well, which models the board as a map of rows by their row index:
```
opaque type Row = Int
opaque type Col = Int
opaque type BoardRow = Set[Col]
opaque type Board = Map[Row, BoardRow]

case class Diff(toggledAlive :Set[(Row, Col)], toggledDead :Set[(Row, Col)])
```

This approach was chosen over something like a 2x2 matrix as the active cells are typically quite sparse (that is, the alive cells vastly outnumber the dead cells).

This has some interesting implications:
 * the rule-checking significantly faster
 * the board doesn't have to be a 2x2 matrix, but can easily support differently-sized rows, or rows which are thousands of columns at no additional cost
 * it makes some of the boundary checking easier, and the code (hopefully) more easy to reason about


The 'Board' extension provides the key functionality:
```
extension (board: Board)
  // provides a delta between this and another boards
  def diff(after : Board): Diff = ...
  // returns a new board with the game rules applied 
  def advance: Board = ...
  // turn this board back into ascii text (See Board.parse(ascii: String) to turn it back again)
  def render(deadChar: Char = '.', aliveChar: Char = 'o'): String = ...
```

The board companion object knows how to parse text:
```
object Board:
    def parse(text: String, aliveChar: Char): Board = ...
```

## Run Locally
Using [sbt](https://www.scala-sbt.org/):
```sbt fullOptJS``` and open [life.html](life.html)

or (if you're in a hurry):
```sbt fastOptJS``` and open [life-fastopt.html](life-fastopt.html)

## Building / Testing
This is a Scala3 ScalaJS project built using sbt:

```sbt test fastOptJS```

The `*.html` files in this repo can be used to test locally. My workflow is usually just:

```
# open the test html file in a browser
open -a ./life-fastopt.html

# watch the files/changes
sbt ~fastOptJS
```

## Deploying to GitHub
When ready, I just do a manual test and copy into the `./docs` directory for the github pages: 
```
sbt clean test fullOptJS
cp ./target/scala-3.2.0/life-opt/main.js ./docs/main.js
```

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
