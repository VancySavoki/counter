package org.trivia.counter;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CounterApplication.class)
@Slf4j
public class CounterApplicationTests {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void get() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();            //得到返回代码
        String content = mvcResult.getResponse().getContentAsString();    //得到返回结果

        Result result = JSONObject.parseObject(content, Result.class);
        Assert.assertEquals("200", result.getStatus());
    }
    @Test
    public void post() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();            //得到返回代码
        String content = mvcResult.getResponse().getContentAsString();    //得到返回结果

        Result result = JSONObject.parseObject(content, Result.class);
        Assert.assertEquals(1, result.getData());
    }
    @Test
    public void upload() throws Exception {
        Path counter = Paths.get("counter.txt");
        Path pom = Paths.get("pom.xml");
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "counter.txt", MediaType.TEXT_PLAIN_VALUE, Files.readAllBytes(counter));
        MockMultipartFile pomFile =
                new MockMultipartFile("file", "pom.xml", MediaType.TEXT_XML_VALUE, Files.readAllBytes(pom));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart("/uploadFiles")
                .file(mockMultipartFile)
                .file(pomFile)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();            //得到返回代码
        String content = mvcResult.getResponse().getContentAsString();    //得到返回结果

        Result result = JSONObject.parseObject(content, Result.class);
        Assert.assertEquals("ok", result.getMsg());
    }
    @Test
    public void concurrentTest() throws Exception {
        int concurrency = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(concurrency);
        for(int i = 0; i < concurrency ; i++) {
            int temp = i;
            new Thread(() -> {
                try {
                    MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/")
                            .accept(MediaType.APPLICATION_JSON))
                            .andReturn();
                    log.info("{} - {}", temp, mvcResult.getResponse().getContentAsString());
                } catch (Exception e) {
                    log.error("Request failed", e);
                }
                countDownLatch.countDown();
            }).start();

        }
        countDownLatch.await();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();            //得到返回代码
        String content = mvcResult.getResponse().getContentAsString();    //得到返回结果

        Result result = JSONObject.parseObject(content, Result.class);
        Assert.assertEquals("200", result.getStatus());
        Assert.assertEquals(1000, result.getData());
    }

}
