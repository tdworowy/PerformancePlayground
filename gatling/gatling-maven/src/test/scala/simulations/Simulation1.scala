package simulations

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.language.postfixOps

class Simulation1 extends Simulation {
 val generateRandomStr: Int => String = (range:Int) => scala.util.Random.alphanumeric.take(range).mkString

 val httpProtocol: HttpProtocolBuilder = http
    .baseUrl("http://192.168.50.241:8080") 
 val scn: ScenarioBuilder = scenario("GetSimulation")
    .exec(http("Get Random String") 
      .get("/randStr")
      .check(status.is(200)))
    .pause(2)

 val scn1: ScenarioBuilder = scenario("PostSimulation")
  .exec(session => {
    val field1 = generateRandomStr(20)
    val field2 = generateRandomStr(20)
    session.set("field1", field1).set("field2", field2)
  })
    .exec(
      http("Post Random String")
      .post("/postData")
      .header("content-type", "application/json")
      .body(StringBody(session => s"""{"field1": "${session("field1").as[String]}","field2": "${session("field2").as[String]}"}""")).asJson
      .check(status.is(200)))
    .pause(2)  

    setUp( 
    scn.inject(rampUsers(10) during(30 seconds)),
    scn1.inject(constantConcurrentUsers(10) during(30 seconds))
  ).protocols(httpProtocol)
}
// TODO 'not found' errors in vscodium, metals seems to don't work correctly with gatling
// works ok in IDEA