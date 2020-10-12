# AwesomeMQ

**Table of Contents**

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Architecture](#architecture)
- [Communication Layer](#communication-layer)
- [Communication Primitives](#communication-primitives)
- [Fault Tolerance](#fault-tolerance)
- [Contact](#contact)

# Introduction

AwesomeMQ is a lightweight distributed object layer on top of a Message Queue middleware compatible with the AMQP protocol. It provides 1-1 (sync, async) and 1-to-many (multi) invocation abstractions on top of the messaging middleware.

# Requirements

- Java 1.6 or newer
- RabbitMQ

# Architecture

Objects in AwesomeMQ receive method calls as messages in incoming queues, and then reply the results (if necessary) to clients in their response queues. As we can see in the figure shown below, AwesomeMQ creates a lightweight invocation layer on top of queues. When a client object invokes a method in a remote object, the message will be redirected to the appropriate queue thanks to the client stub. One or more servers can receive method invocations in an object pool.

![Arch](/src/main/resources/architecture.png)

There are two kind of invocation abstractions: unicast and multicast. Unicast remote invocations (client1, client2) are processed by just one server in the pool. This happens because all skeletons in the same object pool will consume messages from the same queue. In this case, the queue will load balance messages between the existing consumers.

If the client performs a multicast invocation (client 3), the client stub will send the message to a multi fanout that will instead redirect this message to all server queues. All servers in the pool will then process this invocation.

# Communication Layer

Our major aim is to create a minimalist communication layer delegating complex communications to the messaging services. The programming model must be simple and it must completely hide queue and message management to developers. Our middleware avoids any stub compilation or preprocessing phase thanks to the use of dynamic stubs.
Although we are inspired in Java RMI, we decided to create our own naming service and method decorators in order to simplify the overall communication model.

We delegate as much responsibilities as we can to the MQ broker. In this line, we do not even provide a centralized Naming registry for binding and locating remote objects. Our omq.Broker interface provides similar bind and lookup methods than RMI Naming but instead of storing them on a single server, we just interface with the Message Broker. Let us explain the mappings used by our omq.Broker class:

- **Broker.bind("object_id", new Server())** registers a remote object with an specific identifier.The remote object will consume method requests from the incoming queue with that routing name.

- **Broker.lookup("object_id", Server.class)** will create a Proxy object for class Server.class that will subscribe to its Response QUeue and will send messages to the objects bound to a request queue with "object_id".

Note that binding more than one server in the same identifier also means that they will balance the load from clients. MQ Brokers already provide automatic load balancing of messages to the different consumers of the same Queue. This help us to scale the service by adding more servers to handle the load. In this case, there is
no need to modify client stubs and they do not need to be aware of changes in the pool of servers offering a service.

# Communication Primitives

We offer three main invocation abstractions: asynchronous, synchronous,and multi calls:

- @AsyncMethod: This is an asynchronous non-blocking oneway invocation where the client publishes a message in the target object request Queue. The client won't be notified if the message was handled correctly.

- @SyncMethod: This is a synchronous blocking remote call where the client publishes a message in the target object request Queue, blocking until a response is received in its own client response queue. This call can be configured with a timeout and a number of retries to trigger the exception if the result does not arrive.

- @MultiMethod: This is a one-to-many invocation from one client to many servers. This call can also be combined with @AsyncMethod or @SyncMethod. The former produces a non-blocking multiple invocation to many servers, whereas
  the latter produces a blocking multiple invocation that collects the results received from many servers in a determined timeout.

# Fault Tolerance

Unlike RMI or ElasticRMI where objects reside in main memory and any crash could lead to receive no response, AwesomeMQ does not lose any information piece. For instance, if a remote object falls during a remote operation, this operation will be dispatched to another server instance. In this way, no remote invocations can be lost. This happens because every message sent to a remote object will be stored in the queue system until the object sends an ACK stating that the operation has finished. This occurs even if the operation is asynchronous. By using this approach, the message system can also know which instances are busy or not to balance the load.
