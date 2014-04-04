package org.dandelion.radiot.server

import org.scalatest.{BeforeAndAfter, Matchers}
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI
import org.eclipse.jetty.websocket.api.annotations._
import org.eclipse.jetty.websocket.api.Session
import org.scalatest.concurrent.Eventually
import scala.concurrent.duration._


class TopicTrackerServletTest extends RadiotServerSpec
with Matchers with Eventually with BeforeAndAfter {
  implicit val config = PatienceConfig(timeout = 5 seconds, interval = 0.5 seconds)
  val client = new WebSocketClient
  val socket = new TopicTrackerSocket
  val servlet = new TopicTrackerServlet

  addServlet(servlet, "/chat/*")

  before {
    servlet.connectToChat()

    client.start()
    client.connect(socket, serverUrl)

    eventually { socket should be('connected) }
  }

  after {
    client.stop()
    servlet.disconnectFromChat()
  }

  it("answers a simple request") {
    get("/chat") {
      status should equal(200)
      body should equal("Hello world!")
    }
  }

  it("receives current topic after connection is established") {
    topicShouldBe("Default topic")
  }

  it("changes a topic by POST request") {
    val newTopic = "New topic"
    post("/chat/set-topic", newTopic) {
      status should equal(200)
      topicShouldBe(newTopic)
    }
  }

  it("changes a topic by a message from the chat") {
    val newTopic = "New topic to discuss"

    sendMessageToChat(newTopic)

    topicShouldBe(newTopic)
  }


  def topicShouldBe(expected: String) {
    eventually { socket.topic should equal(expected) }
  }

  def sendMessageToChat(msg: String) {
    new JabberChat("jc-radio-t", "password") {
      connect()
      sendMessage(msg)
      disconnect()
    }
  }

  def serverUrl = localPort match {
    case Some(port) => new URI(s"ws://localhost:$port/chat/current-topic")
    case None => throw new RuntimeException("No port is specified")
  }
}


@WebSocket
class TopicTrackerSocket {
  var topic: String = ""
  private var session: Session = null

  @OnWebSocketConnect
  def onConnect(session: Session) {
    this.session = session
    session.getRemote.sendString("get")
  }

  @OnWebSocketMessage
  def onMessage(msg: String) {
    topic = msg
  }

  def isConnected = (session != null) && session.isOpen
}

