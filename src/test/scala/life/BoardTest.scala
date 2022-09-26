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

  "Board.parse" should {
    "work for advanced shapes" in {
      Board.parse("""....oo
                    |....o.o
                    |....o
                    |.......o
                    |ooo....o.o
                    |o......o..o...ooo
                    |.o.......oo..o..oo
                    |...ooo.......o.....oo
                    |.............oo....ooo
                    |....o.o...ooo.........o
                    |.....oo..o..o......o..o
                    |.........o.........o
                    |.........oo........o.o
                    |......ooo
                    |.....o..o
                    |.....o
                    |.....oo
                    |......o
                    |
                    |.......oo.ooo
                    |.......oo
                    |........o...o
                    |.........oo""".stripMargin, 'o')
    }
  }
  "Board.advance" should {
    def verifyAdvance(original: Board, expected: Board) =
      withClue(s"${original.advance.render()}\nshould be\n${expected.render()}\n") {
        original.advance shouldBe expected
      }

    "work for the glider" in {
      val glider1 = Board.parse(
        """..o..
          |...o.
          |.ooo.
          |.....
          |.....""".stripMargin,
        'o'
      )
      glider1.becomesAlive(1.asRow, 1.asCol) shouldBe true
      glider1.becomesAlive(1.asRow, 2.asCol) shouldBe false
      glider1.survives(2.asRow, 1.asCol) shouldBe false
      glider1.survives(2.asRow, 2.asCol) shouldBe true

      val glider2 = Board.parse(
        """.....
          |.o.o.
          |..oo.
          |..o..
          |.....""".stripMargin,
        'o'
      )
      val glider3 = Board.parse(
        """.....
          |...o.
          |.o.o.
          |..oo.
          |.....""".stripMargin,
        'o'
      )
      val glider4 = Board.parse(
        """......
          |..o...
          |...oo.
          |..oo..
          |......""".stripMargin,
        'o'
      )

      verifyAdvance(glider1, glider2)
      verifyAdvance(glider2, glider3)
      verifyAdvance(glider3, glider4)
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

      verifyAdvance(oscillator1, oscillator2)
      verifyAdvance(oscillator2, oscillator1)
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
      verifyAdvance(block, block)
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

      verifyAdvance(toad1, toad2)
      verifyAdvance(toad2, toad1)
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
