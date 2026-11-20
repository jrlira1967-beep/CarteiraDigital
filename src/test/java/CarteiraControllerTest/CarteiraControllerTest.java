package CarteiraControllerTest;

import com.example.wallet.entity.Carteira;
import com.example.wallet.exception.SaldoInsuficienteException;
import com.example.wallet.service.CarteiraService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarteiraControllerTest.class)
class CarteiraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarteiraService service;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper que você me perguntou no começo!
    private String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ===== CONSULTAR SALDO =====

    @Test
    void deveRetornar200AoConsultarSaldo() throws Exception {
        when(service.consultarSaldo(1L)).thenReturn(250.0);

        mockMvc.perform(get("/carteiras/1/saldo"))
                .andExpect(status().isOk())
                .andExpect(content().string("250.0"));
    }

    @Test
    void deveRetornar400AoConsultarCarteiraInexistente() throws Exception {
        when(service.consultarSaldo(99L))
                .thenThrow(new IllegalArgumentException("Carteira não encontrada"));

        mockMvc.perform(get("/carteiras/99/saldo"))
                .andExpect(status().isBadRequest());
    }

    // ===== DEPOSITAR =====

    @Test
    void deveRetornar200AoDepositar() throws Exception {
        Carteira carteira = new Carteira(1L, "José", 150.0);
        when(service.depositar(eq(1L), eq(50.0))).thenReturn(carteira);

        // Aqui você manda um JSON no body
        String json = "{\"valor\": 50.0}";

        mockMvc.perform(post("/carteiras/1/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.0));
    }

    @Test
    void deveRetornar400AoDepositarValorInvalido() throws Exception {
        when(service.depositar(eq(1L), eq(0.0)))
                .thenThrow(new IllegalArgumentException("Valor deve ser maior que zero"));

        String json = "{\"valor\": 0.0}";

        mockMvc.perform(post("/carteiras/1/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // ===== SACAR =====

    @Test
    void deveRetornar200AoSacar() throws Exception {
        Carteira carteira = new Carteira(1L, "José", 150.0);
        when(service.sacar(eq(1L), eq(50.0))).thenReturn(carteira);

        mockMvc.perform(post("/carteiras/1/sacar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 50.0}"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar400AoSacarSemSaldo() throws Exception {
        when(service.sacar(eq(1L), eq(1000.0)))
                .thenThrow(new SaldoInsuficienteException("Saldo insuficiente"));

        mockMvc.perform(post("/carteiras/1/sacar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 1000.0}"))
                .andExpect(status().isBadRequest());
    }

    // ===== TRANSFERIR =====

    @Test
    void deveRetornar200AoTransferir() throws Exception {
        String json = "{\"idOrigem\": 1, \"idDestino\": 2, \"valor\": 100.0}";

        mockMvc.perform(post("/carteiras/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar400AoTransferirSemSaldo() throws Exception {
        // Simula que o service vai jogar a exceção
        org.mockito.Mockito.doThrow(new SaldoInsuficienteException("Saldo insuficiente"))
                .when(service).transferir(eq(1L), eq(2L), eq(1000.0));

        String json = "{\"idOrigem\": 1, \"idDestino\": 2, \"valor\": 1000.0}";

        mockMvc.perform(post("/carteiras/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}