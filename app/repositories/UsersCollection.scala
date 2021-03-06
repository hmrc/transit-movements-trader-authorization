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

package repositories

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

@Singleton
private[repositories] class UsersCollection @Inject() (mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends (() => Future[JSONCollection]) {

  val collectionName = UsersCollection.collectionName

  override def apply(): Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection](collectionName))

}

object UsersCollection {

  val collectionName: String = "users"

}
