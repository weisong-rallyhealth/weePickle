package com.rallyhealth.weepickle.v0

import utest._
import com.rallyhealth.weepickle.v0.WeePickle.read
import acyclic.file
import com.rallyhealth.weejson.v0.{IncompleteParseException, ParseException}
import com.rallyhealth.weepickle.v0.core.AbortException
case class Fee(i: Int, s: String)
sealed trait Fi
object Fi{
  implicit def rw2: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Fi] = com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter.merge(Fo.rw2, Fum.rw2)
  case class Fo(i: Int) extends Fi
  object Fo{
    implicit def rw2: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Fo] = com.rallyhealth.weepickle.v0.WeePickle.macroRW
  }
  case class Fum(s: String) extends Fi
  object Fum{
    implicit def rw2: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Fum] = com.rallyhealth.weepickle.v0.WeePickle.macroRW
  }
}
/**
* Generally, every failure should be a Invalid.Json or a
* InvalidData. If any assertion errors, match errors, number
* format errors or similar leak through, we've failed
*/
object FailureTests extends TestSuite {

  def tests = Tests {
//    test("test"){
//      read[com.rallyhealth.weejson.v0.Value](""" {unquoted_key: "keys must be quoted"} """)
//    }

    test("jsonFailures"){
      // Run through the test cases from the json.org validation suite,
      // skipping the ones which we don't support yet (e.g. leading zeroes,
      // extra commas) or will never support (e.g. too deep)

      val failureCases = Seq(
//        """ "A JSON payload should be an object or array, not a string." """,
        """ {"Extra value after close": true} "misplaced quoted value" """,
        """ {"Illegal expression": 1 + 2} """,
        """ {"Illegal invocation": alert()} """,
        """ {"Numbers cannot have leading zeroes": 013} """,
        """ {"Numbers cannot be hex": 0x14} """,
        """ ["Illegal backslash escape: \x15"] """,
        """ [\naked] """,
        """ ["Illegal backslash escape: \017"] """,
//        """ [[[[[[[[[[[[[[[[[[[["Too deep"]]]]]]]]]]]]]]]]]]]] """,
        """ {"Missing colon" null} """,

        """ {"Double colon":: null} """,
        """ {"Comma instead of colon", null} """,
        """ ["Colon instead of comma": false] """,
        """ ["Bad value", truth] """,
        """ ['single quote'] """,
        """ ["	tab	character	in	string	"] """,
        """ ["tab\   character\   in\  string\  "] """,
        """ ["line
          break"] """,
        """ ["line\
          break"] """,
        """ [0e] """,
        """ {unquoted_key: "keys must be quoted"} """,
        """ [0e+-1] """,

        """ ["mismatch"} """,
        """ ["extra comma",] """,
        """ ["double extra comma",,] """,
        """ [   , "<-- missing value"] """,
        """ ["Comma after the close"], """,
        """ ["Extra close"]] """,
        """ {"Extra comma": true,} """
      ).map(_.trim())
      val res =
        for(failureCase <- failureCases)
        yield try {
          intercept[ParseException] { read[com.rallyhealth.weejson.v0.Value](failureCase) }
          None
        }catch{
          case _:Throwable =>
          Some(failureCase)
        }

      val nonFailures = res.flatten
      assert(nonFailures.isEmpty)
      intercept[IncompleteParseException]{read[com.rallyhealth.weejson.v0.Value](""" {"Comma instead if closing brace": true, """)}
      intercept[IncompleteParseException]{read[com.rallyhealth.weejson.v0.Value](""" ["Unclosed array" """)}
    }

    test("facadeFailures"){
      def assertErrorMsgDefault[T: com.rallyhealth.weepickle.v0.WeePickle.Reader](s: String, msgs: String*) = {
        val err = intercept[AbortException] { com.rallyhealth.weepickle.v0.WeePickle.read[T](s) }
        for (msg <- msgs) assert(err.getMessage.contains(msg))
        err
      }
      test("caseClass"){
        // Separate this guy out because the read macro and
        // the intercept macro play badly with each other

        test("invalidTag"){
          test - assertErrorMsgDefault[Fi.Fo]("""{"$type": "omg"}]""", "invalid tag for tagged object: omg at index 1")
          test - assertErrorMsgDefault[Fi]("""{"$type": "omg"}]""", "invalid tag for tagged object: omg at index 1")
        }

        test("taggedInvalidBody"){
          test - assertErrorMsgDefault[Fi.Fo]("""{"$type": "com.rallyhealth.weepickle.v0.Fi.Fo", "i": true, "z": null}""", "expected number got boolean at index 53")
          test - assertErrorMsgDefault[Fi]("""{"$type": "com.rallyhealth.weepickle.v0.Fi.Fo", "i": true, "z": null}""", "expected number got boolean at index 53")
        }
      }
    }
    test("compileErrors"){
      compileError("write(new Object)")
      compileError("""read[Object]("")""")
//      compileError("""read[Array[Object]]("")""").msg
      // Make sure this doesn't hang the compiler =/
      compileError("implicitly[com.rallyhealth.weepickle.v0.WeePickle.Reader[Nothing]]")
    }
    test("expWholeNumbers"){
      com.rallyhealth.weepickle.v0.WeePickle.read[Byte]("0e0") ==> 0.toByte
      com.rallyhealth.weepickle.v0.WeePickle.read[Short]("0e0") ==> 0
      com.rallyhealth.weepickle.v0.WeePickle.read[Char]("0e0") ==> 0.toChar
      com.rallyhealth.weepickle.v0.WeePickle.read[Int]("0e0") ==> 0
      com.rallyhealth.weepickle.v0.WeePickle.read[Long]("0e0") ==> 0

      com.rallyhealth.weepickle.v0.WeePickle.read[Byte]("10e1") ==> 100
      com.rallyhealth.weepickle.v0.WeePickle.read[Short]("10e1") ==> 100
      com.rallyhealth.weepickle.v0.WeePickle.read[Char]("10e1") ==> 100
      com.rallyhealth.weepickle.v0.WeePickle.read[Int]("10e1") ==> 100
      com.rallyhealth.weepickle.v0.WeePickle.read[Long]("10e1") ==> 100

      com.rallyhealth.weepickle.v0.WeePickle.read[Byte]("10.1e1") ==> 101
      com.rallyhealth.weepickle.v0.WeePickle.read[Short]("10.1e1") ==> 101
      com.rallyhealth.weepickle.v0.WeePickle.read[Char]("10.1e1") ==> 101
      com.rallyhealth.weepickle.v0.WeePickle.read[Int]("10.1e1") ==> 101
      com.rallyhealth.weepickle.v0.WeePickle.read[Long]("10.1e1") ==> 101

      // Not supporting these for now, since AFAIK none of the
      // JSON serializers I know generate numbers of this form
      //      com.rallyhealth.weepickle.v0.WeePickle.read[Byte]("10e-1") ==> 1
      //      com.rallyhealth.weepickle.v0.WeePickle.read[Short]("10e-1") ==> 1
      //      com.rallyhealth.weepickle.v0.WeePickle.read[Char]("10e-1") ==> 1
      //      com.rallyhealth.weepickle.v0.WeePickle.read[Int]("10e-1") ==> 1
      //      com.rallyhealth.weepickle.v0.WeePickle.read[Long]("10e-1") ==> 1
    }
    test("tooManyFields"){
      val b63 = Big63(
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21,
        22, 23, 24, 25, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49,
        50, 51, 52, 53, 54, 55, 56,
        57, 58, 59, 60, 61, 62
      )
      val b64 = Big64(
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21,
        22, 23, 24, 25, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49,
        50, 51, 52, 53, 54, 55, 56,
        57, 58, 59, 60, 61, 62, 63
      )
      val b65 = Big65(
        0, 1, 2, 3, 4, 5, 6, 7,
        8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21,
        22, 23, 24, 25, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42,
        43, 44, 45, 46, 47, 48, 49,
        50, 51, 52, 53, 54, 55, 56,
        57, 58, 59, 60, 61, 62, 63,
        64
      )
      implicit val b63rw: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Big63] = com.rallyhealth.weepickle.v0.WeePickle.macroRW
      implicit val b64rw: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Big64] = com.rallyhealth.weepickle.v0.WeePickle.macroRW
      val written63 = com.rallyhealth.weepickle.v0.WeePickle.write(b63)
      assert(com.rallyhealth.weepickle.v0.WeePickle.read[Big63](written63) == b63)
      val written64 = com.rallyhealth.weepickle.v0.WeePickle.write(b64)
      assert(com.rallyhealth.weepickle.v0.WeePickle.read[Big64](written64) == b64)
      val err = compileError("{implicit val b64rw: com.rallyhealth.weepickle.v0.WeePickle.ReaderWriter[Big65] = com.rallyhealth.weepickle.v0.WeePickle.macroRW}")
      assert(err.msg.contains("weepickle does not support serializing case classes with >64 fields"))
    }
  }
}

