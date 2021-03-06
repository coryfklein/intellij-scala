package org.jetbrains.sbt
package project.notifications

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.{ActionManager, ActionPlaces}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.playback.commands.ActionCommand
import org.jetbrains.plugins.scala.util.NotificationUtil
import org.jetbrains.sbt.project.notifications.LegacyProjectFormatNotifier._
import org.jetbrains.sbt.project.settings.SbtLocalSettings

/**
 * @author Pavel Fatin
 */
class LegacyProjectFormatNotifier(project: Project) extends ProjectComponent {
  override def projectOpened() {

    val sbtSettings = SbtLocalSettings.getInstance(project)

    if (!sbtSettings.sbtSupportSuggested) {
      val modules = ModuleManager.getInstance(project).getModules.toSeq

      val fromGenIdea = modules.exists(_.getModuleFilePath.contains(".idea_modules"))

      if (fromGenIdea) {
        sbtSettings.sbtSupportSuggested = true

        val builder = NotificationUtil.builder(project, Message).setNotificationType(NotificationType.WARNING)

        builder.setHandler { _ =>
          val manager = ActionManager.getInstance
          Option(manager.getAction("ImportProject")).foreach { action =>
            manager.tryToExecute(action, ActionCommand.getInputEvent("ImportProject"), null, ActionPlaces.UNKNOWN, true)
          }
        }

        builder.show()
      }
    }
  }
}

object LegacyProjectFormatNotifier {
  def Message: String = "<html>" +
          "<p>This IDEA project was converted from an sbt project with the <b>gen-idea</b> tool," +
          "<br />which currently relies on a legacy Scala project model.</p>" +
          "<br />" +
          "<p>Please consider using the built-in sbt support via the <a href=\"ftp://import\">Import project</a> action.</p>" +
          "</html>"
}
