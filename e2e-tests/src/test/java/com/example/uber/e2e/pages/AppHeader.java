package com.example.uber.e2e.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AppHeader {

    private static final By LOGOUT_BUTTON = By.cssSelector("nav button");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public AppHeader(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BUTTON));
    }

    public void logout() {
        driver.findElement(LOGOUT_BUTTON).click();
    }
}
