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

import models.requests.PrivateBetaCheck
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import javax.inject.{Inject, Singleton}
import models.RedactedResponse
import reactivemongo.core.errors.ReactiveMongoException
import repositories.PrivateBetaUserRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PrivateBetaCheckController @Inject()(
                                            cc: ControllerComponents,
                                            repository: PrivateBetaUserRepository
                                          )(implicit ec: ExecutionContext) extends BackendController(cc) {

  def check(): Action[PrivateBetaCheck] = Action.async(parse.json[PrivateBetaCheck]) {
    implicit request =>

      repository.getUserByEori(request.body.eori).flatMap {
        case Some(_) => Future.successful(NoContent)
        case None    => Future.successful(NotFound)
      }
  }
}
