package com.example.wallet.service;

import com.example.wallet.entity.Carteira;
import com.example.wallet.exception.SaldoInsuficienteException;
import com.example.wallet.repository.CarteiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarteiraService {

    private final CarteiraRepository repository;

    public CarteiraService(CarteiraRepository repository) {
        this.repository = repository;
    }

    // 1. Consultar Saldo: Retornar o saldo atual da conta
    public double consultarSaldo(Long id) {
        Carteira carteira = buscarPorId(id);
        return carteira.getSaldo();
    }

    // 2. Realizar Depósito: Adicionar um valor ao saldo
    @Transactional
    public Carteira depositar(Long id, double valor) {
        // Validação: Não é permitido depositar valores menores ou iguais a zero
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser maior que zero");
        }

        Carteira carteira = buscarPorId(id);
        carteira.setSaldo(carteira.getSaldo() + valor);
        return repository.save(carteira);
    }

    // 3. Realizar Saque: Subtrair um valor do saldo
    @Transactional
    public Carteira sacar(Long id, double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor do saque deve ser maior que zero");
        }

        Carteira carteira = buscarPorId(id);

        if (carteira.getSaldo() < valor) {
            throw new SaldoInsuficienteException("Saldo insuficiente para saque");
        }

        carteira.setSaldo(carteira.getSaldo() - valor);
        return repository.save(carteira);
    }

    // 4. Realizar Transferência: Transferir um valor entre duas contas
    @Transactional
    public void transferir(Long idOrigem, Long idDestino, double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero");
        }

        Carteira origem = buscarPorId(idOrigem);
        Carteira destino = buscarPorId(idDestino);

        // Validação: Saldo da origem inferior ao valor -> SaldoInsuficienteException
        if (origem.getSaldo() < valor) {
            throw new SaldoInsuficienteException("Saldo insuficiente para transferência");
        }

        origem.setSaldo(origem.getSaldo() - valor);
        destino.setSaldo(destino.getSaldo() + valor);

        repository.save(origem);
        repository.save(destino);
    }

    private Carteira buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carteira não encontrada: " + id));
    }
}