package com.bally.evolve.core.sse.engine.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bally.evolve.core.sse.engine.config.SseEngineBeanConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineScheduledConfig;
import com.bally.evolve.core.sse.engine.config.SseEngineWebMvcBeanConfig;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncListener;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
    classes =
        com.bally.evolve.core.sse.engine.controller.SseEngineControllerTest.SseEngineMvcApplication
            .class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
class SseEngineControllerTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void testSseEmitterEvents() throws Exception {
    // Setup
    ResultActions resultActions =
        mockMvc.perform(
            post("/sse-engine/dispatch-event")
                .param("name", "emitter-event")
                .param("subscriptionId", "ID#1")
                .param("eventPayload", "event payload content"));
    resultActions.andExpect(status().isOk());
    resultActions.andReturn();

    // Execute
    ResultActions resultActions2 =
        mockMvc.perform(get("/sse-engine/stream-emitter-events").param("subscriptionId", "ID#1"));
    resultActions2.andExpect(request().asyncStarted());
    resultActions2.andDo(MockMvcResultHandlers.log());
    MvcResult mvcResult = resultActions2.andReturn();

    Awaitility.await()
        .pollDelay(4, TimeUnit.SECONDS)
        .untilAsserted(() -> Assertions.assertTrue(true));

    // Trigger a timeout on the request
    MockAsyncContext asyncContext = (MockAsyncContext) mvcResult.getRequest().getAsyncContext();
    for (AsyncListener listener : asyncContext.getListeners()) {
      listener.onTimeout(null);
    }

    mockMvc.perform(asyncDispatch(mvcResult));
    resultActions2.andDo(MockMvcResultHandlers.log());

    // Asserts
    resultActions2.andExpect(status().isOk());
    resultActions2.andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    String responseContent = mvcResult.getResponse().getContentAsString();
    log.info("-------- /stream-emitter-events response ---------");
    log.info("\n" + responseContent);
    assertThat(responseContent)
        .contains("id:ID#1")
        .contains("event:INIT", "event:emitter-event")
        .contains("\"name\": \"emitter-event\"")
        .contains("\"subscriptionId\": \"ID#1\"")
        .contains("\"payload\": \"event payload content\"");
  }

  //  @Test
  //  void testSseFluxEvents() throws Exception {
  //    // Setup
  //    Payload payloadExpected = new Payload();
  //    payloadExpected.setAttribute1("attr1");
  //    payloadExpected.setAttribute2("attr2");
  //    ObjectMapper objMapper = new ObjectMapper();
  //
  //    ResultActions resultActions =
  //        mockMvc.perform(
  //            post("/sse-engine/dispatch-event")
  //                .param("name", "flux-event")
  //                .param("subscriptionId", "ID#1")
  //                .param(
  //                    "eventPayload",
  //                    new String(
  //                        Base64.getEncoder()
  //                            .encode(
  //                                objMapper
  //                                    .writeValueAsString(payloadExpected)
  //                                    .getBytes(StandardCharsets.UTF_8)))));
  //    resultActions.andExpect(status().isOk());
  //    resultActions.andReturn();
  //
  //    // Execute
  //    ResultActions resultActions2 =
  //        mockMvc.perform(get("/sse-engine/stream-flux-events").param("subscriptionId", "ID#1"));
  //    resultActions2.andExpect(request().asyncStarted());
  //    resultActions2.andDo(MockMvcResultHandlers.log());
  //    MvcResult mvcResult = resultActions2.andReturn();
  //
  //    Awaitility.await()
  //        .pollDelay(4, TimeUnit.SECONDS)
  //        .untilAsserted(() -> assertThat(request().asyncStarted()).isNotNull());
  //
  //    // Trigger a timeout on the request
  //    MockAsyncContext asyncContext = (MockAsyncContext) mvcResult.getRequest().getAsyncContext();
  //    for (AsyncListener listener : asyncContext.getListeners()) {
  //      listener.onTimeout(null);
  //    }
  //
  //    mockMvc.perform(asyncDispatch(mvcResult));
  //    resultActions2.andDo(MockMvcResultHandlers.log());
  //
  //    // Asserts
  //    resultActions2.andExpect(status().isOk());
  //    resultActions2.andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
  //    String responseContent = mvcResult.getResponse().getContentAsString();
  //    String dataContent = responseContent.substring(responseContent.indexOf("data:") + 5);
  //    Event event = objMapper.readValue(dataContent, Event.class);
  //    Payload payloadActual =
  //        objMapper.readValue(
  //            Base64.getDecoder().decode(event.getPayload().getBytes(StandardCharsets.UTF_8)),
  //            Payload.class);
  //    log.info("-------- /stream-flux-events response ---------");
  //    log.info("\n" + responseContent);
  //    assertThat(responseContent).contains("id:ID#1").contains("event:INIT", "event:flux-event");
  //    Assertions.assertEquals("flux-event", event.getName());
  //    Assertions.assertEquals("ID#1", event.getSubscriptionId());
  //    Assertions.assertEquals(payloadExpected, payloadActual);
  //  }

  @Getter
  @Setter
  private static class Payload {
    private String attribute1;
    private String attribute2;

    public Payload() {}

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (this.getClass() != obj.getClass()) return false;
      Payload other = (Payload) obj;
      return Objects.equals(attribute1, other.attribute1)
          && Objects.equals(attribute2, other.attribute2);
    }
  }

  @SpringBootApplication
  @Import({
    SseEngineController.class,
    SseEngineBeanConfig.class,
    SseEngineScheduledConfig.class,
    SseEngineWebMvcBeanConfig.class
  })
  public static class SseEngineMvcApplication {
    public static void main(String[] args) {
      SpringApplication.run(SseEngineMvcApplication.class, args);
    }
  }
}
