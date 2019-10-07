package com.knoldus.util

import java.io.{File, FileOutputStream, OutputStream}

import com.knoldus.models.Model.ImageSetMessage
import com.knoldus.util.ConnectionProvider.path
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileStatus, FileSystem, LocatedFileStatus, Path, RemoteIterator}
import org.apache.hadoop.io.{BytesWritable, SequenceFile, Text}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait HDFSConnectionFactory {
  val conf: Configuration
  val fs: FileSystem
}

object ConnectionProvider extends HDFSConnectionFactory {
  val conf = new Configuration
  System.setProperty("HADOOP_USER_NAME", ConfigConstants.hdfsUser)
  conf.set("fs.defaultFS", s"${ConfigConstants.hdfsUrl}")
  conf.set("dfs.replication", "1")
  val fs = FileSystem.get(conf)
  val key: Text = new Text()
  val value: BytesWritable = new BytesWritable()

  val path = new Path(s"${ConfigConstants.hdfsDir}${ConfigConstants.unitId}/")

  val dirPath =  s"${ConfigConstants.hdfsDir}${ConfigConstants.unitId}/"

  def getValidPath: Future[List[(String, String, String)]] = {
    val niveList =  fs.listStatus(path)
      .toList.map(_.getPath.toString)
      .map(path => {
        println("Now Iterating elements")
        (
          path,
          path.replace(s"${ConfigConstants.hdfsUrl}${ConfigConstants.hdfsDir}${ConfigConstants.unitId}/",""),
          s"${path.replace(s"${ConfigConstants.hdfsUrl}","")}/Left"
        )
      })


    Future.sequence(niveList.map{
              case (path, path2, path3) =>
          Future(fs.exists(new Path(path3)), (path, path2, path3))map(x => {
            println("--------------------now checking filter")
            x
          })
    }).map(filterList => filterList.filter(_._1).map(_._2))
  }

  def getCounterForPath(path:String, path2: String, path3: String) = Future{
    fs.listStatus(new Path(path3)).toList.map(pathStatus => {
      println(pathStatus.getPath.toString)
      pathStatus.getPath.toString
        .replace(s"$path/Left/","")
        .replace(s"$path2-","")
        .replace("-L.png","")
        .replace("-L.jpg","").toLong
    }).foldLeft(0L)((acc, nextval) => if(acc >= nextval) acc else nextval)
  }
}
