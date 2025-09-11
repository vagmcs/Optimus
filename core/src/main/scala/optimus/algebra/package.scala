package optimus

import gnu.trove.map.hash.TLongDoubleHashMap
import scala.language.implicitConversions

package object algebra {

  // Anonymous constant for variables not having a symbol.
  final val ANONYMOUS = ""

  type UniqueId = Long
  type DecodedIds = Vector[Int]
  type LongDoubleMap = TLongDoubleHashMap

  private[optimus] object LongDoubleMap {

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @return an empty TLongDoubleHashMap
     */
    def empty: LongDoubleMap = new TLongDoubleHashMap()

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @param key an encoded vector of variables
     * @param value a scalar value
     * @return a TLongDoubleHashMap holding a single mapping
     */
    def apply(key: Long, value: Double): LongDoubleMap = new TLongDoubleHashMap(Array(key), Array(value))

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @param keys an array of encoded vector variables
     * @param values an array of scalar values
     * @return a TLongDoubleHashMap holding the given mappings
     */
    def apply(keys: Array[Long], values: Array[Double]): LongDoubleMap = new TLongDoubleHashMap(keys, values)

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @param map a TLongDoubleHashMap
     * @return a TLongDoubleHashMap holding the mappings
     *         of the given TLongDoubleHashMap
     */
    def apply(map: LongDoubleMap): LongDoubleMap = new TLongDoubleHashMap(map)

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @param v a variable
     * @return a TLongDoubleHashMap holding a mapping of the
     *         encoded variable to scalar 1
     */
    def apply(v: Var): LongDoubleMap = apply(encode(v.index), 1)

    /**
     * @see [[gnu.trove.map.hash.TLongDoubleHashMap]]
     * @param scalar a scalar
     * @param vars a vector of variables
     * @return a TLongDoubleHashMap holding a mapping of the
     *         encoded variables to the scalar
     */
    def apply(scalar: Const, vars: Vector[Var]): LongDoubleMap = apply(encode(vars), scalar.value)
  }

  /**
   * Szudzik pairing function is a process for uniquely encoding a pair of natural
   * numbers into a single natural number. It is used by algebra to encode variable
   * products by ID and produce unique product IDs.
   *
   * @note This variant encodes a variable ID alone.
   *
   * @param x a variable ID
   * @return a unique ID in the space of encodings for a single variable ID
   */
  def encode(x: Long): UniqueId = encode(x, -1)

  /**
   * Szudzik pairing function is a process that uniquely encodes a pair of natural
   * numbers into a single natural number. It is used by algebra to encode variable
   * products by ID and produce unique product IDs.
   *
   * @param x a variable ID
   * @param y another variable ID
   * @return a unique ID in the space of encodings for x and y
   */
  def encode(x: Long, y: Long): UniqueId = if (x < y) (y + 1) * (y + 1) + (x + 1) else (x + 1) * (x + 1) + (y + 1)

  /**
   * Szudzik pairing function is a process for uniquely encoding a pair of natural
   * numbers into a single natural number. It is used by algebra to encode variable
   * products by ID and produce unique product IDs.
   *
   * @note This variant encodes a vector of variables. The vector should contain
   *       a single variable or a pair of variables.
   *
   * @param vars a vector of variables
   * @return a unique ID in the space of encodings for the variables
   */
  def encode(vars: Vector[Var]): UniqueId =
    if (vars.size == 1) encode(vars.head.index)
    else encode(vars.head.index, vars.last.index)

  /**
   * Szudzik inverse pairing function. Uniquely decodes a natural
   * number encoding into the pair of natural numbers that produced
   * the encoding.
   *
   * @note In case the encoding refers to a single variable the vector
   *       will contain only
   *
   * @param z the number to decode
   * @return a vector holding the pair of numbers
   */
  def decode(z: UniqueId): DecodedIds = {
    val q = math.floor(math.sqrt(z.toDouble))
    val l = z - q * q
    Vector(l.toInt - 1, q.toInt - 1).filter(_ > -1)
  }

  // Algebra implicit conversions

  implicit def Int2Const(value: Int): Const = if (value == 0) Zero else Const(value.toDouble)

  implicit def Long2Const(value: Long): Const = if (value == 0) Zero else Const(value.toDouble)

  implicit def Double2Const(value: Double): Const = if (value == 0.0) Zero else Const(value)
}
