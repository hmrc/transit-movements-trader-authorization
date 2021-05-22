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

import play.api.libs.json.{JsObject, JsValue, Json, OWrites, Writes}

trait Redact[A] {

  def redact[B <: JsValue](unredacted: JsValue): B

}

object RedactJsObject {

  def apply[A](a: A)(implicit owrites: OWrites[A], redactor: Redact[A]): JsObject =
    redactor.redact[JsObject](Json.toJsObject(a))
}

object RedactJsValue {

  def apply[A](a: A)(implicit writes: Writes[A], redactor: Redact[A]): JsValue =
    redactor.redact[JsValue](Json.toJson(a))
}
