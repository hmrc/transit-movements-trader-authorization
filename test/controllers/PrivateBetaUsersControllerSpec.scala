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
import models.domain.{Eori, Status, User, UserId}
import models.requests.NewUserDetails
import models.{FakeUserIdProvider, UserIdProvider}
import play.api.http.{Status => HttpStatus}
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import reactivemongo.api.commands.LastError
import repositories.FakePrivateBetaUserRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrivateBetaUsersControllerSpec extends SpecBase {

  val unimplementedUserIdProvider = new UserIdProvider {
    override def generate: UserId = ???
  }

  def expectedResponseJsonForUser(user: User): JsObject =
    Json.obj(
      User.Constants.FieldNames.userId -> user.userId.value,
      User.Constants.FieldNames.name   -> user.name,
      User.Constants.FieldNames.status -> user.status.toString
    )

  "add" - {

    val newUserRequest: FakeRequest[NewUserDetails] = FakeRequest("POST", routes.PrivateBetaUsersController.add().url)
      .withBody(NewUserDetails("userName", Eori("userEori")))

    val user = User(UserId("userId"), "userName", Eori("userName"), Status.Active)

    val userIdProvider = new FakeUserIdProvider(user.userId)

    "returns 201 when a user has been added" in {

      val repository = new FakePrivateBetaUserRepository {
        override def addUser(user: User): Future[User] = Future.successful(user)
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, userIdProvider)

      val result: Future[Result] = controller.add()(newUserRequest)

      status(result) mustEqual HttpStatus.CREATED
      headers(result).get("Location").value mustEqual routes.PrivateBetaUsersController.getUser(user.userId).url
      contentAsJson(result) mustEqual expectedResponseJsonForUser(user)
    }

    "returns 500 when repository fails to add user" in {

      val repository = new FakePrivateBetaUserRepository {
        override def addUser(user: User): Future[User] = Future.failed(LastError(false, None, None, None, 0, None, false, None, None, false, None, None))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, userIdProvider)

      val result: Future[Result] = controller.add()(newUserRequest)

      status(result) mustEqual HttpStatus.INTERNAL_SERVER_ERROR
    }

  }

  "getUsers" - {
    val user1 = User(UserId("userId1"), "userName1", Eori("userEori1"), Status.Active)
    val user2 = User(UserId("userId2"), "userName2", Eori("userEori2"), Status.Active)
    val getUsersRequest: FakeRequest[Unit] =
      FakeRequest("GET", routes.PrivateBetaUsersController.getUsers().url)
        .withBody(())

    "returns 200 when there is a matching user" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUsers: Future[Seq[User]] = Future.successful(Seq(user1, user2))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.getUsers()(getUsersRequest)

      status(result) mustEqual HttpStatus.OK
      contentAsJson(result) mustEqual Json.obj(
        "users" -> Seq(expectedResponseJsonForUser(user1), expectedResponseJsonForUser(user2))
      )
    }

    "returns 200 when there are no matching users" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUsers: Future[Seq[User]] = Future.successful(Seq.empty[User])
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.getUsers()(getUsersRequest)

      status(result) mustEqual HttpStatus.OK
      contentAsJson(result) mustEqual Json.obj(
        "users" -> Json.arr()
      )
    }

    "returns 500 when repository call fails" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUsers: Future[Seq[User]] = Future.failed(new Exception("DB failure"))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result: Future[Result] = controller.getUsers()(getUsersRequest)

      status(result) mustEqual HttpStatus.INTERNAL_SERVER_ERROR
    }
  }

  "getUser" - {
    val user = User(UserId("userId"), "userName", Eori("userEori"), Status.Active)
    val getUserRequest: FakeRequest[Unit] =
      FakeRequest("GET", routes.PrivateBetaUsersController.getUser(user.userId).url)
        .withBody(())

    "returns 200 when there is a matching user" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUserByUserId(userId: UserId): Future[Option[User]] = Future.successful(Some(user))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.getUser(user.userId)(getUserRequest)

      status(result) mustEqual HttpStatus.OK
      contentAsJson(result) mustEqual expectedResponseJsonForUser(user)
    }

    "returns 404 when there are no matching users" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUserByUserId(userId: UserId): Future[Option[User]] = Future.successful(None)
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.getUser(UserId("abc"))(getUserRequest)

      status(result) mustEqual HttpStatus.NOT_FOUND
    }

    "returns 500 when repository call fails" in {
      val repository = new FakePrivateBetaUserRepository {
        override def getUserByUserId(userId: UserId): Future[Option[User]] = Future.failed(new Exception("DB failure"))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result: Future[Result] = controller.getUser(user.userId)(getUserRequest)

      status(result) mustEqual HttpStatus.INTERNAL_SERVER_ERROR
    }
  }

  "updateUserStatus" - {

    val userId = UserId("one")
    val user   = User(userId, "userName", Eori("userEori"), Status.Active)
    val updateUserStatusRequest: FakeRequest[Status] =
      FakeRequest("PATCH", routes.PrivateBetaUsersController.updateUserStatus(userId).url)
        .withBody(Status.Inactive)

    "returns 200 when there is a matching user that has been deleted" in {
      val repository = new FakePrivateBetaUserRepository {
        override def updateUserStatus(userId: UserId, status: Status): Future[Option[User]] = Future.successful(Some(user.copy(status = Status.Inactive)))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.updateUserStatus(userId)(updateUserStatusRequest)

      status(result) mustEqual HttpStatus.OK
      contentAsJson(result) mustEqual expectedResponseJsonForUser(user.copy(status = Status.Inactive))

    }

    "returns 404 when there are no matching users" in {
      val repository = new FakePrivateBetaUserRepository {
        override def updateUserStatus(userId: UserId, status: Status): Future[Option[User]] = Future.successful(None)
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.updateUserStatus(UserId("abc"))(updateUserStatusRequest)

      status(result) mustEqual HttpStatus.NOT_FOUND
    }

    "returns 500 when repository fails to add user" in {
      val repository = new FakePrivateBetaUserRepository {
        override def updateUserStatus(userId: UserId, status: Status): Future[Option[User]] = Future.failed(new Exception("DB failure"))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result: Future[Result] = controller.updateUserStatus(userId)(updateUserStatusRequest)

      status(result) mustEqual HttpStatus.INTERNAL_SERVER_ERROR
    }

  }

  "remove" - {

    val userId = UserId("one")
    val deleteRequest: FakeRequest[Unit] =
      FakeRequest("DELETE", routes.PrivateBetaUsersController.delete(userId).url)
        .withBody(())

    "returns 204 when there is a matching user that has been deleted" in {
      val repository = new FakePrivateBetaUserRepository {
        override def removeUser(userId: UserId): Future[Boolean] = Future.successful(true)
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.delete(userId)(deleteRequest)

      status(result) mustEqual HttpStatus.NO_CONTENT
    }

    "returns 404 when there are no matching users" in {
      val repository = new FakePrivateBetaUserRepository {
        override def removeUser(userId: UserId): Future[Boolean] = Future.successful(false)
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result = controller.delete(UserId("abc"))(deleteRequest)

      status(result) mustEqual HttpStatus.NOT_FOUND
    }

    "returns 500 when repository fails to add user" in {
      val repository = new FakePrivateBetaUserRepository {
        override def removeUser(userId: UserId): Future[Boolean] = Future.failed(new Exception("DB failure"))
      }

      val controller = new PrivateBetaUsersController(Helpers.stubControllerComponents(), repository, unimplementedUserIdProvider)

      val result: Future[Result] = controller.delete(userId)(deleteRequest)

      status(result) mustEqual HttpStatus.INTERNAL_SERVER_ERROR
    }

  }

}
