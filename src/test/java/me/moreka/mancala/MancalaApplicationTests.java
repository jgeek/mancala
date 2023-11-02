package me.moreka.mancala;

import me.moreka.mancala.dto.GameDto;
import me.moreka.mancala.entity.Pit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MancalaApplicationTests {

    private static final String LOCAL_HOST = "http://localhost:";
    private static final String GAME_START_PATH = "/games/start";
    private static final String MOVE_PATH = "/games/%s/move";

    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private GameDto gameDto;

    @BeforeEach
    void contextLoads() throws JSONException, JsonProcessingException {

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject playerJson = new JSONObject();
        playerJson.put("player1", "behnia");
        playerJson.put("player2", "Sepanta");

        HttpEntity<String> request = new HttpEntity<>(playerJson.toString(), headers);
        String stringResult = restTemplate
                .postForObject(LOCAL_HOST + port + GAME_START_PATH, request, String.class);
        JSONObject data = new JSONObject(stringResult).getJSONObject("data");
        ObjectMapper mapper = new ObjectMapper();
        gameDto = mapper.readValue(data.toString(), GameDto.class);

        assertEquals(gameDto.getUser1Pits().size(), 7);
        assertEquals(gameDto.getUser2Pits().size(), 7);
        assertEquals(gameDto.getUser1Pits().stream().filter(Pit::isBig).count(), 1);
        assertEquals(gameDto.getUser2Pits().stream().filter(Pit::isBig).count(), 1);
    }

    @Test
    public void movePitIndexZero() throws JSONException, JsonProcessingException {

        JSONObject moveJson = new JSONObject();
        moveJson.put("userId", gameDto.getPlayer1().getId());
        moveJson.put("pitIndex", 0);

        HttpEntity<String> request = new HttpEntity<>(moveJson.toString(), headers);
        String stringResult = restTemplate
                .postForObject(LOCAL_HOST + port + String.format(MOVE_PATH, gameDto.getGameId()), request,
                        String.class);
        JSONObject data = new JSONObject(stringResult).getJSONObject("data");
        ObjectMapper mapper = new ObjectMapper();
        var afterMoveGameDto = mapper.readValue(data.toString(), GameDto.class);
        afterMoveGameDto.getUser1Pits().stream().takeWhile(p -> p.getIndex() > 0 && p.getIndex() < 6)
                .forEach(p -> assertEquals(p.getStones(), 7));
        afterMoveGameDto.getUser1Pits().stream().filter(p -> p.getIndex() == 6).forEach(p -> {
            assertEquals(p.getStones(), 1);
        });

        assertTrue(afterMoveGameDto.getPlayer1().isCurrentPlayer());
    }

}
