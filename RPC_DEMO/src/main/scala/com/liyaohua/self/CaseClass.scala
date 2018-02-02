package com.liyaohua.self

class CaseClass {

}

case class RegisterWorker(workerId:String,memory:Int,cores:Int) extends Serializable
case class HeartBeat(workId:String) extends Serializable
case object SendHearBeat
case object CheckTimeOutWorker
case object RegisteredWorker