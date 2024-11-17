import org.scalacheck._
import Prop.forAll
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import message.gRPCtest.{ConnectionGrpc, TestRequest, TestResponse}
import io.grpc.{ManagedChannelBuilder, ServerBuilder}
import scala.concurrent.ExecutionContext

object gRPCTestSpecification extends Properties("gRPC") {

  // Test property to check if gRPC is installed and working
  property("Does gRPC installed correctly") = forAll { _: Unit =>
    // Set up a simple gRPC server
    val executionContext: ExecutionContext = ExecutionContext.global
    val server = ServerBuilder
      .forPort(50051)
      .addService(ConnectionGrpc.bindService(new TestServerImpl, executionContext))
      .build()
      .start()

    // Set up a gRPC client
    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build()
    val stub = ConnectionGrpc.stub(channel)

    // Send a simple request and wait for the response
    val request = TestRequest(message = "Test gRPC")
    val responseFuture: Future[TestResponse] = stub.testMethod(request)
    val response = Await.result(responseFuture, 5.seconds)

    // Shutdown the server and client
    server.shutdown()
    channel.shutdown()

    // Check if the response is correct
    response.reply == "Response to: Test gRPC"
  }
}

// Simple implementation of the gRPC service for the test
class TestServerImpl extends ConnectionGrpc.Connection {
  override def testMethod(request: TestRequest): Future[TestResponse] = {
    val response = TestResponse(reply = s"Response to: ${request.message}")
    Future.successful(response)
  }
}
