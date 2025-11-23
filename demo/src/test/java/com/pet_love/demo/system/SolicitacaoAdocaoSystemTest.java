package com.pet_love.demo.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SolicitacaoAdocaoSystemTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        // Navega para a tela de solicitações de adoção
        driver.get(BASE_URL + "/adocoes");

        if (driver.getCurrentUrl().contains("/login")) {
            realizarLogin("admin", "admin");
        }

        abrirMenuENavegarParaAdocoes();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // CT09 - Validar se o sistema cria uma nova solicitação de adoção com os dados corretos, sem erros
    @Test
    void testCadastroSolicitacaoAdocao() {
        cadastrarSolicitacaoPadrao("Solicitação realizada");

        WebElement novaLinha = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Pet 1')]"))
        );
        Assertions.assertNotNull(novaLinha, "Solicitação de Adoção não foi adicionada à tabela");
    }

    // CT10 - Validar se o sistema impede a criação de uma solicitação de adoção sem o pet informado
    @Test
    void testCadastroSolicitacaoAdocaoSemPet() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("", "Dono 1", "10-12-2025", "Solicitação realizada");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Pet é obrigatório')]")
        ));
        Assertions.assertTrue(errorMsg.isDisplayed(),"Mensagem de erro não foi exibida");
    }

    // CT11 - Validar se o sistema impede a criação de uma solicitação de adoção sem o dono informado
    @Test
    void testCadastroSolicitacaoAdocaoSemDono() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "", "10-12-2025", "Solicitação realizada");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Pessoa é obrigatório')]")
        ));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Mensagem de erro não foi exibida");
    }

    // CT12 - Validar se o sistema impede a criação de uma solicitação de adoção sem a data informada
    @Test
    void testCadastroSolicitacaoAdocaoSemData() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "Dono 1", "", "Solicitação realizada");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Data é obrigatório')]")
        ));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Mensagem de erro não foi exibida");
    }

    // CT13 - Validar se o sistema impede a criação de solicitação de adoção com data anterior à data atual
    @Test
    void testCadastroSolicitacaoAdocaoDataAnteriorADataAtual() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "Dono 1", "08-10-2025", "Solicitação realizada");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        boolean message = !driver.findElements(
                By.xpath("//*[contains(text(), 'A data da solicitação de adoção não pode ser anterior à data atual')]")
        ).isEmpty();
        Assertions.assertTrue(message, "Mensagem de erro não foi exibida");
    }

    // CT14 - Validar se o sistema impede a criação de uma solicitação de adoção com data inexistente
    @Test
    void cadastrarSolicitacaoAdocaoComDataInexistente() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "Dono 1", "31-02-2025", "Solicitação realizada");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        boolean message = !driver.findElements(
                By.xpath("//*[contains(text(), 'Data informada é inválida')]")
        ).isEmpty();
        Assertions.assertTrue(message, "Mensagem de erro não foi exibida");
    }

    // CT15 - Validar se o sistema impede a criação de uma solicitação de adoção sem o status informado
    @Test
    void cadastroSolicitacaoAdocaoSemStatus() {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "Dono 1", "10-12-2025", "");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(), 'Status é obrigatório')]")
        ));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Mensagem de erro não foi exibida");
    }

    // CT16 - Validar se o sistema exclui uma solicitação de adoção, sem erros
    @Test
    void testExcluirSolicitacaoAdocaoSucesso() {
        // Localiza uma linha que tenha botão Excluir
        WebElement solicitacaoAdocao = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//tbody//tr[td//button[contains(., 'Excluir')]]")
        ));

        // Guarda o texto da linha antes de excluir
        String conteudoSolicitacao = solicitacaoAdocao.getText();

        WebElement deleteBtn = solicitacaoAdocao.findElement(By.xpath(".//button[contains(., 'Excluir')]"));
        deleteBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.modal.show")
        ));
        Assertions.assertNotNull(modal, "Modal de confirmação não apareceu.");

        WebElement confirmaBtn = modal.findElement(By.xpath(".//button[contains(., 'Sim')]"));
        confirmaBtn.click();

        wait.until(ExpectedConditions.invisibilityOf(modal));
        Assertions.assertThrows(NoSuchElementException.class, () ->
            driver.findElement(By.xpath("//tr[td[contains(., '" + conteudoSolicitacao + "')]]")),
                "A solicitação de adoção ainda existe após a exclusão"
        );
    }

    // CT17 - Validar se o sistema exclui uma solicitação de adoção com status "Aprovado"
    @Test
    void testeExcluirSolicitacaoAdocaoComStatusAprovado() {
        cadastrarSolicitacaoPadrao("Aprovado");

        WebElement solicitacaoAdocao = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//tr[td[text()='Aprovado']]")
                )
        );

        String conteudoSolicitacao = solicitacaoAdocao.getText();

        WebElement deleteBtn = solicitacaoAdocao.findElement(By.xpath(".//button[contains(., 'Excluir')]"));
        deleteBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.modal.show")
        ));
        Assertions.assertNotNull(modal, "Modal de confirmação não apareceu.");

        WebElement confirmaBtn = modal.findElement(By.xpath(".//button[contains(., 'Sim')]"));
        confirmaBtn.click();
        wait.until(ExpectedConditions.invisibilityOf(modal));

        boolean message = !driver.findElements(
                By.xpath("//*[contains(text(), \"Não é permitido excluir uma solicitação de adoção com status 'Aprovado'\")]")
        ).isEmpty();
        Assertions.assertTrue(message, "Mensagem de erro não foi exibida");

        WebElement solicitacaoAdocaoAposExclusao = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(., '" + conteudoSolicitacao + "')]]")
        ));
        Assertions.assertNotNull(solicitacaoAdocaoAposExclusao, "A solicitação com status 'Aprovado' não deveria ter sido excluída");
    }

    // CT18 - Validar se o sistema exclui uma solicitação de adoção com status "Reprovado"
    @Test
    void testeExcluirSolicitacaoAdocaoComStatusReprovado() {
        cadastrarSolicitacaoPadrao("Reprovado");

        WebElement solicitacaoAdocao = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//tr[td[text()='Reprovado']]")
                )
        );

        String conteudoSolicitacao = solicitacaoAdocao.getText();

        WebElement deleteBtn = solicitacaoAdocao.findElement(By.xpath(".//button[contains(., 'Excluir')]"));
        deleteBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.modal.show")
        ));
        Assertions.assertNotNull(modal, "Modal de confirmação não apareceu.");

        WebElement confirmaBtn = modal.findElement(By.xpath(".//button[contains(., 'Sim')]"));
        confirmaBtn.click();
        wait.until(ExpectedConditions.invisibilityOf(modal));

        boolean message = !driver.findElements(
                By.xpath("//*[contains(text(), \"Não é permitido excluir uma solicitação de adoção com status 'Reprovado'\")]")
        ).isEmpty();
        Assertions.assertTrue(message, "Mensagem de erro não foi exibida");

        WebElement solicitacaoAdocaoAposExclusao = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(., '" + conteudoSolicitacao + "')]]")
        ));
        Assertions.assertNotNull(solicitacaoAdocaoAposExclusao, "A solicitação com status 'Reprovado' não deveria ter sido excluída");
    }

    private void cadastrarSolicitacaoPadrao(String status) {
        abrirFormularioSolicitacaoAdocao();
        preencherFormulario("Pet 1", "Dono 1", "10-12-2025", status);
        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();
    }

    private void abrirFormularioSolicitacaoAdocao() {
        WebElement novaSolicitacaoBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Nova Solicitação')]"))
        );
        novaSolicitacaoBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-dialog")));
    }

    private void preencherFormulario(String pet, String dono, String data, String status) {
        if (pet != null && !pet.isBlank()) {
            Select petSelect = new Select(driver.findElement(By.name("pet")));
            petSelect.selectByVisibleText(pet);
        }

        if (dono != null && !dono.isBlank()) {
            Select donoSelect = new Select(driver.findElement(By.name("person")));
            donoSelect.selectByVisibleText(dono);
        }

        WebElement dateInput = driver.findElement(By.name("date"));
        dateInput.clear();
        if (data != null && !data.isBlank()) {
            dateInput.sendKeys(data);
        }

        if (status != null && !status.isBlank()) {
            Select statusSelect = new Select(driver.findElement(By.name("status")));
            statusSelect.selectByVisibleText(status);
        }
    }

    private void realizarLogin(String usuario, String senha) {
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("usuario")));
        usuarioInput.sendKeys(usuario);

        WebElement senhaInput = driver.findElement(By.name("senha"));
        senhaInput.sendKeys(senha);

        WebElement entrarButton = driver.findElement(By.cssSelector("button.enter-button"));
        entrarButton.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    private void abrirMenuENavegarParaAdocoes() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .btn")
        ));
        menuButton.click();

        WebElement linkAdocoes = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href='/adocoes']")
        ));
        linkAdocoes.click();

        wait.until(ExpectedConditions.urlContains("/adocoes"));
    }
}