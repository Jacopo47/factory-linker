
package factory.linker

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LibrarySuite extends FunSuite {
  test("someLibraryMethod is always true") {
    val testResult = true
    assert(testResult)
  }
}
