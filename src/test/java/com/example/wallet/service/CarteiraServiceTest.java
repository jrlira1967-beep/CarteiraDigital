package com.example.wallet.service;

import com.example.wallet.entity.Carteira;
import com.example.wallet.exception.SaldoInsuficienteException;
import com.example.wallet.repository.CarteiraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarteiraServiceTest {

    @Mock
    private CarteiraRepository repository;

    @InjectMocks
    private CarteiraService service;

    @Test
    void deveCriarCarteiraComSaldoZero() {
        Carteira carteira = new Carteira(null, "José", 0.0);
        when(repository.save(any(Carteira.class))).thenReturn(carteira);

        Carteira resultado = service.criarCarteira("José");

        assertEquals(0.0, resultado.getSaldo());
        assertEquals("José", resultado.getTitular());
    }

    @Test
    void deveDepositarValorComSucesso() {
        Carteira carteira = new Carteira(1L, "José", 0.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));
        when(repository.save(any(Carteira.class))).thenReturn(carteira);

        Carteira resultado = service.depositar(1L, 100.0);

        assertEquals(100.0, resultado.getSaldo());
    }

    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.depositar(1L, -10.0));
    }

    @Test
    void deveSacarComSucesso() {
        Carteira carteira = new Carteira(1L, "José", 200.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));
        when(repository.save(any(Carteira.class))).thenReturn(carteira);

        Carteira resultado = service.sacar(1L, 50.0);

        assertEquals(150.0, resultado.getSaldo());
    }

    @Test
    void deveLancarExcecaoAoSacarSemSaldo() {
        Carteira carteira = new Carteira(1L, "José", 10.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));

        assertThrows(SaldoInsuficienteException.class,
                () -> service.sacar(1L, 100.0));
    }

    @Test
    void deveTransferirComSucesso() {
        Carteira origem = new Carteira(1L, "José", 200.0);
        Carteira destino = new Carteira(2L, "Maria", 0.0);

        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));
        when(repository.save(any(Carteira.class))).thenAnswer(i -> i.getArgument(0));

        service.transferir(1L, 2L, 100.0);

        assertEquals(100.0, origem.getSaldo());
        assertEquals(100.0, destino.getSaldo());
    }

    @Test
    void deveLancarExcecaoAoTransferirSemSaldo() {
        Carteira origem = new Carteira(1L, "José", 10.0);
        Carteira destino = new Carteira(2L, "Maria", 0.0);

        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        assertThrows(SaldoInsuficienteException.class,
                () -> service.transferir(1L, 2L, 100.0));
    }
}