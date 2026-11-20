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

    // ===== CONSULTAR SALDO =====

    @Test
    void deveConsultarSaldo() {
        Carteira carteira = new Carteira(1L, "José", 250.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));

        double saldo = service.consultarSaldo(1L);

        assertEquals(250.0, saldo);
        verify(repository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoConsultarCarteiraInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.consultarSaldo(99L));
    }

    // ===== DEPOSITAR =====

    @Test
    void deveDepositarComSucesso() {
        Carteira carteira = new Carteira(1L, "José", 100.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));
        when(repository.save(any(Carteira.class))).thenAnswer(i -> i.getArgument(0));

        Carteira resultado = service.depositar(1L, 50.0);

        assertEquals(150.0, resultado.getSaldo());
        verify(repository).save(any(Carteira.class));
    }

    @Test
    void deveLancarExcecaoAoDepositarValorZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.depositar(1L, 0.0));
    }

    @Test
    void deveLancarExcecaoAoDepositarValorNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.depositar(1L, -10.0));
    }

    @Test
    void deveLancarExcecaoAoDepositarEmCarteiraInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.depositar(99L, 50.0));
    }

    // ===== SACAR - O QUE FALTAVA =====

    @Test
    void deveSacarComSucesso() {
        Carteira carteira = new Carteira(1L, "José", 200.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));
        when(repository.save(any(Carteira.class))).thenAnswer(i -> i.getArgument(0));

        Carteira resultado = service.sacar(1L, 50.0);

        assertEquals(150.0, resultado.getSaldo());
        verify(repository).save(any(Carteira.class));
    }

    @Test
    void deveLancarSaldoInsuficienteAoSacar() {
        Carteira carteira = new Carteira(1L, "José", 10.0);
        when(repository.findById(1L)).thenReturn(Optional.of(carteira));

        assertThrows(SaldoInsuficienteException.class,
                () -> service.sacar(1L, 100.0));
        verify(repository, never()).save(any(Carteira.class));
    }

    @Test
    void deveLancarExcecaoAoSacarValorZeroOuNegativo() {
        assertThrows(IllegalArgumentException.class, () -> service.sacar(1L, 0.0));
        assertThrows(IllegalArgumentException.class, () -> service.sacar(1L, -5.0));
    }

    // ===== TRANSFERIR =====

    @Test
    void deveTransferirComSucesso() {
        Carteira origem = new Carteira(1L, "José", 200.0);
        Carteira destino = new Carteira(2L, "Maria", 50.0);

        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));
        when(repository.save(any(Carteira.class))).thenAnswer(i -> i.getArgument(0));

        service.transferir(1L, 2L, 100.0);

        assertEquals(100.0, origem.getSaldo());
        assertEquals(150.0, destino.getSaldo());
        verify(repository, times(2)).save(any(Carteira.class));
    }

    @Test
    void deveLancarSaldoInsuficienteAoTransferirSemSaldo() {
        Carteira origem = new Carteira(1L, "José", 10.0);
        Carteira destino = new Carteira(2L, "Maria", 0.0);

        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        assertThrows(SaldoInsuficienteException.class,
                () -> service.transferir(1L, 2L, 100.0));
        verify(repository, never()).save(any(Carteira.class));
    }

    @Test
    void deveLancarExcecaoAoTransferirValorZeroOuNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(1L, 2L, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> service.transferir(1L, 2L, -10.0));
    }

    @Test
    void deveLancarExcecaoAoTransferirParaCarteiraInexistente() {
        Carteira origem = new Carteira(1L, "José", 200.0);
        when(repository.findById(1L)).thenReturn(Optional.of(origem));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.transferir(1L, 99L, 50.0));
    }
}