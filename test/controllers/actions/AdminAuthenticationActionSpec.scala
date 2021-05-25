/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SpecBase
import play.api.http.Status
import play.api.mvc.ControllerComponents
import play.api.mvc.Results.Ok
import play.api.test.Helpers.{status, _}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.{HeaderNames, UpstreamErrorResponse}
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

import scala.concurrent.{ExecutionContext, Future}

class AdminAuthenticationActionSpec extends SpecBase {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val controllerComponents: ControllerComponents = Helpers.stubControllerComponents()

  val fakeRequestWithToken = FakeRequest("", "").withHeaders(HeaderNames.authorisation -> "abc")

  "LDAPAuthenticationAction" - {

    "must return OK when authorised" in {

      val authAction = new LDAPAuthenticationAction(BackendAuthComponentsStub())

      val result = authAction.authorisedTeamMember { _ => Ok("success") }(fakeRequestWithToken)

      status(result) mustBe OK
    }

    "must return Unauthorised" in {

      val unauthorisedBehaviour = StubBehaviour(
        authenticate = _ => Future.failed(UpstreamErrorResponse("Unauthorised", Status.UNAUTHORIZED))
      )

      val authAction = new LDAPAuthenticationAction(BackendAuthComponentsStub(unauthorisedBehaviour))

      val result = authAction.authorisedTeamMember { _ => Ok("success") }(fakeRequestWithToken)

      status(result) mustBe Status.UNAUTHORIZED
    }

    "must return Forbidden" in {

      val forbiddenBehaviour = StubBehaviour(
        authenticate = _ => Future.failed(UpstreamErrorResponse("Forbidden", Status.FORBIDDEN))
      )

      val authAction = new LDAPAuthenticationAction(BackendAuthComponentsStub(forbiddenBehaviour))

      val result = authAction.authorisedTeamMember { _ => Ok("success") }(fakeRequestWithToken)

      status(result) mustBe Status.FORBIDDEN
    }
  }
}