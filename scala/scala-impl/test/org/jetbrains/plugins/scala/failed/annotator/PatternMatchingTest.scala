package org.jetbrains.plugins.scala.failed.annotator

import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.junit.experimental.categories.Category

/**
  * @author Anton.Yalyshev
  * @since 02.12.2017.
  */
@Category(Array(classOf[PerfCycleTests]))
class PatternMatchingTest extends ScalaLightCodeInsightFixtureTestAdapter {
  override protected def shouldPass: Boolean = false

  def testSCL13151() = {
    val text =
      """
        |class Term[A]
        |class Number(val n: Int) extends Term[Int]
        |object X {
        |  def f[B](t: Term[B]): B = t match {
        |    case y: Number => y.n // False error: Expression of type Int doesn't conform to expected type B
        |  }
        |}
      """.stripMargin
    checkTextHasNoErrors(text)
  }

  def testSCL13141() = {
    val text =
      """
        |object X {
        |  def f(x : List[_]): Any = x match { case z : List[a] => { val e : a = z.head; e } }
        |}
      """.stripMargin
    checkTextHasNoErrors(text)
  }
}