/*
 * Copyright 2022 HM Revenue & Customs
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

package models.domain

import Status.DB._
import models.{Redact, RedactSingleValue, RedactedResponse}
import models.domain.UserId.DB._
import models.domain.Eori.DB._
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class User(
  userId: UserId,
  name: String,
  eori: Eori,
  status: Status
)

object User {

  object Constants {

    object FieldNames {
      val userId = "userId"
      val name   = "name"
      val eori   = "eori"
      val status = "status"

      val mandatoryFieldsFromDb: JsObject =
        Json.obj(
          userId -> 1,
          name   -> 1,
          eori   -> 1,
          status -> 1
        )
    }

  }

  implicit val readsUser: Reads[User] =
    (
      __.read[UserId] and
        (__ \ Constants.FieldNames.name).read[String] and
        __.read[Eori] and
        __.read[Status]
    )(User.apply _)

  implicit val writesUser: OWrites[User] =
    (
      __.write[UserId] and
        (__ \ Constants.FieldNames.name).write[String] and
        __.write[Eori] and
        __.write[Status]
    )(unlift(User.unapply))

  implicit val redactUser: Redact[User, JsObject] =
    user =>
      Json.obj(
        "userId" -> user.userId.value,
        "name"   -> user.name,
        "status" -> user.status.toString
      )

  implicit val redactUsers: Redact[Seq[User], JsObject] =
    users =>
      Json.obj(
        "users" ->
          users.map(RedactedResponse(_))
      )
}
