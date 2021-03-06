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

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait Status

object Status {

  case object Active extends Status
  case object Inactive extends Status

  object DB {

    implicit val writesStatus: OWrites[Status] =
      status =>
        Json.obj(
          User.Constants.FieldNames.status -> status.toString
        )

    implicit val readsStatus: Reads[Status] =
      (__ \ User.Constants.FieldNames.status)
        .read[String]
        .filter(
          Seq("Active", "Inactive").contains(_)
        )
        .map {
          case x if x == "Active"   => Active
          case x if x == "Inactive" => Inactive
        }

  }

  object API {

    implicit val readsStatus: Reads[Status] =
      (__ \ "op")
        .read[String]
        .filter(_ == "replace")
        .andKeep((__ \ "path").read[String].filter(_ == "/status"))
        .andKeep(
          (__ \ "value")
            .read[String]
            .filter(
              Seq("Active", "Inactive").contains(_)
            )
            .map {
              case x if x == "Active"   => Active
              case x if x == "Inactive" => Inactive
            }
        )
  }

}
