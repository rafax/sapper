package com.gajdulewicz

import java.lang.reflect.Type

import com.gajdulewicz.Sapper.MappingResult

import scala.reflect.ClassTag

object ReflectiveMapper {
  def apply[TF: ClassTag, TT: ClassTag](from: TF): MappingResult[TT] = {
    val toCtor = implicitly[ClassTag[TT]].runtimeClass.getConstructors.head
    val toParams = toCtor.getParameters.map(p => (p.getName, p.getType))
    val fromFields = from.getClass.getDeclaredFields.map(f => {
      f.setAccessible(true)
      (f.getName, f.getGenericType, f.get(from))
    })
    val (defined, notDefined) = unpack(fromFields)
    if (notDefined.nonEmpty)
      Left(notDefined.map(_._2 + " is null or not present"))
    else
      Right(toCtor.newInstance(defined.flatMap(_._1): _*).asInstanceOf[TT])
  }

  def unpack(fromFields: Array[(String, Type, AnyRef)]): (Array[(Option[Object], String)], Array[(Option[Object], String)]) = {
    fromFields.map {
      case (name, cls, Some(value)) => Some(value.asInstanceOf[Object])
      case (name, cls, None) => None
      case (name, cls, null) => None
      case (name, cls, value) => Some(value.asInstanceOf[Object])
    } zip fromFields.map(_._1) partition (_._1.isDefined)
  }
}
