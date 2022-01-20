/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import javax.inject.Inject
import play.api.mvc.{ActionBuilder, AnyContent}
import uk.gov.hmrc.internalauth.client._
import play.api.mvc.Results._

import scala.concurrent.Future

trait AdminAuthenticationAction {
  def authorisedTeamMember: ActionBuilder[AuthenticatedRequest, AnyContent]
}

class LDAPAuthenticationAction @Inject() (internalAuth: BackendAuthComponents) extends AdminAuthenticationAction {

  private val permission = Predicate.Permission(
    Resource(
      ResourceType("transit-movements-trader-admin-frontend"),
      ResourceLocation("")
    ),
    IAAction("ADMIN")
  )

  def authorisedTeamMember: ActionBuilder[AuthenticatedRequest, AnyContent] =
    internalAuth.authorizedAction(
      predicate = permission,
      onUnauthorizedError = Future.successful(Unauthorized),
      onForbiddenError = Future.successful(Forbidden)
    )

}
