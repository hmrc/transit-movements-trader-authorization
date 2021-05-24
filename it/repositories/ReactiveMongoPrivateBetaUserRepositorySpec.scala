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

package repositories

import base.SpecBase
import models.domain
import models.domain.{Eori, User, UserId}
import models.domain.Status.{Active, Inactive}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteConcern.Default
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.compat._
import repositories.UsersCollection.collectionName

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReactiveMongoPrivateBetaUserRepositorySpec extends SpecBase with MongoSuite with BeforeAndAfterEach with GuiceOneAppPerSuite with FailOnUnindexedQueries {

  private def deleteAllDocumentsFromCollection(): Future[Unit] =
    database
      .flatMap[Unit](
        _.collection[JSONCollection](collectionName)
          .findAndRemove(Json.obj(), None, None, Default, None, None, Seq.empty)
          .map(
            _ => ()
          )
      )

  override def beforeEach(): Unit = {
    deleteAllDocumentsFromCollection().futureValue
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    deleteAllDocumentsFromCollection().futureValue
    super.afterEach()
  }

  private val repository: PrivateBetaUserRepository = app.injector.instanceOf[ReactiveMongoPrivateBetaUserRepository]

  "addUser" - {
    "must persist the user" in {
      val user = User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      repository.addUser(user).futureValue

      val selector = Json.obj(User.Constants.FieldNames.userId -> user.userId.value)

      val result = database.flatMap {
        result =>
          result.collection[JSONCollection](UsersCollection.collectionName).find(selector, None).one[User]
      }.futureValue

      result.value mustEqual user
    }
  }

  "getUserByUserId" - {
    "gets the user with the matching id" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val result = repository.getUserByUserId(user1.userId).futureValue

      result.value mustEqual user1
    }
  }

  "getUserByEori" - {
    "gets the user with the matching eori" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val result = repository.getUserByEori(user1.eori).futureValue

      result.value mustEqual user1
    }
  }

  "getUsers" - {

    "returns all users" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val result = repository.getUsers.futureValue

      result must contain theSameElementsAs Seq(user1, user2)
    }
  }

  "removeUserGroup" - {
    "removed data for a only the given user" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val result = repository.removeUser(user1.userId).futureValue

      result mustEqual true

      val user2FromDb = repository.getUserByUserId(user2.userId).futureValue

      user2FromDb.value mustEqual user2
    }
  }

  "updateUserStatus" - {
    "updates the status of the user with matching userId" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val beforeUpdate = repository.getUserByUserId(user1.userId).futureValue
      beforeUpdate.value.status mustEqual Active

      val result = repository.updateUserStatus(user1.userId, Inactive).futureValue

      result.value.status mustBe Inactive

      val user1AfterUpdate = repository.getUserByUserId(user1.userId).futureValue
      val user2AfterUpdate = repository.getUserByUserId(user2.userId).futureValue

      user1AfterUpdate.value.status mustEqual Inactive
      user2AfterUpdate.value.status mustEqual Active
    }
  }

  "removeUser" - {
    "removes the user with matching userId" in {
      val user1 = domain.User(
        UserId("id1"),
        "testGroupName",
        Eori("indivUserEori1"),
        Active
      )

      val user2 = domain.User(
        UserId("id2"),
        "testGroupName",
        Eori("indivUserEori2"),
        Active
      )

      repository.addUser(user1).futureValue
      repository.addUser(user2).futureValue

      val result = repository.removeUser(user1.userId).futureValue

      result mustEqual true

      val user1AfterRemove = repository.getUserByUserId(user1.userId).futureValue
      val user2AfterRemove = repository.getUserByUserId(user2.userId).futureValue

      user1AfterRemove must not be defined
      user2AfterRemove.value mustEqual user2
    }

  }

}
