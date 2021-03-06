package com.gajdulewicz

import java.util.Date

import com.gajdulewicz.Sapper.MappingResult
import org.scalatest.{FlatSpec, MustMatchers}

import scala.util.Random

case class PersonApiModel(nick: Option[String], name: String, birthDate: Option[java.util.Date])

case class Person(nick: String, name: String, birthDate: java.util.Date)

case class NotAPerson(a: Int, b: Double, c: String)

class SapperSpec extends FlatSpec with MustMatchers {

  val manualMapper = new Mapper[PersonApiModel, Person] {
    override def map(from: PersonApiModel): MappingResult[Person] = {
      val errors = Seq(
        if (from.nick.isEmpty) Some("Nick is not defined") else None,
        if (from.name == null) Some("name is not defined") else None,
        if (from.birthDate.isEmpty) Some("birthDate is not defined") else None
      ).flatten
      if (errors.isEmpty) Right(Person(from.nick.get, from.name, from.birthDate.get))
      else Left(errors)
    }
  }

  val mappers = Map("manual" -> manualMapper.map _, "reflective" -> ReflectiveMapper[PersonApiModel, Person] _)

  mappers.foreach {
    case (name, mapper) => {
      name should "map with all fields present" in {
        val p = mapper(PersonApiModel(Some("nick"), "name", Some(new Date())))
        p.isRight must be(true)
      }

      it should "not map with no fields" in {
        val p = mapper(PersonApiModel(None, null, None))
        p.isLeft must be(true)
        p.left.get must not be empty
      }

      it should "not map without one field" in {
        val p = mapper(PersonApiModel(Some("first"), "nick", None))
        p.isLeft must be(true)
        p.left.get must not be empty
      }

      it should "return errors for each field that is not present" in {
        val p = mapper(PersonApiModel(Some("first"), null, None))
        p.isLeft must be(true)
        p.left.get must not be empty
        p.left.get.size must be(2)
        println(p.left.get)
      }
    }
  }


  "perfomance" should "be reasonable" in {
    val api = (1 to 100000).map(f => PersonApiModel(
      Some(Random.alphanumeric.take(10).mkString),
      Random.alphanumeric.take(10).mkString,
      Some(new Date))).toList

    mappers.foreach {
      case (n, m) => {
        val start = System.currentTimeMillis()
        api.foreach(a => m(a))
        val end = System.currentTimeMillis()
        println(n + " " + (end - start) + "ms")
      }
    }
  }
}