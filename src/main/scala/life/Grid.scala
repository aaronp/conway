package life

import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, MouseEvent, document, window}


class Grid private(context: CanvasRenderingContext2D, cellSize: Int, initialBoard : Board):
  private var down = false

  var board = initialBoard

  def plotGridLines(): Unit = {
    context.withColor("blue") {
      (0 to cols).foreach { c =>
        val x = c * cellSize
        context.line(Point(x, 0), Point(x, canvasHeight))
      }
      (0 to rows).foreach { r =>
        val y = r * cellSize
        context.line(Point(0, y), Point(canvasWidth, y))
      }
    }
  }

  def canvasWidth = context.canvas.width

  def canvasHeight = context.canvas.height

  lazy val cols: Int = canvasWidth / cellSize
  lazy val rows: Int = canvasHeight / cellSize

end Grid

object Grid:

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!")
  }

  def init(context: CanvasRenderingContext2D, cellSize: Int) =
    val grid = Grid(context, cellSize, Board())
    import context.canvas

    grid.plotGridLines()
    canvas.onmousedown = (e: MouseEvent) => {
      grid.down = true
    }
    canvas.onmouseup =
      (e: MouseEvent) => {
        grid.down = false
      }

    canvas.onmousemove = {
      (e: MouseEvent) =>
        val rect = canvas.getBoundingClientRect()
        if (grid.down) {
          context.withColor("red") {
            context.fillRect(
              e.clientX - rect.left,
              e.clientY - rect.top,
              10, 10
            )
          }
        }
    }

    val button = document.createElement("button")
    button.textContent = "Click me!"
    button.addEventListener(
      "click",
      { (e: MouseEvent) =>
        addClickedMessage()
      }
    )
    document.body.appendChild(button)

    grid
