package com.gajdulewicz

import scala.reflect.ClassTag

object Sapper {
  type ConcreteMapper[TFrom, TTo] = TFrom => Option[TTo]

  def apply[TFrom, TTo](from: TFrom)(implicit mapper: ConcreteMapper[TFrom, TTo]): Option[TTo] = {
    mapper(from)
  }
}

object ReflectiveMapper {

  def apply[TF: ClassTag, TT: ClassTag](from: TF): Option[TT] = {
    val toCtor = implicitly[ClassTag[TT]].runtimeClass.getConstructors.head
    val toParams = toCtor.getParameters.map(p => (p.getName, p.getType))
    val fromFields = from.getClass.getDeclaredFields.map(f => {
      f.setAccessible(true)
      (f.getName, f.getGenericType, f.get(from))
    })
    val params: Seq[Object] = fromFields.flatMap {
      case (name, cls, Some(value)) => Some(value.asInstanceOf[Object])
      case (name, cls, None) => None
      case (name, cls, value) => Some(value.asInstanceOf[Object])
    }
    if (toParams.length != params.length)
      None
    else
      Some(toCtor.newInstance(params: _*).asInstanceOf[TT])
  }
}