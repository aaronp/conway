package life

import org.scalajs.dom.html.Canvas
import org.scalajs.dom.{CanvasRenderingContext2D, MouseEvent, document}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Main")
object Main {
  @JSExport("apply")
  def apply(canvas: Canvas) = {

    val renderer = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
    renderer.expandToFill
    renderer.clear("grey")

    Grid.init(renderer, 40)
  }
}
