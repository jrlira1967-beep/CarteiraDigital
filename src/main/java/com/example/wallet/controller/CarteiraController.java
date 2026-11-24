package com.example.wallet.controller;

import com.example.wallet.entity.Carteira;
import com.example.wallet.exception.SaldoInsuficienteException;
import com.example.wallet.service.CarteiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carteiras")
public class CarteiraController {

    private final CarteiraService service;

    public CarteiraController(CarteiraService service) {
        this.service = service;
    }

    // GET /carteiras/1/saldo -> 200
    @GetMapping("/{id}/saldo")
    public ResponseEntity<Double> consultarSaldo(@PathVariable("id") Long id) {
        double saldo = service.consultarSaldo(id);
        return ResponseEntity.ok(saldo);
    }

    // POST /carteiras/1/depositar -> 200
    @PostMapping("/{id}/depositar")
    public ResponseEntity<Carteira> depositar(@PathVariable("id") Long id, @RequestBody ValorRequest request) {
        Carteira carteira = service.depositar(id, request.valor());
        return ResponseEntity.ok(carteira);
    }

    // POST /carteiras/1/sacar -> 200
    @PostMapping("/{id}/sacar")
    public ResponseEntity<Carteira> sacar(@PathVariable("id") Long id, @RequestBody ValorRequest request) {
        Carteira carteira = service.sacar(id, request.valor());
        return ResponseEntity.ok(carteira);
    }

    // POST /carteiras/transferir -> 200
    @PostMapping("/transferir")
    public ResponseEntity<Void> transferir(@RequestBody TransferenciaRequest request) {
        service.transferir(request.idOrigem(), request.idDestino(), request.valor());
        return ResponseEntity.ok().build();
    }

    // Isso aqui é o que faz voltar 400 e não 500 quando dá erro!
    @ExceptionHandler({IllegalArgumentException.class, SaldoInsuficienteException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // DTOs - pra receber o JSON que o teste manda
    // {"valor": 50.0}
    public record ValorRequest(double valor) {}

    // {"idOrigem": 1, "idDestino": 2, "valor": 100.0}
    public record TransferenciaRequest(Long idOrigem, Long idDestino, double valor) {}
}