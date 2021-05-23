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

import logging.Logging
import models.domain.{User, UserId}
import models.requests.NewUserDetails
import models.{domain, RedactedResponse, UserIdProvider}
import models.domain.Status.API._
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import reactivemongo.core.errors.ReactiveMongoException
import repositories.PrivateBetaUserRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PrivateBetaUsersController @Inject() (
  cc: ControllerComponents,
  repository: PrivateBetaUserRepository,
  userIdProvider: UserIdProvider
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def add(): Action[NewUserDetails] = Action.async(parse.json[NewUserDetails]) {
    request =>
      val user: User = User(userIdProvider.generate, request.body.name, request.body.eori, domain.Status.Active)

      repository
        .addUser(user)
        .map {
          x =>
            Created(RedactedResponse(user))
              .withHeaders(
                LOCATION -> routes.PrivateBetaUsersController.getUser(x.userId).url
              )
        }
        .recover {
          case e: ReactiveMongoException =>
            logger.error("[NewUserError] Encountered database exception", e)
            InternalServerError
          case e =>
            logger.error("[NewUserError] Encountered exception", e)
            InternalServerError
        }
  }

  def getUsers(): Action[Unit] = Action.async(parse.empty) {
    _ =>
      repository.getUsers
        .map {
          users =>
            Ok(
              Json.obj(
                "users" ->
                  users.map(RedactedResponse(_))
              )
            )
        }
        .recover {
          case e: ReactiveMongoException =>
            logger.error("[GetUsersError] Encountered database exception", e)
            InternalServerError
          case e =>
            logger.error("[GetUsersError] Encountered exception", e)
            InternalServerError
        }
  }

  def getUser(userId: UserId): Action[Unit] = Action.async(parse.empty) {
    _ =>
      repository
        .getUser(userId)
        .map {
          case Some(user) => Ok(RedactedResponse(user))
          case None =>
            logger.error("[GetUserError] Error when trying to get user")
            NotFound
        }
        .recover {
          case e: ReactiveMongoException =>
            logger.error("[GetUserError] Encountered database exception", e)
            InternalServerError
          case e =>
            logger.error("[GetUserError] Encountered exception", e)
            InternalServerError
        }
  }

  def updateUserStatus(userId: UserId): Action[domain.Status] = Action.async(parse.json[domain.Status]) {
    request =>
      repository
        .updateUserStatus(userId, request.body)
        .map {
          case Some(user) => Ok(RedactedResponse(user))
          case None       => NotFound
        }
        .recover {
          case e: ReactiveMongoException =>
            logger.error("[UpdateUserStatusError] Encountered database exception", e)
            InternalServerError
          case e =>
            logger.error("[UpdateUserStatusError] Encountered exception", e)
            InternalServerError
        }
  }

  def delete(userId: UserId): Action[Unit] = Action.async(parse.empty) {
    _ =>
      repository
        .removeUser(userId)
        .map {
          case true => NoContent
          case false =>
            logger.error("[DeleteUserError] Error when trying to delete user")
            NotFound
        }
        .recover {
          case e: ReactiveMongoException =>
            logger.error("[DeleteUserError] Encountered database exception", e)
            InternalServerError
          case e =>
            logger.error("[DeleteUserError] Encountered exception", e)
            InternalServerError
        }
  }
}
