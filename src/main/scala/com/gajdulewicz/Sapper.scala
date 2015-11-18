package com.gajdulewicz

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

object ReflectiveMapper {
  def apply[TF: ClassTag, TT: ClassTag](from: TF): MappingResult[TT] = {
    val toCtor = implicitly[ClassTag[TT]].runtimeClass.getConstructors.head
    val toParams = toCtor.getParameters.map(p => (p.getName, p.getType))
    val fromFields = from.getClass.getDeclaredFields.map(f => {
      f.setAccessible(true)
      (f.getName, f.getGenericType, f.get(from))
    })
    val (defined, notDefined) = fromFields.map {
      case (name, cls, Some(value)) => Some(value.asInstanceOf[Object])
      case (name, cls, None) => None
      case (name, cls, null) => None
      case (name, cls, value) => Some(value.asInstanceOf[Object])
    } zip fromFields.map(_._1) partition (_._1.isDefined)
    if (notDefined.nonEmpty)
      Left(notDefined.map(_._2 + " is null or not present") )
    else
    Right(toCtor.newInstance(defined.flatMap(_._1): _*).asInstanceOf[TT])
  }
}