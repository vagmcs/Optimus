/*
 *
 *   /\\\\\
 *  /\\\///\\\
 * /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 * /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 * \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *  \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *    \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *       \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *          \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 * The mathematical programming library for Scala.
 *
 */

package optimus.algebra

import org.scalacheck.Gen
import org.scalatest.{ FunSpec, Matchers }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

final class EncodeDecodeSpecTest extends FunSpec with Matchers with ScalaCheckPropertyChecks {

  describe("Encode and decode a single number") {

    it("An Int MaxValue alone should encode into 4611686016279904256") {
      encode(Int.MaxValue - 1) shouldEqual 4611686014132420609L
      decode(encode(Int.MaxValue - 1)) shouldEqual Vector(Int.MaxValue - 1)
    }

    it("0 should encode to 1") {
      encode(0) shouldEqual 1
      decode(encode(0)) shouldEqual Vector(0)
    }

    it("1 should encode to 4") {
      encode(1) shouldEqual 4
      decode(encode(1)) shouldEqual Vector(1)
    }

    it("An encoding should never be identical to another") {
      val encodings = (1L to 1000L).map(encode)
      encodings.length shouldEqual encodings.distinct.length

      val generator = for {
        x <- Gen.choose(0, Int.MaxValue)
        y <- Gen.choose(0, Int.MaxValue)
      } yield (x, y)

      forAll(generator) {
        case (x: Int, y: Int) => whenever (x != y) {
          encode(x) shouldNot be (encode(y))
        }
      }
    }
  }

  describe("Encode and decode pairs of numbers") {

    it("0,1 should encode to 5") {
      encode(0, 1) shouldEqual 5
      decode(encode(0, 1)) shouldEqual Vector(0, 1)
    }

    it("0,1 encoding should be identical to 1,0") {
      encode(0, 1) shouldEqual encode(1, 0)
      decode(encode(0, 1)) shouldEqual decode(encode(1, 0))
    }

    it("Order should not change the encoding") {

      val generator = for {
        x <- Gen.choose(0, Int.MaxValue)
        y <- Gen.choose(0, Int.MaxValue)
      } yield (x, y)

      forAll(generator) {
        case (x: Int, y: Int) =>
          encode(x, y) shouldEqual encode(y, x)
      }
    }

    it("An encoding should never be identical to another") {

      val generator = for {
        x <- Gen.choose(0, Int.MaxValue)
        y <- Gen.choose(0, Int.MaxValue)
        z <- Gen.choose(0, Int.MaxValue)
        q <- Gen.choose(0, Int.MaxValue)
      } yield (x, y, z, q)

      forAll(generator) {
        case (x: Int, y: Int, z: Int, q: Int) => whenever (x != z && y != q) {
          encode(x, y) shouldNot be (encode(z, q))
        }
      }
    }
  }
}
