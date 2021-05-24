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

import logging.Logging
import models.domain.User
import reactivemongo.api.indexes.IndexType

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
private[repositories] class DefaultPrivateBetaUserCollectionIndexManager @Inject() (
  collection: UsersCollection
)(implicit ec: ExecutionContext)
    extends PrivateBetaUserCollectionIndexManager
    with Logging {

  private val userIdIndex = SimpleMongoIndexConfig(
    key = Seq(User.Constants.FieldNames.userId -> IndexType.Ascending),
    name = Some("userId-index"),
    unique = true
  )

  private val eoriIndex = SimpleMongoIndexConfig(
    key = Seq(User.Constants.FieldNames.eori -> IndexType.Ascending),
    name = Some("eori-index"),
    unique = true
  )

  val started: Future[Unit] =
    (for {
      coll              <- collection()
      userIdIndexResult <- coll.indexesManager.ensure(userIdIndex)
      eoriIndexResult   <- coll.indexesManager.ensure(eoriIndex)
    } yield {
      logger.info(IndexLogMessages.indexManagerResultLogMessage(collection.collectionName, userIdIndex.name.get, userIdIndexResult))
      logger.info(IndexLogMessages.indexManagerResultLogMessage(collection.collectionName, eoriIndex.name.get, eoriIndexResult))
      ()
    }).recover {
      case e: Throwable =>
        val message = IndexLogMessages.indexManagerFailedKey(collection.collectionName) + " failed with exception"
        logger.error(message, e)
        throw e
    }

}

private[repositories] trait PrivateBetaUserCollectionIndexManager {

  def started: Future[Unit]

}
