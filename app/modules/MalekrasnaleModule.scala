package modules

import com.google.inject.AbstractModule
import daos.PostDAO
import daos.PostDAOImpl
import daos.WallDAO
import daos.WallDAOImpl
import net.codingwell.scalaguice.ScalaModule

class MalekrasnaleModule extends AbstractModule with ScalaModule {

  def configure() = {
    bind[WallDAO].to[WallDAOImpl]
    bind[PostDAO].to[PostDAOImpl]
  }
}
