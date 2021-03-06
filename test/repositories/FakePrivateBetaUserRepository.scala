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

import models.domain.{Eori, Status, User, UserId}

import scala.concurrent.Future

class FakePrivateBetaUserRepository extends PrivateBetaUserRepository {
  override def addUser(user: User): Future[User] = ???

  override def getUserByUserId(user: UserId): Future[Option[User]] = ???

  override def getUserByEori(eori: Eori): Future[Option[User]] = ???

  override def getUsers: Future[Seq[User]] = ???

  override def updateUserStatus(userId: UserId, status: Status): Future[Option[User]] = ???

  override def removeUser(userId: UserId): Future[Boolean] = ???
}
