package agora

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class ConsultationSimulation : Simulation() {

    private val randomFcmTokenFeeder = generateSequence {
        mapOf(
            "FCM_TOKEN" to java.util.UUID.randomUUID().toString(),
            "JWT_TOKEN" to "<ActualJwtToken>",
            // Dev
//            "CONSULTATION_ID" to "f5fd9c1d-6583-494c-8b0f-78129d6a0382",
            // Prod
            "CONSULTATION_ID" to "c342e83e-0b5a-11ee-be56-0242ac120002",
        )
    }.iterator()

    private val httpProtocol = http
        .baseUrl("https://agora-prod.osc-secnum-fr1.scalingo.io")
        .acceptHeader("*/*;q=0.8")
        .acceptEncodingHeader("gzip, deflate, br")
        .acceptLanguageHeader("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
        .userAgentHeader("Tests Gatling")
//        .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/113.0")

//    private val sendFcmToken = exec(
//        http("register_fcm_token")
//            .post("/signup")
//            .headers(
//                mapOf(
//                    "fcmToken" to "#{FCM_TOKEN}",
//                    "platform" to "web",
//                    "versionCode" to "1",
//                )
//            )
//            .check(status().shouldBe(200))
////            .check(jsonPath("$.loginToken").saveAs("loginToken"))
////            .check(jsonPath("$.JWT_TOKEN").saveAs("JWT_TOKEN"))
//    ).pause(1)

//    private val login = exec(
//        http("login_to_application")
//            .post("/login")
//            .headers(
//                mapOf(
//                    "fcmToken" to "#{FCM_TOKEN}",
//                    "platform" to "web",
//                    "versionCode" to "1",
//                )
//            )
//            .body(StringBody("#{loginToken}"))
//            .check(status().shouldBe(200))
//            .check(jsonPath("$.JWT_TOKEN").saveAs("JWT_TOKEN"))
//    ).pause(1)

    private val loadConsultations = exec(
        http("load_consultations")
            .get("/consultations")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(5)

    private val loadOneConsultation = exec(
        http("load_one_consultation")
            .get("/consultations/#{CONSULTATION_ID}")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
//            .check(jsonPath("$.coverUrl").saveAs("coverUrl"))
    ).pause(5)

    private val loadConsultationQuestions = exec(
        http("load_consultation_questions")
            .get("/consultations/#{CONSULTATION_ID}/questions")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(10)

    private val sendConsultationResponse = exec(
        http("send_consultation_response")
            .post("/consultations/#{CONSULTATION_ID}/responses")
            .headers(
                mapOf(
                    "Authorization" to "Bearer #{JWT_TOKEN}",
                    "Content-Type" to "application/json"
                )
            )
            .body(RawFileBody("agora/SignupSimulation/sendBreakfastConsultationResponsesBody.json"))
            .check(status().shouldBe(200))
    ).pause(1)

    private val loadConsultationResponses = exec(
        http("load_consultation_responses")
            .get("/consultations/#{CONSULTATION_ID}/responses")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(1)

    private val scn = scenario("ConsultationSimulation").feed(randomFcmTokenFeeder)
        .exec(
//            sendFcmToken,
            loadConsultations,
            loadOneConsultation,
            loadConsultationQuestions,
//            sendConsultationResponse,
            loadConsultationResponses,
        )

    init {
        setUp(scn.injectOpen(rampUsers(1000).during(60))).protocols(httpProtocol)
    }
}
