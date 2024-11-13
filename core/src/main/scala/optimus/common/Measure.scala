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

package optimus.common

import com.typesafe.scalalogging.LazyLogging

object Measure extends LazyLogging {

  private def ms2Text(milliseconds: Long): String = {
    val time = milliseconds / 1000
    val seconds = time % 60
    val minutes = (time % 3600) / 60
    val hours = time / 3600
    s"${hours}h ${minutes}m ${seconds}s ${milliseconds % 1000}ms"
  }

  /**
   * Calculates the time passed from a given starting value.
   *
   * @param start starting time in milliseconds
   * @return the total time passed in seconds
   */
  def measureTime(start: Long): String = ms2Text(System.currentTimeMillis - start)

  /**
   * Measure the time passed for a given process to run.
   *
   * @param msg a message to print along measured time
   * @param process the process to run
   * @tparam T type of the resulting value
   *
   * @return the result of the process
   */
  def measureTime[T](msg: String)(process: => T): T = {
    val start = System.currentTimeMillis
    val result = process
    logger.info(s"$msg ${measureTime(start)}")
    result
  }

  /**
   * Measure the time passed for a given process to run.
   *
   * @param process the process to run
   * @tparam T type of the resulting value
   *
   * @return the result of the process along the time
   *         passed in milliseconds
   */
  def measureTime[T](process: => T): (T, Long) = {
    val start = System.currentTimeMillis
    (process, System.currentTimeMillis - start)
  }
}
