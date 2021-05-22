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

package models

import play.api.libs.json.{__, Json, OWrites, Reads}

case class Eori(value: String)

object Eori {

  object DB {

    implicit val writesEori: OWrites[Eori] =
      eori =>
        Json.obj(
          "eori" -> eori.value
        )

    implicit val readsEori: Reads[Eori] =
      (__ \ User.Constants.FieldNames.eori)
        .read[String]
        .map(Eori(_))

  }

  object API {

    implicit val readsEori: Reads[Eori] =
      DB.readsEori
  }

}
