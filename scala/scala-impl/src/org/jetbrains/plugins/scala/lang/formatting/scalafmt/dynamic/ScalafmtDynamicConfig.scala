package org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic

import java.lang.reflect.Constructor

import org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic.exceptions.ReflectionException
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic.utils.ReflectUtils._

import scala.util.Try

//noinspection TypeAnnotation
class ScalafmtDynamicConfig private[dynamic](val fmtReflect: ScalafmtReflect,
                                             private[dynamic] val target: Object, // real config object
                                             private val classLoader: ClassLoader) {

  private val targetCls = target.getClass
  private val constructor: Constructor[_] = targetCls.getConstructors()(0)
  private val constructorParams = constructor.getParameters.map(_.getName)
  private val rewriteParamIdx = constructorParams.indexOf("rewrite").ensuring(_ >= 0)
  private val emptyRewrites = target.invoke("apply$default$" + (rewriteParamIdx + 1))

  private val dialectCls = classLoader.loadClass("scala.meta.Dialect")
  private val dialectsCls = classLoader.loadClass("scala.meta.dialects.package")

  private val rewriteRulesMethod = Try(targetCls.getMethod("rewrite")).toOption

  private val continuationIndentMethod = Try(targetCls.getMethod("continuationIndent")).toOption
  private val continuationIndentCallSiteMethod = Try(targetCls.getMethod("continuationIndentCallSite")).toOption
  private val continuationIndentDefnSiteMethod = Try(targetCls.getMethod("continuationIndentDefnSite")).toOption
  private val DefaultIndentCallSite = 2
  private val DefaultIndentDefnSite = 4

  private val sbtDialect: Object = {
    try dialectsCls.invokeStatic("Sbt") catch {
      case ReflectionException(_: NoSuchMethodException) =>
        dialectsCls.invokeStatic("Sbt0137")
    }
  }

  val version: String = {
    target.invokeAs[String]("version").trim
  }

  def isIncludedInProject(filename: String): Boolean = {
    val matcher = target.invoke("project").invoke("matcher")
    matcher.invokeAs[java.lang.Boolean]("matches", filename.asParam)
  }

  def withSbtDialect: ScalafmtDynamicConfig = {
    // TODO: maybe hold loaded classes in some helper class not to reload them each time?
    val newTarget = target.invoke("withDialect", (dialectCls, sbtDialect))
    new ScalafmtDynamicConfig(fmtReflect, newTarget, classLoader)
  }

  // TODO: what about rewrite tokens?
  def hasRewriteRules: Boolean = {
    rewriteRulesMethod match {
      case Some(method) =>
        // > v0.4.1
        val rewriteSettings = method.invoke(target) // TODO: check whether it is correct for all versions
        !rewriteSettings.invoke("rules").invokeAs[Boolean]("isEmpty")
      case None =>
        false
    }
  }

  def withoutRewriteRules: ScalafmtDynamicConfig = {
    if (hasRewriteRules) {
      // FIXME: this is only tested for version 1.5.1, check behaviour for other versions
      val fieldsValues = constructorParams.map(param => target.invoke(param))
      fieldsValues(rewriteParamIdx) = emptyRewrites
      val targetNew = constructor.newInstance(fieldsValues: _*).asInstanceOf[Object]
      new ScalafmtDynamicConfig(fmtReflect, targetNew, classLoader)
    } else {
      this
    }
  }

  // TODO: check whether it is correct for all versions
  val continuationIndentCallSite: Int = {
    continuationIndentMethod match {
      case Some(method) => // >v0.4
        val indentsObj = method.invoke(target)
        indentsObj.invokeAs[Int]("callSite")
      case None =>
        continuationIndentCallSiteMethod match {
          case Some(method) => // >v0.2.0
            method.invoke(target).asInstanceOf[Int]
          case None =>
            DefaultIndentCallSite
        }
    }
  }

  // TODO: check whether it is correct for all versions
  val continuationIndentDefnSite: Int = {
    continuationIndentMethod match {
      case Some(method) =>
        val indentsObj = method.invoke(target)
        indentsObj.invokeAs[Int]("defnSite")
      case None =>
        continuationIndentDefnSiteMethod match {
          case Some(method) =>
            method.invoke(target).asInstanceOf[Int]
          case None =>
            DefaultIndentDefnSite
        }
    }
  }

  override def equals(obj: Any): Boolean = target.equals(obj)

  override def hashCode(): Int = target.hashCode()
}