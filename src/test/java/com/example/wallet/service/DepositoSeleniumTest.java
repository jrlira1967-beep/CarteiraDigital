package com.example.wallet.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositoSeleniumTest {

    private WebDriver driver;

    @BeforeEach
    void setup() {
        // NÃO chama WebDriverManager - ele está sem internet
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        // Aponta pro Edge que já existe no seu PC
        options.setBinary("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void deveSimularDepositoPelaTela() {
        // 1. Abre a página HTML local
        String caminho = Paths.get("src/main/resources/deposito.html")
                .toAbsolutePath().toUri().toString();
        driver.get(caminho);

        // 2. Simula o usuário digitando
        driver.findElement(By.id("conta")).sendKeys("1");
        driver.findElement(By.id("valor")).sendKeys("100");

        // 3. Simula o usuário clicando no botão
        driver.findElement(By.id("btn-depositar")).click();

        // 4. Valida se apareceu a mensagem de sucesso
        String mensagem = driver.findElement(By.id("mensagem")).getText();
        assertTrue(mensagem.contains("Depósito de R$ 100"));
        assertTrue(mensagem.contains("conta 1"));
    }
}