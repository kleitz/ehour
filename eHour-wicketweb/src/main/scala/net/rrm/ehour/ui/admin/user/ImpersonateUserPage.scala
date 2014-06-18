package net.rrm.ehour.ui.admin.user

import net.rrm.ehour.ui.admin.AbstractAdminPage
import net.rrm.ehour.ui.common.border.{GreyBlueRoundedBorder, GreyRoundedBorder}
import net.rrm.ehour.ui.common.panel.entryselector.EntrySelectedEvent
import net.rrm.ehour.ui.common.session.EhourWebSession
import net.rrm.ehour.ui.common.util.AuthUtil
import net.rrm.ehour.ui.common.wicket.AjaxLink
import net.rrm.ehour.ui.common.wicket.AjaxLink._
import net.rrm.ehour.user.service.UserService
import org.apache.wicket.event.IEvent
import org.apache.wicket.markup.html.basic.Label
import org.apache.wicket.markup.html.panel.Fragment
import org.apache.wicket.model.ResourceModel
import org.apache.wicket.spring.injection.annot.SpringBean

class ImpersonateUserPage extends AbstractAdminPage(new ResourceModel("admin.impersonate.title")) {
  val Self = this

  val BorderId = "border"
  val FrameId = "frame"
  val ContentId = "content"

  val frame = new GreyRoundedBorder(FrameId, new ResourceModel("admin.export.title"))
  val border = new GreyBlueRoundedBorder(BorderId).setOutputMarkupId(true).asInstanceOf[GreyBlueRoundedBorder]

  @SpringBean
  protected var userService: UserService = _

  override def onInitialize() {
    super.onInitialize()

    add(new UserSelectionPanel("userSelection"))
    add(frame)
    frame.add(border)
    border.add(createNoUserSelectedFragment(ContentId))
  }

  override def onEvent(wrappedEvent: IEvent[_]) {
    wrappedEvent.getPayload match {
      case event: EntrySelectedEvent => {
        border.addOrReplace(createUserSelectedFragment(ContentId, event.userId))
        event.refresh(border)
      }
      case _ =>
    }
  }

  private def createNoUserSelectedFragment(id: String) = new Fragment(id, "noUserSelected", Self).setOutputMarkupId(true)

  private def createUserSelectedFragment(id: String, userId: Integer) = {
    val user = userService.getUser(userId)

    val f = new Fragment(id, "userSelected", Self)
    f.setOutputMarkupId(true)

    val linkCallback: LinkCallback = target => {
      val session = EhourWebSession.getSession
      session.impersonateUser(user)
      val roles = session.getRoles

      val homepageForRole = AuthUtil.getHomepageForRole(roles)
      setResponsePage(homepageForRole)
    }

    val link = new AjaxLink("impersonateLink", linkCallback)
    link.add(new Label("name", user.getFullName))
    f.add(link)

    f
  }
}


