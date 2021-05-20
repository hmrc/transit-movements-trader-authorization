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

package departuresPrivateBeta.models

import play.api.libs.json.{Json, OWrites}

sealed trait PrivateBetaCheckResponse
object NotEnrolledInPrivateBeta extends PrivateBetaCheckResponse

object PrivateBetaCheckResponse {

  implicit val writes: OWrites[PrivateBetaCheckResponse] =
    _ match {
      case NotEnrolledInPrivateBeta =>
        Json.obj(
          "feature"             -> "departures-private-beta",
          "authorizationResult" -> "NOT_AUTHORIZED"
        )
    }
}