case class Big63(_0: Byte, _1: Byte, _2: Byte, _3: Byte, _4: Byte, _5: Byte, _6: Byte, _7: Byte,
  _8: Byte, _9: Byte, _10: Byte, _11: Byte, _12: Byte, _13: Byte, _14: Byte,
  _15: Byte, _16: Byte, _17: Byte, _18: Byte, _19: Byte, _20: Byte, _21: Byte,
  _22: Byte, _23: Byte, _24: Byte, _25: Byte, _26: Byte, _27: Byte, _28: Byte,
  _29: Byte, _30: Byte, _31: Byte, _32: Byte, _33: Byte, _34: Byte, _35: Byte,
  _36: Byte, _37: Byte, _38: Byte, _39: Byte, _40: Byte, _41: Byte, _42: Byte,
  _43: Byte, _44: Byte, _45: Byte, _46: Byte, _47: Byte, _48: Byte, _49: Byte,
  _50: Byte, _51: Byte, _52: Byte, _53: Byte, _54: Byte, _55: Byte, _56: Byte,
  _57: Byte, _58: Byte, _59: Byte, _60: Byte, _61: Byte, _62: Byte)
case class Big64(_0: Byte, _1: Byte, _2: Byte, _3: Byte, _4: Byte, _5: Byte, _6: Byte, _7: Byte,
  _8: Byte, _9: Byte, _10: Byte, _11: Byte, _12: Byte, _13: Byte, _14: Byte,
  _15: Byte, _16: Byte, _17: Byte, _18: Byte, _19: Byte, _20: Byte, _21: Byte,
  _22: Byte, _23: Byte, _24: Byte, _25: Byte, _26: Byte, _27: Byte, _28: Byte,
  _29: Byte, _30: Byte, _31: Byte, _32: Byte, _33: Byte, _34: Byte, _35: Byte,
  _36: Byte, _37: Byte, _38: Byte, _39: Byte, _40: Byte, _41: Byte, _42: Byte,
  _43: Byte, _44: Byte, _45: Byte, _46: Byte, _47: Byte, _48: Byte, _49: Byte,
  _50: Byte, _51: Byte, _52: Byte, _53: Byte, _54: Byte, _55: Byte, _56: Byte,
  _57: Byte, _58: Byte, _59: Byte, _60: Byte, _61: Byte, _62: Byte, _63: Byte)
case class Big65(_0: Byte, _1: Byte, _2: Byte, _3: Byte, _4: Byte, _5: Byte, _6: Byte, _7: Byte,
  _8: Byte, _9: Byte, _10: Byte, _11: Byte, _12: Byte, _13: Byte, _14: Byte,
  _15: Byte, _16: Byte, _17: Byte, _18: Byte, _19: Byte, _20: Byte, _21: Byte,
  _22: Byte, _23: Byte, _24: Byte, _25: Byte, _26: Byte, _27: Byte, _28: Byte,
  _29: Byte, _30: Byte, _31: Byte, _32: Byte, _33: Byte, _34: Byte, _35: Byte,
  _36: Byte, _37: Byte, _38: Byte, _39: Byte, _40: Byte, _41: Byte, _42: Byte,
  _43: Byte, _44: Byte, _45: Byte, _46: Byte, _47: Byte, _48: Byte, _49: Byte,
  _50: Byte, _51: Byte, _52: Byte, _53: Byte, _54: Byte, _55: Byte, _56: Byte,
  _57: Byte, _58: Byte, _59: Byte, _60: Byte, _61: Byte, _62: Byte, _63: Byte,
  _64: Byte)
