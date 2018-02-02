package com.liyaohua.self

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * create by @liyaohua
  * 2018年01月30日11:22:59
  */
class Master extends Actor {

  val CHECK_INTERVAL = 15000
  val id2Worker = new mutable.HashMap[String, WorkInfo]()
  val workers = new mutable.HashSet[WorkInfo]()

  //启动master进程
  override def preStart(): Unit = {
    println("------------------MASTER 启动成功------------------")
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }


  override def receive: Receive = {
    case RegisterWorker(workerID: String, memory: Int, cores: Int) => {
      println(s"a role of worker willi register : $workerID ram:$memory MB cpu: $cores cores" )
      val workinfo = new WorkInfo(workerID, memory, cores)
      id2Worker(workerID) = workinfo
      workers += workinfo
      sender ! RegisteredWorker
    }
    case HeartBeat(workId) => {
      println(s"a hearbeat from worker workerId : $workId")
      val workinfo: WorkInfo = id2Worker(workId)
      val current = System.currentTimeMillis()
      workinfo.lastHeartBeatTime = current
    }

    case CheckTimeOutWorker => {
      println("interval msg check ")
      val current = System.currentTimeMillis()
      //过滤掉超时的worker
      val deadworker = workers.filter(w => {
        current - w.lastHeartBeatTime > CHECK_INTERVAL
      })
      deadworker.foreach(w => {
        id2Worker -= w.id
        workers -= w
      })

      println(id2Worker.size)
    }
  }
}

object Master {
  val MASTER_ACTOR_SYSTEM = "MASTERSYSTEM"
  val MASTER_ACTOR_NAME = "MASTER"


  def main(args: Array[String]): Unit = {
    val host = "172.18.163.71"
    val port = 9000

    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remot.netty.tcp.hostname= "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    val config = ConfigFactory.parseString(configStr)
    val actorSystem = ActorSystem(MASTER_ACTOR_SYSTEM, config)
    val masterActor = actorSystem.actorOf(Props[Master], MASTER_ACTOR_NAME)
  }
}

