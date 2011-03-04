package org.jetbrains.plugins.scala
package codeInspection.methodSignature

import com.intellij.codeInspection._
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.jetbrains.plugins.scala.codeInspection.InspectionsUtil
import quickfix.RemoveEqualsSign

class UnitMethodDefinedWithEqualsSign extends LocalInspectionTool {
  @Language("HTML")
  override val getStaticDescription = """<html><body>
<p>Methods with a result type of <code>Unit</code> are only executed for their <a href="http://en.wikipedia.org/wiki/Side_effect_(computer_science)">side effects</a>.</p>
<p>A better way to express such methods is to leave off the equals sign,
and enclose the body of the method in curly braces.</p>
<p>In this form, the method looks like a <dfn>procedure</dfn>, a method that is executed only for its side effects:</p>
<br>
<pre><code>
  <span style="color:#808080">// excessive clutter, looks like a function</span>
  <strong style="color:#000080">def</strong> close() = { println("closed") }

  <span style="color:#808080">// may accidentally change its result type after changes in body</span>
  <strong style="color:#000080">def</strong> close() = { file.delete() } <span style="color:#808080">// method result type is <code>Boolean</code> now</span>

  <span style="color:#808080">// concise form, side-effect is clearly stated, result type is always <code>Unit</code></span>
  <strong style="color:#000080">def</strong> close() { file.delete() }
</code></pre>
<p><small>* Refer to Programming in Scala, 4.1 Classes, fields, and methods</small></p>
</body></html>
    """

  def getGroupDisplayName = InspectionsUtil.MethodSignature

  def getDisplayName = "Method with Unit result type defined with equals sign"

  def getShortName = getDisplayName

  override def isEnabledByDefault = true

  override def getID = "UnitMethodDefinedWithEqualsSign"

  override def buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = VisitorWrapper {
    case f: ScFunctionDefinition if !f.hasExplicitType && f.hasUnitReturnType =>
      f.assignment.foreach { assignment =>
        holder.registerProblem(assignment, getDisplayName, new RemoveEqualsSign(f))
      }
  }
}