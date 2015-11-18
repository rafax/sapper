package com.gajdulewicz

import java.util.Date

import com.gajdulewicz.Sapper.ConcreteMapper
import org.scalatest.{FlatSpec, MustMatchers, WordSpec}

import scala.util.{Random, Try}

case class PersonApiModel(nick: Option[String], name: String, birthDate: Option[java.util.Date])

case class Person(nick: String, name: String, birthDate: java.util.Date)

case class NotAPerson(a: Int, b: Double, c: String)

class FooSpec extends FlatSpec with MustMatchers {

  val manualMapper = new ConcreteMapper[PersonApiModel, Person] {
    override def apply(from: PersonApiModel): Option[Person] = {
      Try {
        Person(from.nick.get, from.name, from.birthDate.get)
      }.toOption
    }
  }

  val mappers: Seq[ConcreteMapper[PersonApiModel, Person]] = Seq(manualMapper, ReflectiveMapper[PersonApiModel, Person] _)

  mappers.foreach(mapper => {
    mapper.getClass.getName should "map with all fields present" in {
      val p = Sapper(PersonApiModel(Some("nick"), "name", Some(new Date())))(mapper)
      p must be(defined)
    }

    it should "not map with no fields" in {
      val p = Sapper(PersonApiModel(None, null, None))(mapper)
      p must not be defined
    }

    it should "not map without one field" in {
      val p = Sapper(PersonApiModel(Some("first"), "nick", None))(mapper)
      p must not be defined
    }
  })


  "perfomance" should "be reasonable" in {
    val api = (1 to 1000000).map(f => PersonApiModel(
      Some(Random.alphanumeric.take(10).mkString),
      Random.alphanumeric.take(10).mkString,
      Some(new Date))).toList

    mappers.foreach(m => {
      val start = System.currentTimeMillis()
      api.foreach(a => Sapper(a)(m))
      val end = System.currentTimeMillis()
      println(m.getClass.getName + " " + (end - start) + "ms")
    })
  }

}