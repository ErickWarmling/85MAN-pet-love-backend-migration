package com.pet_love.demo.system;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class ConsultaSystemTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        // Navega para a tela de consultas
        driver.get(BASE_URL + "/consultas");

        // Se a página redirecionar para login, então fazer o login
        if (driver.getCurrentUrl().contains("/login")) {
            realizarLogin("admin", "admin");
        }

        // Após o login, abra o menu e clique em "Consulta"
        abrirMenuENavegarParaConsultas();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testCadastroConsulta() {
        // CT01: nova consulta
        cadastrarConsultaPadrao("Consulta de rotina", "150");

        // Verificar se a linha foi adicionada à tabela
        WebElement novaLinha = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//td[contains(., 'Consulta de rotina')]"))
        );
        Assertions.assertNotNull(novaLinha, "Consulta não foi adicionada à tabela.");
    }

    @Test
    void testCadastroConsultaSemDataEHora() {
        // CT02: Data e hora vazias
        abrirFormularioConsulta();
        preencherFormulario("Pet 1", "Vet 1", "", "", "Consulta de check-up.", "150");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        Assertions.assertFalse(saveBtn.isEnabled(), "Botão Salvar deveria estar desabilitado.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'É necessário informar uma data e um horário para agendar a consulta')]")
        ));
        Assertions.assertNotNull(errorMsg, "Mensagem de erro não foi exibida.");
    }

    @Test
    void testCadastroConsultaDataPassada() {
        // CT03: Data anterior à atual
        abrirFormularioConsulta();
        preencherFormulario("Pet 1", "Vet 1", "01-01-2020", "16:20", "Consulta de check-up.", "150");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        Assertions.assertFalse(saveBtn.isEnabled(), "Botão Salvar deveria estar desabilitado.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'É necessário informar uma data maior ou igual à data atual')]")
        ));
        Assertions.assertNotNull(errorMsg, "Mensagem de erro não foi exibida.");
    }

    @Test
    void testCadastroConsultaHorarioPassadoHoje() {
        // CT04: Data igual à atual e horário anterior
        abrirFormularioConsulta();
        String dataAtual = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        preencherFormulario("Pet 1", "Vet 1", dataAtual, "00:01", "Consulta de check-up.", "150");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        Assertions.assertFalse(saveBtn.isEnabled(), "Botão Salvar deveria estar desabilitado.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'É necessário informar um horário anterior ao atual')]")
        ));
        Assertions.assertNotNull(errorMsg, "Mensagem de erro não foi exibida.");
    }

    @Test
    void testCadastroConsultaHorarioInvalido() {
        // CT05: Horário -01:00 e 24:00
        abrirFormularioConsulta();
        preencherFormulario("Pet 1", "Vet 1", "20-11-2025", "-01:00", "Consulta de check-up.", "150");

        WebElement timeInput = driver.findElement(By.name("time"));
        Assertions.assertEquals("", timeInput.getAttribute("value"), "Horário inválido deve ser rejeitado.");

        // Teste 2
        preencherFormulario("Pet 1", "Vet 1", "20-11-2025", "24:00", "Consulta de check-up.", "150");
        Assertions.assertEquals("", timeInput.getAttribute("value"), "Horário acima de 23:59 deve ser rejeitado.");
    }

    @Test
    void testCadastroConsultaValorNegativo() {
        // CT06: Valor negativo
        abrirFormularioConsulta();
        preencherFormulario("Pet 1", "Vet 1", "20-11-2025", "16:30", "Consulta de check-up.", "-150");

        WebElement valueInput = driver.findElement(By.name("value"));
        Assertions.assertEquals("", valueInput.getAttribute("value"), "Valor negativo deve ser rejeitado.");

        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        Assertions.assertFalse(saveBtn.isEnabled(), "Botão Salvar deveria estar desabilitado.");
    }

    @Test
    void testExcluirConsultaSucesso() {
        // CT07: Excluir consulta sem valor (ex.: "Sem valor")
        cadastrarConsultaPadrao("Consulta sem valor para exclusão", "0");

        WebElement row = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//tr[td[text()='Consulta sem valor para exclusão']]")
                )
        );

        row.findElement(By.xpath(".//button[contains(., 'Excluir')]")).click();
        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("div.modal.show")
                )
        );
        modal.findElement(By.xpath(".//button[contains(., 'Sim')]")).click();

        // Aguarda desaparecer da tabela
        wait.until(
                ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//tr[td[text()='Consulta sem valor para exclusão']]")
                )
        );

        Assertions.assertTrue(
                driver.findElements(
                        By.xpath("//tr[td[text()='Consulta sem valor para exclusão']]")
                ).isEmpty(),
                "Consulta ainda existe após exclusão."
        );
    }

    @Test
    void testExcluirConsultaComValor() {
        // CT08: Impedir exclusão caso haja valor informado
        //cadastrarConsulta("Consulta com valor", "100");

        WebElement consultaComValor = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//tr[td[contains(., 'Consulta de rotina')]]")
        ));
        WebElement deleteBtn = consultaComValor.findElement(By.xpath(".//button[contains(., 'Excluir')]"));
        deleteBtn.click();

        // Verificar se o modal NÃO abre e uma mensagem de erro é exibida
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.modal.show")));
            Assertions.fail("Modal de confirmação foi exibido, mas deveria ter sido bloqueado.");
        } catch (TimeoutException e) {
            // Esperado: modal não deve aparecer
            WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'Apenas consultas sem valor informado podem ser excluídas')]")
            ));
            Assertions.assertNotNull(errorMsg, "Mensagem de erro não apareceu para consulta com valor.");
        }
    }

    private void cadastrarConsultaPadrao(String descricao, String valor) {
        abrirFormularioConsulta();
        preencherFormulario("Pet 1", "Vet 1", "20-11-2025", "14:30", descricao, valor);
        WebElement saveBtn = driver.findElement(By.xpath("//button[contains(., 'Salvar')]"));
        saveBtn.click();
    }

    private void abrirFormularioConsulta() {
        WebElement novaConsultaBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(., 'Nova Consulta')]"))
        );
        novaConsultaBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-dialog")));
    }

    private void preencherFormulario(String pet, String vet, String data, String hora, String descricao, String valor) {
        Select petSelect = new Select(driver.findElement(By.name("pet")));
        petSelect.selectByVisibleText(pet);

        Select vetSelect = new Select(driver.findElement(By.name("veterinary")));
        vetSelect.selectByVisibleText(vet);

        WebElement dateInput = driver.findElement(By.name("date"));
        dateInput.clear();
        dateInput.sendKeys(data);

        WebElement timeInput = driver.findElement(By.name("time"));
        timeInput.clear();
        timeInput.sendKeys(hora);

        WebElement descTextarea = driver.findElement(By.name("description"));
        descTextarea.clear();
        descTextarea.sendKeys(descricao);

        WebElement valueInput = driver.findElement(By.name("value"));
        valueInput.clear();
        valueInput.sendKeys(valor);
    }

    private void realizarLogin(String usuario, String senha) {
        // Preenche o campo usuário pelo atributo name
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("usuario")));
        usuarioInput.sendKeys(usuario);

        // Preenche o campo senha
        WebElement senhaInput = driver.findElement(By.name("senha"));
        senhaInput.sendKeys(senha);

        // Clica no botão "ENTRAR"
        WebElement entrarButton = driver.findElement(By.cssSelector("button.enter-button"));
        entrarButton.click();

        // Aguarda até que a URL mude ou a página pós-login carregue
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    private void abrirMenuENavegarParaConsultas() {
        // Clica no botão de menu (hambúrguer) caso o menu não esteja visível
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("nav .btn")
        ));
        menuButton.click();

        // Aguarda até que o item "Consulta" no menu esteja visível
        WebElement linkConsulta = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href='/consultas']")
        ));
        linkConsulta.click();

        // Aguarda o carregamento da página de consultas
        wait.until(ExpectedConditions.urlContains("/consultas"));
    }
}
