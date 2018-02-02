package com.liyaohua.self

object Utils {
  def getSystemContainer: String = {
    val rt = Runtime.getRuntime
    val systemInfo = Kb2Mb(rt.freeMemory())+"&"+rt.availableProcessors()
    systemInfo
  }

  def Kb2Mb(a: Long): String = {
    s"${a / (1024 * 1024)}"
  }
}


