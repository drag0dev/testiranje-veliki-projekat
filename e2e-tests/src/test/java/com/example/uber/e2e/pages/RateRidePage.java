package com.example.uber.e2e.pages;

import com.example.uber.e2e.support.TestConfig;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RateRidePage {

    private static final By DRIVER_RATING_INPUT = By.id("driverRating");
    private static final By VEHICLE_RATING_INPUT = By.id("vehicleRating");
    private static final By COMMENT_INPUT = By.id("comment");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type=submit]");
    private static final By SUCCESS_MESSAGE = By.cssSelector(".success");
    private static final By FIELD_ERROR = By.cssSelector(".field-error");
    private static final By FORM_ERROR = By.cssSelector(".form-error");
    private static final By HEADING = By.tagName("h1");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public RateRidePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public RateRidePage open(int rideId) {
        driver.get(TestConfig.BASE_URL + "/rides/" + rideId + "/rate");
        wait.until(ExpectedConditions.visibilityOfElementLocated(HEADING));
        return this;
    }

    public void rate(int driverRating, int vehicleRating, String comment) {
        setNumberField(DRIVER_RATING_INPUT, driverRating);
        setNumberField(VEHICLE_RATING_INPUT, vehicleRating);

        if (comment != null && !comment.isBlank()) {
            driver.findElement(COMMENT_INPUT).sendKeys(comment);
        }

        submit();
    }

    public void submitEmpty() {
        submit();
    }

    public boolean isSuccessShown() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_MESSAGE));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean hasFieldErrors() {
        return !driver.findElements(FIELD_ERROR).isEmpty();
    }

    public String waitForFormErrorText() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(FORM_ERROR));
        return error.getText();
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private void setNumberField(By locator, int value) {
        WebElement field = driver.findElement(locator);
        field.clear();
        field.sendKeys(String.valueOf(value));
    }

    private void submit() {
        driver.findElement(SUBMIT_BUTTON).click();
    }
}
