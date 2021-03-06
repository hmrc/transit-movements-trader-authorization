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

import controllers.actions.AdminAuthenticationAction
import play.api.mvc.{ActionBuilder, AnyContent, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.internalauth.client.AuthenticatedRequest
import uk.gov.hmrc.internalauth.client.test.BackendAuthComponentsStub

import scala.concurrent.ExecutionContext

class FakeLDAPAuthenticationAction extends AdminAuthenticationAction {

  implicit val ec: ExecutionContext                       = scala.concurrent.ExecutionContext.Implicits.global
  implicit val controllerComponents: ControllerComponents = stubControllerComponents()

  override def authorisedTeamMember: ActionBuilder[AuthenticatedRequest, AnyContent] =
    BackendAuthComponentsStub().authenticatedAction()

}
