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

import java.util.UUID

import play.api.libs.json._
import play.api.mvc.PathBindable

case class UserId(value: String)

object UserId {

  def generateFromUUID(): UserId = UserId(UUID.randomUUID().toString)

  object DB {

    implicit val writesUserId: OWrites[UserId] =
      userId =>
        Json.obj(
          User.Constants.FieldNames.userId -> userId.value
        )

    implicit val readsUserId: Reads[UserId] =
      (__ \ User.Constants.FieldNames.userId)
        .read[String]
        .map(UserId(_))

  }

  implicit val pathBindableMessageId: PathBindable[UserId] = new PathBindable[UserId] {

    override def bind(key: String, value: String): Either[String, UserId] =
      Right(UserId(value))

    override def unbind(key: String, value: UserId): String =
      value.value
  }

}
