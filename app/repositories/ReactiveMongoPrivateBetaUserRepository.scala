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

import com.google.inject.Inject
import models.domain.{Eori, Status, User, UserId}
import play.api.libs.json.Json
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteConcern
import reactivemongo.play.json.compat._

import scala.concurrent.{ExecutionContext, Future}

private[repositories] class ReactiveMongoPrivateBetaUserRepository @Inject() (collection: UsersCollection)(implicit ec: ExecutionContext)
    extends PrivateBetaUserRepository {

  override def addUser(user: User): Future[User] =
    collection().flatMap {
      _.insert(false)
        .one(Json.toJsObject(user))
        .map(
          x => x.writeErrors
        )
        .map(
          _ => user
        )
    }

  override def getUserByUserId(userId: UserId): Future[Option[User]] = {
    val query = Json.obj(
      User.Constants.FieldNames.userId -> userId.value
    )

    collection().flatMap {
      _.find(query, None)
        .one[User]
    }
  }

  override def getUserByEori(eori: Eori): Future[Option[User]] = {
    val query = Json.obj(
      User.Constants.FieldNames.eori -> eori.value
    )

    collection().flatMap {
      _.find(query, None)
        .one[User]
    }
  }

  override def getUsers: Future[Seq[User]] =
    collection().flatMap {
      _.find(Json.obj(), None)
        .cursor[User]()
        .collect[Seq](10000, Cursor.FailOnError())
    }

  override def updateUserStatus(userId: UserId, status: Status): Future[Option[User]] = {
    val selector = Json.obj(
      User.Constants.FieldNames.userId -> userId.value
    )

    val update = Json.obj(
      "$set" -> Json.obj(
        "status" -> status.toString
      )
    )
    collection().flatMap {
      _.findAndUpdate(
        selector = selector,
        update = update,
        fetchNewObject = true,
        upsert = false,
        sort = None,
        fields = Some(User.Constants.FieldNames.mandatoryFieldsFromDb),
        bypassDocumentValidation = false,
        writeConcern = WriteConcern.Default,
        maxTime = None,
        collation = None,
        arrayFilters = Seq.empty
      )
        .map(_.result[User])
    }
  }

  override def removeUser(userId: UserId): Future[Boolean] = {
    val query = Json.obj(
      User.Constants.FieldNames.userId -> userId.value
    )

    collection()
      .flatMap {
        _.delete(false)
          .one(query)
      }
      .map(_.n > 0)
  }

}
