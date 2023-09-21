package agora

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class QaGSimulation : Simulation() {

    private val randomFcmTokenFeeder = generateSequence {
        mapOf(
            "FCM_TOKEN" to java.util.UUID.randomUUID().toString(),
            "JWT_TOKEN" to "<ActualJwtToken>",
            "THEMATIQUE_ID" to "30671310-ee62-11ed-a05b-0242ac120003",
            "THEMATIQUE_LABEL" to "Démocratie",
            "QAG_RESPONSE_ID" to "f29c5d6f-9838-4c57-a7ec-0612145bb0c8",
            // DEV
//            "QAG_ID_1" to "b68c37bf-dfcd-4f23-9d2b-0d309a0c1e55",
//            "QAG_ID_2" to "8b3d4344-6c43-4842-9933-3f43d27a3fa5",
//            "QAG_ID_3" to "1c972fc9-a885-41f4-bbb3-035818ae9847",
//            "QAG_SUPPORT_ID_1" to "1c972fc9-a885-41f4-bbb3-035818ae9847",
//            "QAG_SUPPORT_ID_2" to "1491b4a2-7e84-46b2-b16f-a1a53e1d38c4",
            // PROD
            "QAG_ID_1" to "aaa162a9-d8bb-462d-84ed-9e1bf8d6dadc",
            "QAG_ID_2" to "8f7ad397-ef1c-4095-bc2b-86bf6271cc8c",
            "QAG_ID_3" to "3d744635-1171-4574-96a1-526d7f788784",
//            "QAG_SUPPORT_ID_1" to "1c972fc9-a885-41f4-bbb3-035818ae9847",
//            "QAG_SUPPORT_ID_2" to "1491b4a2-7e84-46b2-b16f-a1a53e1d38c4",
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
//            .check(jsonPath("$.loginToken").saveAs("loginToken"))
//            .check(jsonPath("$.JWT_TOKEN").saveAs("JWT_TOKEN"))
//    ).pause(1)

//    private val login = exec(
//        http("login_to_application")
//            .post("/login")
//            .headers(
//                mapOf(
//                    "fcmToken" to "#{FCM_TOKEN}",
//                    "platform" to "web",
//                    "version" to "1",
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
    ).pause(2)

    private val loadResponses = exec(
        http("load_responses")
            .get("/qags/responses")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
//            .check(jsonPath("$.responses[0].qagId").saveAs("qagIdWithResponse"))
    )

    private val loadQaGs = exec(
        http("load_qags")
            .get("/qags")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
//            .check(jsonPath("$.qags.popular[0].qagId").saveAs("qagIdWithoutResponse1"))
//            .check(jsonPath("$.qags.popular[1].qagId").saveAs("qagIdWithoutResponse2"))
//            .check(jsonPath("$.qags.popular[2].qagId").saveAs("qagIdWithoutResponse3"))
    ).pause(2)

    private val loadThematiques = exec(
        http("load_thematiques")
            .get("/thematiques")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
//            .check(jsonPath("$.thematiques[2].id").saveAs("thematiqueId1"))
//            .check(jsonPath("$.thematiques[2].label").saveAs("thematiqueLabel1"))
    )

    private val loadAQuestionWithGovAnswer = exec(
        http("load_qag_with_answer")
            .get("/qags/#{QAG_RESPONSE_ID}")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(2)

//    private val sendFeedbackOnGovAnswer = exec(
//        http("send_feedback_on_gov_answer")
//            .post("/qags/#{QAG_RESPONSE_ID}/feedback")
//            .headers(
//                mapOf(
//                    "Authorization" to "Bearer #{JWT_TOKEN}",
//                    "Content-Type" to "application/json"
//                )
//            )
//            .body(StringBody("""{"isHelpful": true}"""))
//            .check(status().shouldBe(200))
//    ).pause(1)

    private fun loadQuestion(questionNumber: Int, questionId: String): ChainBuilder {
        return exec(
            http("load_qag_without_answer_${questionNumber}")
                .get("/qags/${questionId}")
                .header("Authorization", "Bearer #{JWT_TOKEN}")
                .check(status().shouldBe(200))
        ).pause(2)
    }

    private fun supportQuestion(questionNumber: Int, questionId: String): ChainBuilder {
        return exec(
            http("support_qag_${questionNumber}")
                .post("/qags/${questionId}/support")
                .header("Authorization", "Bearer #{JWT_TOKEN}")
                .check(status().shouldBe(200))
        ).pause(1)
    }

    private val deleteSupportToThirdQuestionWithoutAnswer = exec(
        http("delete_support_qag_3")
            .delete("/qags/#{qagIdWithoutResponse3}/support")
            .header("Authorization", "Bearer #{JWT_TOKEN}")
            .check(status().shouldBe(200))
    ).pause(1)

    private fun filterQuestionsByThematique(
        thematiqueLabel: String,
        thematiqueId: String,
        thematiqueName: String
    ): ChainBuilder {
        return exec(
            http("filter_qag_on_thematique_${thematiqueLabel}")
                .get("/qags?thematiqueId=${thematiqueId}")
                .header("Authorization", "Bearer #{JWT_TOKEN}")
                .check(status().shouldBe(200))
//                .check(jsonPath("$.qags.popular[0].qagId").saveAs("qagFilteredOn_$thematiqueName"))
        ).pause(2)
    }

    private val sendQuestionToGov = exec(
        http("send_question_to_governement")
            .post("/qags")
            .headers(
                mapOf(
                    "Authorization" to "Bearer #{JWT_TOKEN}",
                    "Content-Type" to "application/json"
                )
            )
            .body(RawFileBody("agora/SignupSimulation/sendQuestionToGovBody.json"))
            .check(status().shouldBe(200))
            .check(jsonPath("$.qagId").saveAs("questionSentToGovId"))
    ).pause(1)

//    private val scn = scenario("SignupSimulation")
//        .feed(randomFcmTokenFeeder)
//        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
//            loadQaGs,
//            loadThematiques,
//            loadAQuestionWithGovAnswer,
//            sendFeedbackOnGovAnswer,
//            loadQuestion(1, "#{qagIdWithoutResponse1}"),
//            supportQuestion(1, "#{qagIdWithoutResponse1}"),
//            loadQuestion(2, "#{qagIdWithoutResponse2}"),
//            supportQuestion(2, "#{qagIdWithoutResponse2}"),
//            loadQuestion(3, "#{qagIdWithoutResponse3}"),
//            supportQuestion(3, "#{qagIdWithoutResponse3}"),
//            deleteSupportToThirdQuestionWithoutAnswer,
//            filterQuestionsByThematique("#{thematiqueLabel1}", "#{thematiqueId1}", "travail"),
//            filterQuestionsByThematique("#{thematiqueLabel2}", "#{thematiqueId2}", "education"),
//            loadQuestion(4, "#{qagFilteredOn_education}"),
//            supportQuestion(4, "#{qagFilteredOn_education}"),
//            filterQuestionsByThematique("#{thematiqueLabel3}", "#{thematiqueId3}", "sante"),
//            loadQuestion(5, "#{qagFilteredOn_sante}"),
//            supportQuestion(5, "#{qagFilteredOn_sante}"),
//            loadThematiques,
//            sendQuestionToGov,
//            loadQuestion(6, "#{questionSentToGovId}")
//        )
//
//    private val consultGovResponse = scenario("ConsultGovAnswer").feed(randomFcmTokenFeeder)
//        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
//            loadQaGs,
//            loadThematiques,
//            loadAQuestionWithGovAnswer,
//            sendFeedbackOnGovAnswer
//        )
//
//    private val supportQaG = scenario("SupportQuestions").feed(randomFcmTokenFeeder)
//        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
//            loadQaGs,
//            loadThematiques,
//            loadQuestion(1, "#{qagIdWithoutResponse1}"),
//            supportQuestion(1, "#{qagIdWithoutResponse1}"),
//            loadQuestion(2, "#{qagIdWithoutResponse2}"),
//            supportQuestion(2, "#{qagIdWithoutResponse2}"),
//            loadQuestion(3, "#{qagIdWithoutResponse3}"),
//            supportQuestion(3, "#{qagIdWithoutResponse3}"),
//            deleteSupportToThirdQuestionWithoutAnswer
//        )
//
//    private val filterQaG = scenario("FilterQuestions").feed(randomFcmTokenFeeder)
//        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
//            loadQaGs,
//            loadThematiques,
//            filterQuestionsByThematique("#{thematiqueLabel1}", "#{thematiqueId1}", "democratie"),
//            filterQuestionsByThematique("#{thematiqueLabel2}", "#{thematiqueId2}", "education"),
//            loadQuestion(4, "#{qagFilteredOn_education}"),
//            supportQuestion(4, "#{qagFilteredOn_education}"),
////            filterQuestionsByThematique("#{thematiqueLabel3}", "#{thematiqueId3}", "sante"),
////            loadQuestion(5, "#{qagFilteredOn_sante}"),
////            supportQuestion(5, "#{qagFilteredOn_sante}")
//        )
//
//    private val sendQuestion = scenario("SendQuestion").feed(randomFcmTokenFeeder)
//        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
//            loadQaGs,
//            loadThematiques,
////            loadThematiques,
////            sendQuestionToGov,
//            loadQuestion(6, "#{questionSentToGovId}"),
//        )

    private val qagScenario = scenario("QaGAndMore").feed(randomFcmTokenFeeder)
        .exec(
//            sendFcmToken,
//            login,
//            loadConsultations,
            loadResponses,
            loadThematiques,
            loadQaGs,
            loadAQuestionWithGovAnswer,
//            sendFeedbackOnGovAnswer,
            filterQuestionsByThematique("#{THEMATIQUE_LABEL}", "#{THEMATIQUE_ID}", "#{THEMATIQUE_LABEL}"),
            loadQuestion(1, "#{QAG_ID_1}"),
//            supportQuestion(1, "#{qagIdWithoutResponse1}"),
            loadQuestion(2, "#{QAG_ID_2}"),
//            supportQuestion(2, "#{qagIdWithoutResponse2}"),
            loadQuestion(3, "#{QAG_ID_3}"),
//            supportQuestion(3, "#{qagIdWithoutResponse3}"),
//            deleteSupportToThirdQuestionWithoutAnswer,
        )

    init {
        setUp(
//            consultGovResponse.injectOpen(rampUsers(maxUsers).during(60)),
//            supportQaG.injectOpen(rampUsers(maxUsers).during(60)),
//            filterQaG.injectOpen(rampUsers(maxUsers).during(60)),
//            sendQuestion.injectOpen(rampUsers(maxUsers).during(60)),
            qagScenario.injectOpen(rampUsers(1000).during(60)),
        ).protocols(httpProtocol)
    }
}
