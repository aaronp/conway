package life.ui

import org.scalajs.dom.CanvasRenderingContext2D

case class Point(x: Int, y: Int)

extension (context: CanvasRenderingContext2D)
  def expandToFill =
    val w = context.canvas.parentElement.clientWidth
    val h = context.canvas.parentElement.clientHeight
    context.canvas.width = w
    context.canvas.height = h

  def withColor(color: String)(f: => Unit): Unit =
    val before = context.fillStyle
    context.fillStyle = color
    try {
      f
    } finally {
      context.fillStyle = before
    }
  def clear(color: String = "white") = context.withColor(color) {
    context.fillRect(0, 0, context.canvas.width, context.canvas.height)
  }
  def line(from: Point, to: Point) =
    context.beginPath()
    context.moveTo(from.x, from.y)
    context.lineTo(to.x, to.y)
    context.stroke()
