package life

opaque type Row = Int

object Row:
  def toInt(row : Row): Int = row
  def apply(r: Int): Row = r

opaque type Col = Int

object Col:
  def toInt(col : Col): Int = col
  def apply(c: Int): Col = c

extension (num: Int)
  def asRow = Row(num)
  def asCol = Col(num)

// the alive cells
opaque type BoardRow = Set[Col]
object BoardRow:
  def parse(text: String, aliveChar: Char): BoardRow =
    val byCol = text.zipWithIndex.collect { case (`aliveChar`, idx) =>
      idx.asCol
    }
    byCol.toSet

extension (row: BoardRow)
  def width = row.maxOption.getOrElse(0)
  def isEmpty = row.isEmpty
  def pretty =
    val rowStr = (0 to row.width).map {
      case c if row(c) => 'o'
      case _ => '.'
    }
    rowStr.mkString("")

case class Diff(toggledAlive :Set[(Row, Col)], toggledDead :Set[(Row, Col)])

opaque type Board = Map[Row, BoardRow]
extension (board: Board)
  def height = board.keySet.maxOption.getOrElse(0)

  /**
   * This is used so that cells can be toggled on/off after an update
   * @param after
   * @return the diff between the two boards
   */
  def diff(after : Board): Diff =
    val toggledDead = {
      val removedRows = board.keySet -- after.keySet
      val removedCells: Set[(Row, Col)] = removedRows.flatMap { r =>
        board(r).map(r -> _)
      }
      val others = (board.keySet & after.keySet).flatMap { r =>
        val removed = board(r) -- after(r)
        removed.map(r -> _)
      }
      removedCells ++ others
    }

    val toggledAlive = {
      val addedRows = after.keySet -- board.keySet
      val addedCells: Set[(Row, Col)] = addedRows.flatMap { r =>
        after(r).map(r -> _)
      }
      val others = (board.keySet & after.keySet).flatMap { r =>
        val added = after(r) -- board(r)
        added.map(r -> _)
      }
      addedCells ++ others
    }
    Diff(toggledAlive, toggledDead)

  def isAlive(row: Row, col: Col): Boolean = board.get(row).exists(r => r(col))

  def aliveCells : Map[Row, BoardRow] = board

  /** Any live cell with two or three live neighbours survives. Any dead cell with three live neighbours becomes a live cell. All other live cells die in the
    * next generation. Similarly, all other dead cells stay dead.
    *
    * @return
    *   the 'next' board given the above rules
    */
  def advance: Board =
    def applyRulesToRow(row: Row, aliveCells: BoardRow, deadCells: BoardRow): BoardRow =
      // Any live cell with two or three live neighbours survives.
      val survivingCells   = aliveCells.filter(col => survives(row, col))
      // Any dead cell with three live neighbours becomes a live cell
      val resurrectedCells = deadCells.filter(col => becomesAlive(row, col))

      // All other live cells die in the next generation. Similarly, all other dead cells stay dead.
      survivingCells ++ resurrectedCells

    val deadRow: Set[Col] = (0 to board.maxWidth).toSet

    // get the neighbours to our alive rows
    val rowsToEval: Set[Row] = board.keySet.flatMap { row =>
      Set(row - 1, row, row + 1)
    }

    // we have a sparse representation of the board (e.g. it's not a 2x2 matrix, but a map).
    // that means that our approach here isn't an exhaustive "from 0 to max width and height",
    // but rather a "the neighbours of the alive cells"
    val toggledRowsByIndex = rowsToEval.flatMap { rowIndex =>

      // here we have to compute an optional BoardRow
      val mappedRow = board.get(rowIndex) match {
        // for the 'None' case, we're on a row above or below a row with alive cells.
        // we need to consider the columns of the alive neighbouring rows (or just brute-force for simplicity and take the perf hit)
        case None             => applyRulesToRow(rowIndex, Set.empty, deadRow)
        case Some(thisRowsAliveCells) =>
          // the dead neighbours are this row's alive neighbours PLUS cells which may be neighbours to rows above and below
          val cellsToCheck = {
            val rowAbove = board.getOrElse(rowIndex - 1, Set.empty)
            val rowBellow = board.getOrElse(rowIndex + 1, Set.empty)
            Board.rowNeighbours(thisRowsAliveCells ++ rowAbove ++ rowBellow)
          }
          val deadNeighbours = cellsToCheck -- thisRowsAliveCells
          applyRulesToRow(rowIndex, thisRowsAliveCells, deadNeighbours)
      }
      if mappedRow.isEmpty then None else Some(rowIndex -> mappedRow)
    }
    toggledRowsByIndex.toMap

  /**
   * @return a new board with the cell at the given row and col switched e.g. alive becomes dead and dead becomes alive
   */
  def toggle(row: Row, col: Col): Board =
    val newRow = board.get(row) match {
      case None => Set(col)
      case Some(boardRow) if board.isAlive(row, col) => boardRow - col
      case Some(boardRow) => boardRow + col
    }
    board.updated(row, newRow)

  private def allNeighbours(row: Row, col: Col) = LazyList(
    (row - 1, col - 1),
    (row - 1, col),
    (row - 1, col + 1),
    (row, col - 1),
    (row, col + 1),
    (row + 1, col - 1),
    (row + 1, col),
    (row + 1, col + 1)
  )

  private def aliveNeighbours(row: Row, col: Col): Int = allNeighbours(row, col).count(isAlive)

  // Any live cell with two or three live neighbours survives.
  private[life] def survives(row: Row, col: Col) =
    val count = board.aliveNeighbours(row, col)
    count == 2 || count == 3

  private[life] def becomesAlive(row: Row, col: Col) = board.aliveNeighbours(row, col) == 3

  private def rowWidth(r: Row) = board.get(r).map(_.width).getOrElse(0)

  private def maxWidth: Int = (0 to board.height).map(r => rowWidth(r.asRow)).maxOption.getOrElse(0)

  def render(deadChar: Char = '.', aliveChar: Char = 'o'): String =
    val maxWidthVal = maxWidth
    val rows = (0 to height).map { r =>
      val rowStr = (0 to maxWidthVal).map {
        case c if isAlive(r.asRow, c.asCol) => aliveChar
        case _                              => deadChar
      }
      rowStr.mkString("")
    }
    rows.mkString("\n")

object Board:

  private[life] def rowNeighbours(cells: BoardRow): Set[Int] = cells.flatMap { col =>
    Set(col - 1, col, col + 1)
  }

  def apply() : Board = Map.empty
  def parse(text: String, aliveChar: Char): Board =
    val byRow = text.linesIterator.zipWithIndex.flatMap { case (rowText, rowIdx) =>
      val row = BoardRow.parse(rowText, aliveChar)
      if row.isEmpty then None else Some(rowIdx.asRow -> row)
    }
    byRow.toMap
