package com.gajdulewicz

import java.lang.reflect.Type

import com.gajdulewicz.Sapper.MappingResult

import scala.reflect.ClassTag

object Sapper {
  type MappingError = String
  type MappingResult[TTo] = Either[Seq[MappingError], TTo]
  type ConcreteMapper[TFrom, TTo] = TFrom => MappingResult[TTo]

  def apply[TFrom, TTo](from: TFrom)(implicit mapper: ConcreteMapper[TFrom, TTo]): MappingResult[TTo] = {
    mapper(from)
  }
}

