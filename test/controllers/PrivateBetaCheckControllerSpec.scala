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

package controllers

import base.SpecBase
import controllers.actions.UserAuthenticationAction
import models.domain.Status.Active
import models.domain.{Eori, User, UserId}
import models.requests.PrivateBetaCheck
import play.api.http.Status
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test._
import repositories.FakePrivateBetaUserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PrivateBetaCheckControllerSpec extends SpecBase {

  private val fakeRequest = FakeRequest("GET", "/")
    .withBody(PrivateBetaCheck(Eori("eoriValue")))

  private val fakeAuthFilter = new UserAuthenticationAction {

    override protected def filter[A](request: Request[A]): Future[Option[Result]] =
      Future.successful(None)

    override protected def executionContext: ExecutionContext = global
  }

  "GET /features/private-beta" - {
    "returns 204 when there is a matching user eori" in {

      val user = User(UserId("id"), "name", Eori("eori"), Active)

      val repository = new FakePrivateBetaUserRepository {
        override def getUserByEori(eori: Eori): Future[Option[User]] =
          Future.successful(Some(user))
      }

      val controller = new PrivateBetaCheckController(Helpers.stubControllerComponents(), repository, fakeAuthFilter)

      val result: Future[Result] = controller.check()(fakeRequest)

      status(result) mustBe Status.NO_CONTENT
    }

    "returns 404 when there is no matching user eori" in {

      val repository = new FakePrivateBetaUserRepository {
        override def getUserByEori(eori: Eori): Future[Option[User]] =
          Future.successful(None)
      }

      val controller = new PrivateBetaCheckController(Helpers.stubControllerComponents(), repository, fakeAuthFilter)

      val result: Future[Result] = controller.check()(fakeRequest)

      status(result) mustBe Status.NOT_FOUND
    }
  }
}
