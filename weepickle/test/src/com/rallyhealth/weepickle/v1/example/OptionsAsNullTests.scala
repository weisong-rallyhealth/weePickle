package com.rallyhealth.weepickle.v0.example

import acyclic.file
import utest._
import com.rallyhealth.weepickle.v0.example.Simple.Thing

case class Opt(a: Option[String], b: Option[Int])
object Opt{
  implicit def rw: OptionPickler.ReaderWriter[Opt] = OptionPickler.macroRW
}
object OptionPickler extends com.rallyhealth.weepickle.v0.AttributeTagged {
  override implicit def OptionWriter[T: Writer]: Writer[Option[T]] =
    implicitly[Writer[T]].comap[Option[T]] {
      case None => null.asInstanceOf[T]
      case Some(x) => x
    }

  override implicit def OptionReader[T: Reader]: Reader[Option[T]] = {
    new Reader.Delegate[Any, Option[T]](implicitly[Reader[T]].map(Some(_))){
      override def visitNull(index: Int) = None
    }
  }
}
// end_ex

object OptionsAsNullTests extends TestSuite {

  import OptionPickler._
  implicit def rw: OptionPickler.ReaderWriter[Thing] = OptionPickler.macroRW
  val tests = TestSuite {
    test("nullAsNone"){

      // Quick check to ensure we didn't break anything
      test("primitive"){
        write("A String") ==> "\"A String\""
        read[String]("\"A String\"") ==> "A String"
        write(1) ==> "1"
        read[Int]("1") ==> 1
        write(Thing(1, "gg")) ==> """{"myFieldA":1,"myFieldB":"gg"}"""
        read[Thing]("""{"myFieldA":1,"myFieldB":"gg"}""") ==> Thing(1, "gg")
      }

      test("none"){
        write[None.type](None) ==> "null"
        read[None.type]("null") ==> None
      }

      test("some"){
        write(Some("abc")) ==> "\"abc\""
        read[Some[String]]("\"abc\"") ==> Some("abc")
        write(Some(1)) ==> "1"
        read[Some[Int]]("1") ==> Some(1)
        write(Some(3.14159)) ==> "3.14159"
        read[Some[Double]]("3.14159") ==> Some(3.14159)
      }

      test("option"){
        write(Option("abc")) ==> "\"abc\""
        read[Option[String]]("\"abc\"") ==> Some("abc")
        read[Option[String]]("null") ==> None
      }

      test("caseClass"){
        write(Opt(None, None)) ==> """{"a":null,"b":null}"""
        read[Opt]("""{"a":null,"b":null}""") ==> Opt(None, None)
        write(Opt(Some("abc"), Some(1))) ==> """{"a":"abc","b":1}"""
      }

      test("optionCaseClass"){
        implicit val thingReader = implicitly[Reader[Thing]]
        implicit val thingWriter = implicitly[Writer[Thing]]

        write(Opt(None, None)) ==> """{"a":null,"b":null}"""
        read[Opt]("""{"a":null,"b":null}""") ==> Opt(None, None)
        write(Opt(Some("abc"), Some(1))) ==> """{"a":"abc","b":1}"""

        write(Option(Thing(1, "gg"))) ==> """{"myFieldA":1,"myFieldB":"gg"}"""
        read[Option[Thing]]("""{"myFieldA":1,"myFieldB":"gg"}""") ==> Option(Thing(1, "gg"))
      }

      // New tests.  Work as expected.
      'customPickler {
        // Custom pickler copied from the documentation
        class CustomThing2(val i: Int, val s: String)

        object CustomThing2 {
          implicit val rw = /*weepickle.default*/ OptionPickler.readerWriter[String].bimap[CustomThing2](
            x => x.i + " " + x.s,
            str => {
              val Array(i, s) = str.split(" ", 2)
              new CustomThing2(i.toInt, s)
            }
          )
        }

        'customClass {
          write(new CustomThing2(10, "Custom")) ==> "\"10 Custom\""
          val r = read[CustomThing2]("\"10 Custom\"")
          assert(r.i == 10, r.s == "Custom")
        }

        'optCustomClass_Some {
          write(Some(new CustomThing2(10, "Custom"))) ==> "\"10 Custom\""
          val r = read[Option[CustomThing2]]("\"10 Custom\"")
          assert(r.get.i == 10, r.get.s == "Custom")
        }

        'optCustomClass_None {
          read[Option[CustomThing2]]("null") ==> None
        }

      }

      // Copied from ExampleTests
      'Js {
        import OptionPickler._   // changed from weepickle.WeePickle._
        case class Bar(i: Int, s: String)
        implicit val fooReadWrite: ReaderWriter[Bar] =
          readerWriter[com.rallyhealth.weejson.v0.Value].bimap[Bar](
            x => com.rallyhealth.weejson.v0.Arr(x.s, x.i),
            json => new Bar(json(1).num.toInt, json(0).str)
          )

        write(Bar(123, "abc")) ==> """["abc",123]"""
        read[Bar]("""["abc",123]""") ==> Bar(123, "abc")

        // New tests.  Last one fails.  Why?
        'option {
          'write {write(Some(Bar(123, "abc"))) ==> """["abc",123]"""}
          'readSome {read[Option[Bar]]("""["abc",123]""") ==> Some(Bar(123, "abc"))}
          'readNull {read[Option[Bar]]("""null""") ==> None}
        }
      }

    }
  }
}
