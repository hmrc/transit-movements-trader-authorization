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

package models

import play.api.libs.json.{JsObject, JsValue, Json, OWrites, Writes}

trait Redact[A, B <: JsValue] {

  def redact(a: A): B

}

object RedactedResponse {

  def apply[A](a: A)(implicit owrites: OWrites[A], redactor: Redact[A, JsObject]): JsObject =
    redactor.redact(a)
}

object RedactSingleValue {

  def apply[A](a: A)(implicit writes: Writes[A], redactor: Redact[A, JsValue]): JsValue =
    redactor.redact(a)
}
