package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.adapters.`in`.web.error.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ExceptionHandlerTestController::class)
@Import(GlobalExceptionHandler::class)
class GlobalExceptionHandlerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `validation error returns 400 with standardized error body`() {
        mockMvc
            .perform(
                post("/test/validation-error")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").exists())
    }

    @Test
    fun `not found error returns 404 with standardized error body`() {
        mockMvc
            .perform(
                get("/test/not-found")
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").exists())
    }

    @Test
    fun `illegal argument error returns 400 with standardized error body`() {
        mockMvc
            .perform(get("/test/illegal-argument"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.error.message").exists())
    }
}
