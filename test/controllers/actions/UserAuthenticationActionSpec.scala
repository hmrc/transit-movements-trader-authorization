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
import play.api.http.Status._
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.Helpers.status
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.auth.core.retrieve._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class UserAuthenticationActionSpec extends SpecBase with DefaultAwaitTimeout {

  def okRequestFunction(request: Request[AnyContent]): Future[Result] = Future.successful(Ok)

  val enrolmentsWithoutEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "HMCE-NCTS-ORG",
        identifiers = Seq.empty,
        state = "Activated"
      )
    )
  )

  val enrolmentsWithEori: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "HMCE-NCTS-ORG",
        identifiers = Seq(
          EnrolmentIdentifier(
            "VATRegNoTURN",
            "456"
          )
        ),
        state = "Activated"
      )
    )
  )

  val enrolmentsWithEoriButNoActivated: Enrolments = Enrolments(
    Set()
  )

  val noEnrolment: Enrolments = Enrolments(
    Set(
      Enrolment(
        key = "IR-SA",
        identifiers = Seq(
          EnrolmentIdentifier(
            "UTR",
            "123"
          )
        ),
        state = "Activated"
      )
    )
  )

  def fakeAuthConnector(stubbedRetrievalResult: Future[_]): AuthConnector = new AuthConnector {

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      stubbedRetrievalResult.map(_.asInstanceOf[A])(ec)
  }

  "Auth Action" - {
    "when the user has the correct enrollments" - {
      "when the EORI is active" - {
        "must return Ok" in {

          val retrievalResult: Future[Enrolments] = Future.successful(enrolmentsWithEori)

          val authAction: UserAuthenticationAction = new DefaultUserAuthenticationAction(fakeAuthConnector(retrievalResult))

          val result: Future[Result] = authAction.invokeBlock(FakeRequest("", ""), okRequestFunction)

          status(result) mustBe OK

        }
      }

      "when the user doesn't have an active EORI" - {
        "must return 401" in {
          val retrievalResult: Future[Enrolments] = Future.successful(enrolmentsWithEoriButNoActivated)

          val authAction: UserAuthenticationAction = new DefaultUserAuthenticationAction(fakeAuthConnector(retrievalResult))
          val result: Future[Result]               = authAction.invokeBlock(FakeRequest("", ""), okRequestFunction)

          status(result) mustBe UNAUTHORIZED
        }
      }

      "when the user doesn't have an EORI" - {
        "must return 401" in {
          val retrievalResult: Future[Enrolments] = Future.successful(enrolmentsWithoutEori)

          val authAction: UserAuthenticationAction = new DefaultUserAuthenticationAction(fakeAuthConnector(retrievalResult))
          val result: Future[Result]               = authAction.invokeBlock(FakeRequest("", ""), okRequestFunction)

          status(result) mustBe UNAUTHORIZED
        }
      }

    }

    "when the user doesn't have the correct enrolment" - {
      "must return 401" in {
        val retrievalResult: Future[Enrolments]  = Future.successful(noEnrolment)
        val authAction: UserAuthenticationAction = new DefaultUserAuthenticationAction(fakeAuthConnector(retrievalResult))
        val result: Future[Result]               = authAction.invokeBlock(FakeRequest("", ""), okRequestFunction)

        status(result) mustBe UNAUTHORIZED
      }
    }

    "when the user doesn't have sufficient confidence level" ignore {}

    "when the user used an unaccepted auth provider" ignore {}

    "when the user has an unsupported affinity group" ignore {}

    "when the user has an unsupported credential role" ignore {}

    "when given enrolments without eori" ignore {
      "must redirect to unauthorised page" in {}
    }
  }
}
