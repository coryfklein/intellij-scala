package org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic

import java.io.PrintWriter
import java.net.URL
import java.nio.file.Path

import com.geirsson.coursiersmall._
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic.ScalafmtDynamicDownloader._
import org.jetbrains.plugins.scala.lang.formatting.scalafmt.dynamic.utils.BuildInfo

import scala.concurrent.duration.Duration
import scala.util.Try

class ScalafmtDynamicDownloader(downloadProgressWriter: PrintWriter,
                                ttl: Option[Duration] = None) {

  def download(version: String): Either[DownloadFailure, DownloadSuccess] = {
    Try {
      val settings = new Settings()
        .withDependencies(dependencies(version))
        .withTtl(ttl.orElse(Some(Duration.Inf)))
        .withWriter(downloadProgressWriter)
        .withRepositories(List(
          Repository.MavenCentral,
          Repository.Ivy2Local,
          Repository.SonatypeReleases,
          Repository.SonatypeSnapshots
        ))
      val jars: Seq[Path] = CoursierSmall.fetch(settings)
      val urls = jars.map(_.toUri.toURL).toArray
      DownloadSuccess(version, urls)
    }.toEither.left.map {
      case e: ResolutionException => DownloadResolutionError(version, e)
      case e => DownloadUnknownError(version, e)
    }
  }

  private def dependencies(version: String): List[Dependency] = {
    List(
      new Dependency(organization(version), s"scalafmt-cli_${scalaBinaryVersion(version)}", version),
      new Dependency("org.scala-lang", "scala-reflect", scalaVersion(version))
    )
  }

  private def scalaBinaryVersion(version: String): String =
    if (version.startsWith("0.")) "2.11"
    else "2.12"

  private def scalaVersion(version: String): String =
    if (version.startsWith("0.")) BuildInfo.scala211
    else BuildInfo.scala

  private def organization(version: String): String =
    if (version.startsWith("1") || version.startsWith("0") || version == "2.0.0-RC1") {
      "com.geirsson"
    } else {
      "org.scalameta"
    }

}

object ScalafmtDynamicDownloader {
  sealed trait DownloadResult {
    def version: String
  }
  case class DownloadSuccess(version: String, jarUrls: Seq[URL]) extends DownloadResult
  sealed trait DownloadFailure extends DownloadResult {
    def cause: Throwable
  }
  case class DownloadResolutionError(version: String, cause: ResolutionException) extends DownloadFailure
  case class DownloadUnknownError(version: String, cause: Throwable) extends DownloadFailure
}
