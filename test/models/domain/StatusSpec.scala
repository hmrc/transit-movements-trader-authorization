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

import base.SpecBase
import models.domain.Status.{Active, Inactive}
import play.api.libs.json.Json

class StatusSpec extends SpecBase {

  "deserialization from Json" - {
    "from database" - {
      "when status value is Active" in {
        import models.domain.Status.DB._

        val json = Json.obj(
          "status" -> "Active"
        )

        json.as[Status] mustEqual Active

      }

      "when status value is Inactive" in {
        import models.domain.Status.DB._

        val json = Json.obj(
          "status" -> "Inactive"
        )

        json.as[Status] mustEqual Inactive
      }
    }

    "from API" - {
      "when status value is Active" in {
        import models.domain.Status.API._

        val json = Json.obj(
          "op"    -> "replace",
          "path"  -> "/status",
          "value" -> "Active"
        )

        json.as[Status] mustEqual Active
      }

      "when status value is Inactive" in {
        import models.domain.Status.API._

        val json = Json.obj(
          "op"    -> "replace",
          "path"  -> "/status",
          "value" -> "Inactive"
        )

        json.as[Status] mustEqual Inactive
      }
    }
  }
}
