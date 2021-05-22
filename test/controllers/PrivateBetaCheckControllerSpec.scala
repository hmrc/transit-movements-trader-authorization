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
import models.{Eori, PrivateBetaCheck}
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class PrivateBetaCheckControllerSpec extends SpecBase {

  private val fakeRequest = FakeRequest("GET", "/")
    .withBody(PrivateBetaCheck(Eori("eoriValue")))

  private val controller = new PrivateBetaCheckController(Helpers.stubControllerComponents())

  "GET /features/private-beta" - {
    "return 204" in {
      val result: Future[Result] = controller.check()(fakeRequest)

      status(result) mustBe Status.NO_CONTENT
    }
  }
}
