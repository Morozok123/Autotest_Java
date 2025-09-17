package org.ibs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FirstTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    // Конфигурационные константы
    private static final String BASE_URL = "http://217.74.37.176";
    private static final String REGISTER_URL = BASE_URL + "/?route=account/register&language=ru-ru";
    private static final String DRIVER_PATH = "src\\test\\resources\\msedgedriver.exe";
    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(10);
    private static final Duration EXPLICIT_WAIT = Duration.ofSeconds(10);

    // Селекторы
    private static final By FIRST_NAME_INPUT = By.id("input-firstname");
    private static final By LAST_NAME_INPUT = By.id("input-lastname");
    private static final By EMAIL_INPUT = By.id("input-email");
    private static final By PASSWORD_INPUT = By.id("input-password");
    private static final By NEWSLETTER_CHECKBOX = By.id("input-newsletter");
    private static final By AGREE_CHECKBOX = By.xpath("//input[@name='agree']");
    private static final By CONTINUE_BUTTON = By.xpath("//button[text()='Продолжить']");
    private static final By ERROR_ELEMENTS = By.cssSelector(".alert-danger, .text-danger, .has-error");

    @BeforeEach
    void setUp() {
        System.setProperty("webdriver.edge.driver", DRIVER_PATH);
        driver = new EdgeDriver();
        wait = new WebDriverWait(driver, EXPLICIT_WAIT);
        actions = new Actions(driver);

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testEmail() {
        driver.get(REGISTER_URL);
        scrollDown(200);

        fillRegistrationForm(
                "Иван",           // firstName
                "Иванов",         // lastName
                "himail.ru",      // email (invalid - no @)
                "Q1w2e3l",        // password
                true,             // subscribe
                true              // agree
        );

        clickContinueButton();

        assertRegistrationFailed("Регистрация должна быть провальной из-за отсутствия @ в email");
    }

    @Test
    void testName() {
        driver.get(REGISTER_URL);
        scrollDown(200);

        fillRegistrationForm(
                "1234567890",     // firstName (invalid - digits)
                "Иванов",         // lastName
                "IvanIvan22222@mail.ru", // email
                "Q1w2e3l",        // password
                true,             // subscribe
                true              // agree
        );

        clickContinueButton();

        assertRegistrationFailed("Регистрация должна быть провальной из-за наличия цифр в поле 'Имя'");
    }

    @Test
    void testPassword() {
        driver.get(REGISTER_URL);
        scrollDown(200);

        fillRegistrationForm(
                "Иван",                    // firstName
                "Иванов",                  // lastName
                "IIIvanIvan33333@mail.ru",  // email
                "Q1w2e3r4t5y6u7i8o9p0",   // password
                true,                      // subscribe
                true                       // agree
        );

        clickContinueButton();

        assertRegistrationSuccessful("Регистрация должна быть успешной с валидными данными");
    }

    private void fillRegistrationForm(String firstName, String lastName, String email,
                                      String password, boolean subscribe, boolean agree) {
        setInputValue(FIRST_NAME_INPUT, firstName);
        setInputValue(LAST_NAME_INPUT, lastName);
        setInputValue(EMAIL_INPUT, email);
        setInputValue(PASSWORD_INPUT, password);

        if (subscribe) {
            setCheckboxValue(NEWSLETTER_CHECKBOX, true);
        }

        if (agree) {
            setCheckboxValue(AGREE_CHECKBOX, true);
        }
    }

    private void setInputValue(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value);
    }

    private void setCheckboxValue(By locator, boolean checked) {
        WebElement checkbox = wait.until(ExpectedConditions.elementToBeClickable(locator));
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
    }

    private void clickContinueButton() {
        WebElement continueButton = wait.until(ExpectedConditions.elementToBeClickable(CONTINUE_BUTTON));
        continueButton.click();
    }

    private void scrollDown(int pixels) {
        actions.scrollByAmount(0, pixels).perform();
    }

    private void assertRegistrationFailed(String message) {
        boolean isFailed = driver.getCurrentUrl().contains("register") ||
                !driver.findElements(ERROR_ELEMENTS).isEmpty();
        assertTrue(isFailed, message);
    }

    private void assertRegistrationSuccessful(String message) {
        boolean isSuccessful = !driver.getCurrentUrl().contains("register") &&
                driver.findElements(ERROR_ELEMENTS).isEmpty();
        assertTrue(isSuccessful, message);
    }
}