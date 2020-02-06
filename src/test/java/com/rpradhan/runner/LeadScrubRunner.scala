import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class LeadScrubSimulation extends Simulation {

  private val baseUrl = "http://localhost:8080"

  private val contentType = "application/json"

  private val endPoint = "/lead-scrub/iau"

  private val approval = "{\"firstName\": \"carl\", \"lastName\": \"johnson\", \"address\": \"3337 mentone avenue\", \"city\": \"los angeles\", \"state\": \"CA\", \"zip\": \"90034\", \"country\": \"USA\", \"phone1\": \"3107178197\", \"phone2\": \"2222222222\", \"strategy\": \"TARGUS_CALL_WITH_PRIMARY\"}"

  private val reject = "{\"firstName\": \"carl\", \"lastName\": \"johnson\", \"address\": \"3337 mentone avenue\", \"city\": \"los angeles\", \"state\": \"CA\", \"zip\": \"90034\", \"country\": \"USA\", \"phone1\": \"1111111111\", \"phone2\": \"2222222222\", \"strategy\": \"TARGUS_CALL_AGAIN_ON_VERIFY_REJECT\"}"

  private val verify = "{\"firstName\": \"carl\", \"lastName\": \"johnson\", \"address\": \"3337 mentone avenue\", \"city\": \"los angeles\", \"state\": \"CA\", \"zip\": \"90034\", \"country\": \"USA\", \"phone1\": \"1111111111\", \"phone2\": \"2222222222\", \"strategy\": \"TARGUS_CALL_AGAIN_ON_VERIFY_REJECT\"}"

  private val retryApproval = "{\"firstName\": \"carl\", \"lastName\": \"johnson\", \"address\": \"3337 mentone avenue\", \"city\": \"los angeles\", \"state\": \"CA\", \"zip\": \"90034\", \"country\": \"USA\", \"phone1\": \"1111111111\", \"phone2\": \"3107178197\", \"strategy\": \"TARGUS_CALL_AGAIN_ON_VERIFY_REJECT\"}"

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(baseUrl)
    .contentTypeHeader(contentType)

  val approvalScenario: ScenarioBuilder = scenario("Approval")
    .exec(
      http("request_approval")
        .post(endPoint)
        .body(StringBody(approval)).asJson
        .check(status.is((200))))

  val rejectedScenario: ScenarioBuilder = scenario("Rejection")
    .exec(
      http("request_reject")
        .post(endPoint)
        .body(StringBody(reject)).asJson
        .check(status.is((200))))

  val verifyScenario: ScenarioBuilder = scenario("Verify")
    .exec(
      http("request_verify")
        .post(endPoint)
        .body(StringBody(verify)).asJson
        .check(status.is((200))))

  val retryApprovalScenario: ScenarioBuilder = scenario("Retry")
    .exec(
      http("request_retry")
        .post(endPoint)
        .body(StringBody(verify)).asJson
        .check(status.is((200))))

  setUp(
    approvalScenario.inject(
      constantUsersPerSec(1) during (10 seconds) randomized
    ),
    verifyScenario.inject(
      constantUsersPerSec(1) during (10 seconds) randomized
    ),
    rejectedScenario.inject(
      constantUsersPerSec(1) during (10 seconds) randomized
    ),

    retryApprovalScenario.inject(
      constantUsersPerSec(1) during (10 seconds) randomized
    ),
  ).protocols(httpProtocol)
}
