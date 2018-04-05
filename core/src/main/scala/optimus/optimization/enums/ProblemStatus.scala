/*
 *
 *    /\\\\\
 *   /\\\///\\\
 *  /\\\/  \///\\\    /\\\\\\\\\     /\\\       /\\\
 *  /\\\      \//\\\  /\\\/////\\\ /\\\\\\\\\\\ \///    /\\\\\  /\\\\\     /\\\    /\\\  /\\\\\\\\\\
 *  \/\\\       \/\\\ \/\\\\\\\\\\ \////\\\////   /\\\  /\\\///\\\\\///\\\ \/\\\   \/\\\ \/\\\//////
 *   \//\\\      /\\\  \/\\\//////     \/\\\      \/\\\ \/\\\ \//\\\  \/\\\ \/\\\   \/\\\ \/\\\\\\\\\\
 *     \///\\\  /\\\    \/\\\           \/\\\_/\\  \/\\\ \/\\\  \/\\\  \/\\\ \/\\\   \/\\\ \////////\\\
 *        \///\\\\\/     \/\\\           \//\\\\\   \/\\\ \/\\\  \/\\\  \/\\\ \//\\\\\\\\\  /\\\\\\\\\\
 *           \/////       \///             \/////    \///  \///   \///   \///  \/////////   \//////////
 *
 *  Copyright (C) 2014 Evangelos Michelioudakis, Anastasios Skarlatidis
 *
 *  This file is part of Optimus.
 *
 *  Optimus is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  Optimus is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 *  License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Optimus. If not, see <http://www.gnu.org/licenses/>.
 *       
 */

package optimus.optimization.enums

import enumeratum._
import scala.collection.immutable._

sealed abstract class ProblemStatus(override val entryName: String) extends EnumEntry

object ProblemStatus extends Enum[ProblemStatus] {

  val values: IndexedSeq[ProblemStatus] = findValues

  case object NOT_SOLVED    extends ProblemStatus("Not solved")
  case object OPTIMAL      extends ProblemStatus("Optimal")
  case object SUBOPTIMAL   extends ProblemStatus("Suboptimal")
  case object UNBOUNDED     extends ProblemStatus("Unbounded")
  case object INFEASIBLE     extends ProblemStatus("Infeasible")
}
