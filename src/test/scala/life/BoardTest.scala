package life

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BoardTest extends AnyWordSpec with Matchers {

  val board = Board.parse(
    """....
      |...o
      |..oo""".stripMargin,
    'o'
  )

  "Board.cellNeighbours" should {
    "return indices adjacent to alive cells" in {
      Board.cellNeighbours(Set(2, 3, 5).map(_.asCol)) shouldBe Set(1, 4, 6)
      Board.cellNeighbours(Set(0).map(_.asCol)) shouldBe Set(-1, 1)
      Board.cellNeighbours(Set(2, 4).map(_.asCol)) shouldBe Set(1, 3, 5)
      Board.cellNeighbours(Set()) shouldBe Set()
    }
  }
  "Board.diff" should {
    "return the difference between two boards" in {
      val board1 = Board.parse(
        """...o
          |.oo.
          |...o
          |....""".stripMargin,
        'o'
      )
      val board2 = Board.parse(
        """....
          |oo..
          |...o
          |o..o""".stripMargin,
        'o'
      )

      locally {
        val created: Set[(Row, Col)] = Set((3, 0), (3, 3), (1, 0)).map((r, c) => (r.asRow, c.asCol))
        val removed = Set((1.asRow, 2.asCol), (0.asRow, 3.asCol))
        board1.diff(board2) shouldBe Diff(created, removed)
      }

      locally {
        val created = Set((1.asRow, 2.asCol), (0.asRow, 3.asCol))
        val removed: Set[(Row, Col)] = Set((3, 0), (3, 3), (1, 0)).map((r, c) => (r.asRow, c.asCol))
        board2.diff(board1) shouldBe Diff(created, removed)
      }

      board1.diff(board1) shouldBe Diff(Set.empty, Set.empty)
      board2.diff(board2) shouldBe Diff(Set.empty, Set.empty)
    }
  }
  "Board.advance" should {

    def verify(original: Board, expected: Board) =
      withClue(s"${original.advance.render()} shouldBe ${expected.render()}") {
        original.advance shouldBe expected
      }

    "toggle oscillator cells" in {
      val oscillator1 = Board.parse(
        """.....
          |.....
          |.ooo.
          |.....
          |.....""".stripMargin,
        'o'
      )

      val oscillator2 = Board.parse(
        """.....
          |..o..
          |..o..
          |..o..
          |.....""".stripMargin,
        'o'
      )

      verify(oscillator1, oscillator2)
      verify(oscillator2, oscillator1)
      oscillator1 should not be (oscillator2)
    }

    "toggle block cells" in {
      val block = Board.parse(
        """....
          |.oo.
          |.oo.
          |....""".stripMargin,
        'o'
      )
      verify(block, block)
    }

    "toggle toad cells" in {
      val toad1 = Board.parse(
        """......
          |...o..
          |.o..o.
          |.o..o.
          |..o...
          |......""".stripMargin,
        'o'
      )

      val toad2 = Board.parse(
        """......
          |......
          |..ooo.
          |.ooo..
          |......
          |......""".stripMargin,
        'o'
      )

      verify(toad1, toad2)
      verify(toad2, toad1)
      toad1 should not be (toad2)
    }
  }

  "Board.render" should {
    "work" in {
      val actual = board.render('x', 'o')
      actual shouldBe
        """xxxx
          |xxxo
          |xxoo""".stripMargin
    }
  }
}
