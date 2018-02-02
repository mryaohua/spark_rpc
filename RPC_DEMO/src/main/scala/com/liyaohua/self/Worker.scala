package com.liyaohua.self

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * create by @liyaohua
  * 2018年01月30日11:23:15
  */
class Worker(val host: String, val port: Int, val meory: Int, val cores: Int) extends Actor {
  val workId = UUID.randomUUID().toString
  val HEARTBEAT_INTERVAL = 10000

  var masterRef: ActorSelection = _

  override def preStart(): Unit = {
    masterRef = context.actorSelection("akka.tcp://MASTERSYSTEM@172.18.163.71:9000/user/MASTER")
    println("------------------WORKER 启动成功------------------")
    masterRef ! RegisterWorker(workId, meory, cores)
  }

  override def receive: Receive = {
    case RegisteredWorker => {
      println("worker -> master 注册成功！ 开始发送心跳~")
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, HEARTBEAT_INTERVAL millis, self, SendHearBeat)
    }
    case SendHearBeat => {
      masterRef ! HeartBeat(workId)
    }
  }
}

object Worker {

  val WORKER_ACTOR_SYSTEM = "WORKERSTSTEM"
  val WORKER_ACTOR_NAME = "WORKER"

  def main(args: Array[String]): Unit = {
    val workerHost = "172.18.163.71"
    val workerPort = 9010
    val masterHost = "172.18.163.71"
    val masterPort = 9000

    val systemInfo = Utils.getSystemContainer.split("&")
    val workerMemy = systemInfo(0).toInt
    val workerCore = systemInfo(1).toInt

    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$workerHost"
         |akka.remote.netty.tcp.port = "$workerPort"
      """.stripMargin

    val config = ConfigFactory.parseString(configStr)

    val actorSystem = ActorSystem(WORKER_ACTOR_SYSTEM, config)

    actorSystem.actorOf(Props(new Worker(masterHost, masterPort, workerMemy, workerCore)), WORKER_ACTOR_NAME)

  }
}
