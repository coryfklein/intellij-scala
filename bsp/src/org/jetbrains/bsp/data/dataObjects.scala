package org.jetbrains.bsp.data

import java.io.File
import java.net.URI
import java.util

import com.intellij.openapi.externalSystem.model.project.AbstractExternalEntityData
import com.intellij.openapi.externalSystem.model.{Key, ProjectKeys}
import org.jetbrains.bsp.BSP
import org.jetbrains.bsp.data.BspEntityData._
import org.jetbrains.plugins.scala.project.Version
import org.jetbrains.plugins.scala.project.external.SdkReference

abstract class BspEntityData extends AbstractExternalEntityData(BSP.ProjectSystemId) with Product {

  // need to manually specify equals/hashCode here because it is not generated for case classes inheriting from
  // AbstractExternalEntityData
  override def equals(obj: scala.Any): Boolean = obj match {
    case data: BspEntityData =>
      //noinspection CorrespondsUnsorted
      this.canEqual(data) &&
        (this.productIterator sameElements data.productIterator)
    case _ => false
  }

  override def hashCode(): Int = runtime.ScalaRunTime._hashCode(this)
}

object BspEntityData {
  def datakey[T](clazz: Class[T],
                 weight: Int = ProjectKeys.MODULE.getProcessingWeight + 1
                ): Key[T] = new Key(clazz.getName, weight)
}


@SerialVersionUID(3)
case class ScalaSdkData(
    scalaOrganization: String,
    scalaVersion: Option[Version],
    scalacClasspath: util.List[File],
    scalacOptions: util.List[String]
) extends BspEntityData

object ScalaSdkData {
  val Key: Key[ScalaSdkData] = datakey(classOf[ScalaSdkData])
  val LibraryName: String = "scala-sdk"
}


/**
  * Metadata to about bsp targets that have been mapped to IntelliJ modules.
  * @param targetIds target ids mapped to module
  */
@SerialVersionUID(4)
case class BspMetadata(targetIds: util.List[URI])
object BspMetadata {
  val Key: Key[BspMetadata] = datakey(classOf[BspMetadata])
}

