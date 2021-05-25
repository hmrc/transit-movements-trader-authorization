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

import controllers.actions.UserAuthenticationAction
import models.requests.PrivateBetaCheck
import play.api.mvc.{Action, ControllerComponents}
import repositories.PrivateBetaUserRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PrivateBetaCheckController @Inject() (
  cc: ControllerComponents,
  repository: PrivateBetaUserRepository,
  userRequestAuthentication: UserAuthenticationAction
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def check(): Action[PrivateBetaCheck] = (Action andThen userRequestAuthentication).async(parse.json[PrivateBetaCheck]) {
    implicit request =>
      repository.getUserByEori(request.body.eori).flatMap {
        case Some(_) => Future.successful(NoContent)
        case None    => Future.successful(NotFound)
      }
  }
}
