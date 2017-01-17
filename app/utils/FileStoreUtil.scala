package utils

import java.security.MessageDigest

object FileStoreUtil {

  /** Generate SHA1 hash for given `x`.
   */
  def sha1(x: String): String = {
    MessageDigest.getInstance("SHA-1").digest(x.getBytes)
      .map("%02x".format(_)).mkString
  }

  /** Generate a unique filename based on `salt` and `filename`.
    */
  def genUniqueFileName(filename: String, salt: String = ""): String = {
    sha1(salt + filename)
  }
}
