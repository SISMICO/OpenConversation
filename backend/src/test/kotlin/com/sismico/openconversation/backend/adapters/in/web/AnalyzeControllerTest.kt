package com.sismico.openconversation.backend.adapters.`in`.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AnalyzeController::class)
class AnalyzeControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `POST api analyse returns 308 permanent redirect to v1 conversations`() {
        mockMvc
            .perform(
                multipart("/api/analyse")
                    .file("audio", "fake-audio".toByteArray()),
            ).andExpect(status().isPermanentRedirect)
            .andExpect(header().string("Location", "/api/v1/conversations"))
    }
}
