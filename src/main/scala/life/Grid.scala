package life

import org.scalajs.dom
import org.scalajs.dom.*
import org.scalajs.dom.html.{Input, Label}
import scalatags.JsDom.all.*

class Grid private(context: CanvasRenderingContext2D, cellSize: Int):

  def coordsFor(x: Double, y: Double): (Row, Col) =
    val col = if x <= 0 then 0 else x / cellSize
    val row = if y <= 0 then 0 else y / cellSize
    row.toInt.asRow -> col.toInt.asCol

  def fillCell(row: Row, col: Col, color: String): Unit = {
    val y = Row.toInt(row) * cellSize
    val x = Col.toInt(col) * cellSize
    context.withColor(color) {
      context.fillRect(x, y, cellSize, cellSize)
    }
  }

  def plotDiff(diff: Diff, aliveColor: String, deadColor: String): Unit = {
    diff.toggledAlive.foreach {
      case (row, col) => fillCell(row, col, aliveColor)
    }
    diff.toggledDead.foreach {
      case (row, col) => fillCell(row, col, deadColor)
    }
  }

  def plotGridLines(): Unit =
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

  def canvasWidth = context.canvas.width

  def canvasHeight = context.canvas.height

  lazy val cols: Int = canvasWidth / cellSize
  lazy val rows: Int = canvasHeight / cellSize

end Grid

object Grid:

  private val AliveColor = "green"
  private val DeadColor = "yellow"

  def init(context: CanvasRenderingContext2D, cellSize: Int) =
    var board = Board()
    import context.canvas

    enum DragMode:
      case Draw
      case Erase
      case Toggle

    def scaleSize(dim: Double, scale : Double) =
      val scaled = dim * scale
      (scaled - (scaled % cellSize)).toInt

    canvas.width = scaleSize(window.innerWidth, 0.8)
    canvas.height = scaleSize(window.innerHeight, 0.6)

    val grid = Grid(context, cellSize)
    grid.plotGridLines()

    val gridInput = textarea(rows := 8, cols := 20).render

    def updateBoard(newBoard: Board) =
      val diff = board.diff(newBoard)
      board = newBoard
      grid.plotDiff(diff, AliveColor, DeadColor)
      gridInput.value = board.render('.', 'o')

    def toggleBoard(dragMode: DragMode, row: Row, col: Col) =
      dragMode match {
        case DragMode.Draw if !board.isAlive(row, col) => updateBoard(board.toggle(row, col))
        case DragMode.Draw => // no-op
        case DragMode.Erase if board.isAlive(row, col) => updateBoard(board.toggle(row, col))
        case DragMode.Erase => // no-op
        case DragMode.Toggle => updateBoard(board.toggle(row, col))
      }

    object state {
      var lastDragCoords: Option[(Row, Col)] = None // (row, col)
      var dragMode: DragMode = DragMode.Draw
      var playHandle = -1 // used for auto-play
    }

    def coords(e: MouseEvent) =
      grid.coordsFor(e.clientX - canvas.offsetLeft + window.scrollX, e.clientY - canvas.offsetTop + window.scrollY)

    canvas.onmousedown = (e: MouseEvent) => {
      val (row, col) = coords(e)
      toggleBoard(state.dragMode, row, col)
      state.lastDragCoords = Option(row -> col)
    }
    canvas.onmouseup =
      (e: MouseEvent) => {
        val (row, col) = coords(e)
        state.lastDragCoords = None
      }

    canvas.onmousemove = {
      (e: MouseEvent) =>
        val (row, col) = coords(e)
        state.lastDragCoords.foreach {
          case (`row`, `col`) => // no-op
          case _ =>
            state.lastDragCoords = Option(row -> col)
            toggleBoard(state.dragMode, row, col)
        }
    }

    // radios
    locally {
      def newRadio(labelText: String, radioValue: DragMode) = {
        val selected = radioValue == state.dragMode
        val inputButton = input(`type` := "radio", id := labelText.filter(_.isLetter) + "ID", value := radioValue.toString, name := "draw_type", checked := selected).render

        inputButton.onclick = (_) => {
          state.dragMode = radioValue
        }
        val inputLabel = label(`for` := inputButton.id)(labelText).render
        div(
          inputLabel,
          inputButton
        )
      }

      document.body.appendChild(
        div(
          h4("Mouse Click/Drag Behaviour:"),
          newRadio("Draw", DragMode.Draw),
          newRadio("Erase", DragMode.Erase),
          newRadio("Toggle", DragMode.Toggle)
        ).render
      )
    }

    val advanceButton = button()("Advance").render
    advanceButton.onclick = _ => updateBoard(board.advance)

    val loadButton = button()("Load").render
    loadButton.onclick = _ => {
      updateBoard(Board.parse(gridInput.value, 'o'))
    }

    val playButton = button()("Play").render

    def startAnimation() =
      playButton.innerText = "Pause"
      state.playHandle = window.setInterval(() => {
        updateBoard(board.advance)
      }, 250)

    def stopAnimation() =
      window.clearInterval(state.playHandle)
      state.playHandle = -1
      playButton.innerText = "Play"

    playButton.onclick = _ => {
      if state.playHandle == -1 then startAnimation() else stopAnimation()
    }

    document.body.appendChild(div(
      br(),
      advanceButton, span(width := 200)(" "), playButton,
      div(
        gridInput,
        br(),
        loadButton
      )
    ).render)

    // ooh! These are fun:
    // https://alvarotrigo.com/blog/toggle-switch-css/

    grid
