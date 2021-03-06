package org.jetbrains.plugins.scala.lang.formatter.tests

import org.jetbrains.plugins.scala.lang.formatter.AbstractScalaFormatterTestBase
import org.jetbrains.plugins.scala.lang.formatting.settings.ScalaCodeStyleSettings
import org.jetbrains.plugins.scala.util.TestUtils

trait ScalaFmtTestBase extends AbstractScalaFormatterTestBase {
  override def setUp(): Unit = {
    super.setUp()
    val scalaSettings = getScalaSettings
    scalaSettings.FORMATTER = ScalaCodeStyleSettings.SCALAFMT_FORMATTER
    scalaSettings.SCALAFMT_USE_INTELLIJ_FORMATTER_FOR_RANGE_FORMAT = false
  }

  val configPath = TestUtils.getTestDataPath + "/formatter/scalafmt/"

  def setScalafmtConfig(configFile: String): Unit =
    getScalaSettings.SCALAFMT_CONFIG_PATH = configPath + configFile
}
