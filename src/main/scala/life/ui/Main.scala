package life.ui

import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main {
  @JSExport("apply")
  def apply(canvas: Canvas) = {

    val renderer = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    renderer.expandToFill
    renderer.clear(Grid.DeadColor)

    Grid.init(renderer, 15)
  }
}
