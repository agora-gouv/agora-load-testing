package agora

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class AllSimulation : Simulation() {

    private val randomFcmTokenFeeder = generateSequence {
        mapOf(
            "FCM_TOKEN" to java.util.UUID.randomUUID().toString(),
            "CONSULTATION_ID" to "ce070c8b-3fcb-4a37-946a-0d2bcc059c57",
            "QAG_RESPONSE_ID" to "f29c5d6f-9838-4c57-a7ec-0612145bb0c8",
            "QAG_ID_1" to "506586a9-748a-4eff-898e-e856e06e69ba",
            "QAG_ID_2" to "ba1ef3c9-6f32-46f2-9226-e3afd33b1c51",
            "QAG_ID_3" to "aa9c14bc-1bc5-4abe-aafd-aeb4be9c5ae4",
        )
    }.iterator()

    private val httpProtocol = http
        .baseUrl("https://https://agora-prod-pr257.osc-secnum-fr1.scalingo.io/")
        .acceptHeader("*/*;q=0.8")
        .acceptEncodingHeader("gzip, deflate, br")
        .acceptLanguageHeader("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
        .userAgentHeader("fr.gouv.agora/Gatling/1.1")

    private val sendFcmToken = exec(
        http("register_fcm_token")
            .post("/signup")
            .headers(
                mapOf(
                    "fcmToken" to "#{FCM_TOKEN}",
                    "platform" to "web",
                    "versionCode" to "1",
                )
            )
            .check(status().shouldBe(200))
            .check(jsonPath("$.jwtToken").saveAs("JWT_TOKEN"))
    ).pause(1)

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
    ).pause(5)

    private val loadConsultationQuestions = exec(
        http("load_consultation_questions")
            .get("/consultations/#{CONSULTATION_ID}/questions")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(10)

//    private val sendConsultationResponse = exec(
//        http("send_consultation_response")
//            .post("/consultations/#{CONSULTATION_ID}/responses")
//            .headers(
//                mapOf(
//                    "Authorization" to "Bearer #{JWT_TOKEN}",
//                    "Content-Type" to "application/json"
//                )
//            )
//            .body(RawFileBody("agora/SignupSimulation/sendBreakfastConsultationResponsesBody.json"))
//            .check(status().shouldBe(200))
//    ).pause(1)

    private val loadConsultationResponses = exec(
        http("load_consultation_responses")
            .get("/consultations/#{CONSULTATION_ID}/responses")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(1)

    private val loadResponses = exec(
        http("load_responses")
            .get("/qags/responses")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    )

    private val loadQaGs = exec(
        http("load_qags")
            .get("/qags")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(2)

    private val loadThematiques = exec(
        http("load_thematiques")
            .get("/thematiques")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    )

    private val loadAQuestionWithGovAnswer = exec(
        http("load_qag_with_answer")
            .get("/qags/#{QAG_RESPONSE_ID}")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(2)

    private fun loadQuestion(questionNumber: Int, questionId: String): ChainBuilder {
        return exec(
            http("load_qag_without_answer_${questionNumber}")
                .get("/qags/${questionId}")
                .header("Authorization", "Bearer #{JWT_TOKEN}")
                .check(status().shouldBe(200))
        ).pause(2)
    }

    private val scn = scenario("AllSimulation").feed(randomFcmTokenFeeder)
        .exec(
            sendFcmToken,
            loadConsultations,
            loadOneConsultation,
            loadConsultationQuestions,
//            sendConsultationResponse,
            loadConsultationResponses,
            loadResponses,
            loadQaGs,
            loadThematiques,
            loadAQuestionWithGovAnswer,
            loadQuestion(questionNumber = 1, "#{QAG_ID_1}"),
            loadQuestion(questionNumber = 2, "#{QAG_ID_2}"),
            loadQuestion(questionNumber = 3, "#{QAG_ID_3}"),
        )

    init {
        setUp(scn.injectOpen(rampUsers(1000).during(60))).protocols(httpProtocol)
    }
}
