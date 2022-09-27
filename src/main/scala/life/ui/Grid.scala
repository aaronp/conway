package life.ui

import life.*
import org.scalajs.dom
import org.scalajs.dom.*

import scala.collection.immutable
// import scalatags.JsDom.all.*
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
      context.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2)
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

  val AliveColor = "green"
  val DeadColor = "white"

  def init(context: CanvasRenderingContext2D, cellSize: Int) =
    var board = Board()
    import context.canvas

    enum DragMode:
      case Draw
      case Erase
      case Toggle

    def scaleSize(dim: Double, scale: Double) =
      val scaled = dim * scale
      (scaled - (scaled % cellSize)).toInt

    canvas.width = scaleSize(window.innerWidth, 0.8)
    canvas.height = scaleSize(window.innerHeight, 0.6)

    val grid = Grid(context, cellSize)
    grid.plotGridLines()

    val gridInput = textarea(rows := 10, cols := 40).render

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
      var dragMode: DragMode = DragMode.Toggle
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
    val mouseDragBehaviour = {
      def newRadio(labelText: String, radioValue: DragMode) = {
        val selected = radioValue == state.dragMode
        val inputButton = input(`type` := "radio", style := "margin-right:20px", id := labelText.filter(_.isLetter) + "ID", value := radioValue.toString, name := "draw_type", checked := selected).render

        inputButton.onclick = _ => state.dragMode = radioValue

        span(
          label(`for` := inputButton.id)(labelText),
          inputButton
        )
      }

      div(
        span(style := "margin-right:20px")("Mouse Click/Drag Behaviour:"),
        newRadio("Toggle", DragMode.Toggle),
        newRadio("Draw", DragMode.Draw),
        newRadio("Erase", DragMode.Erase)
      )
    }

    val advanceButton = button()("Advance").render
    advanceButton.onclick = _ => updateBoard(board.advance)

    // the toLowerCase is because some conway examples use upper-case Os
    def loadBoardFromText(boardText: String = gridInput.value.toLowerCase) =
      updateBoard(Board.parse(boardText, 'o'))

    val loadButton = button()("Load").render
    loadButton.onclick = _ => loadBoardFromText()

    val playButton = button()("Play").render

    def startAnimation() =
      playButton.innerText = "Pause"
      state.playHandle = window.setInterval(() => {
        updateBoard(board.advance)
      }, 150)

    def stopAnimation() =
      window.clearInterval(state.playHandle)
      state.playHandle = -1
      playButton.innerText = "Play"

    playButton.onclick = _ => {
      if state.playHandle == -1 then startAnimation() else stopAnimation()
    }

    val conwaySelect = {
      val patternByName = ModelRefData().toList.sortBy(_._1)
      val options = patternByName.map((key, _) => option(value := key)(key))
      val selectButton = select(name := "conwayOptions", id := "conwayOptionsId").apply(options.toList: _*).render
      selectButton.onchange = (_) => {
        val (_, pattern) = patternByName(selectButton.selectedIndex)
        gridInput.value = pattern
        loadBoardFromText(pattern)
      }

      div()(
        label(`for` := "conwayOptions"),
        selectButton
      )
    }
    document.body.appendChild(div(
      advanceButton, span(style := "width:200px")(" "), playButton,
      div(
        gridInput,
        div(conwaySelect),
        div(loadButton),
        mouseDragBehaviour
      )
    ).render)

    grid
