package spatutorial.client.components

import japgolly.univeq.UnivEq
import spatutorial.client.components.Bootstrap.CommonStyle

import spatutorial.client.CssSettings._
import scalacss.internal.mutable
import spatutorial.client.components.Bootstrap.CommonStyle._

class BootstrapStyles(implicit r: mutable.Register) extends StyleSheet.Inline()(r) {

  import dsl._

  implicit val styleUnivEq: UnivEq[CommonStyle.Value] = new UnivEq[CommonStyle.Value] {}

  val csDomain = Domain.ofValues(default, primary, success, info, warning, danger)

  val contextDomain = Domain.ofValues(success, info, warning, danger)

  def commonStyle[A: UnivEq](domain: Domain[A], base: String) = styleF(domain)(opt =>
    styleS(addClassNames(base, s"$base-$opt"))
  )

  def styleWrap(classNames: String*) = style(addClassNames(classNames: _*))

  val buttonOpt = commonStyle(csDomain, "btn")

  val button = buttonOpt(default)

  val cardOpt = commonStyle(csDomain, "card")

  val card = cardOpt(default)

  val badgeOpt = commonStyle(csDomain, "badge")

  val badge = badgeOpt(default)

  val alert = commonStyle(contextDomain, "alert")

  val cardHeading = styleWrap("card-header")

  val cardBody = styleWrap("card-body")

  // wrap styles in a namespace, assign to val to prevent lazy initialization
  object modal {
    val modal = styleWrap("modal")
    val fade = styleWrap("fade")
    val dialog = styleWrap("modal-dialog")
    val content = styleWrap("modal-content")
    val header = styleWrap("modal-header")
    val body = styleWrap("modal-body")
    val footer = styleWrap("modal-footer")
  }

  val _modal = modal

  object listGroup {
    val listGroup = styleWrap("list-group")
    val item = styleWrap("list-group-item")
    val itemOpt = commonStyle(contextDomain, "list-group-item")
  }

  val _listGroup = listGroup
  val floatRight = styleWrap("float-right")
  val buttonSM = styleWrap("btn-sm")
  val buttonSecondary = styleWrap("btn-secondary")
  val close = styleWrap("close")

  val badgePill = style(addClassName("badge-pill"), borderRadius(1.em))

  val navbar = styleWrap("navbar-nav", "mr-auto")

  val formGroup = styleWrap("form-group")
  val formControl = styleWrap("form-control")

  val spacingMR1 = styleWrap("mr-1")
  val spacingMT3 = styleWrap("mt-3")

}
