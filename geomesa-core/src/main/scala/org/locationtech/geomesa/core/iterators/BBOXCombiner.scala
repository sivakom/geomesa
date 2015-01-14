package org.locationtech.geomesa.core.iterators

import java.util

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.io.WKTReader
import org.apache.accumulo.core.data.{Value, Key}
import org.apache.accumulo.core.iterators.Combiner
import org.locationtech.geomesa.utils.geohash.BoundingBox

import scala.collection.JavaConversions._

import BBOXCombiner._

class BBOXCombiner extends Combiner {
  override def reduce(p1: Key, p2: util.Iterator[Value]): Value = {
    println("In reduce")
    if(p2.hasNext) {
      println("p2 hasNext")
      val combinedBbox = p2.map(valueToBbox).reduce( (a, b) => BoundingBox.getCoveringBoundingBox(a, b))
      bboxToValue(combinedBbox)
    } else {
      new Value()
    }
  }
}

object BBOXCombiner {
  val wktReader = new ThreadLocal[WKTReader] {
    override def initialValue = new WKTReader()
  }

  // These two functions are inverse
  def bboxToValue(bbox: BoundingBox): Value = {
    new Value((bbox.ll.toString + ":" + bbox.ur.toString).getBytes)
  }

  def valueToBbox(value: Value): BoundingBox = {
    val wkts = value.toString.split(":")
    BoundingBox(wktReader.get.read(wkts(0)).asInstanceOf[Point], wktReader.get.read(wkts(1)).asInstanceOf[Point])
  }
}