package com.example.uber.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.uber.e2e.pages.LoginPage;
import com.example.uber.e2e.pages.RateRidePage;
import com.example.uber.e2e.support.TestDatabase;
import com.example.uber.e2e.support.TestUsers;
import com.example.uber.e2e.support.WebDriverFactory;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

class RatingE2ETest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        driver = WebDriverFactory.createChromeDriver();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    void takeAScreenShot(String testName) {
        TakesScreenshot ts = (TakesScreenshot)driver;
        File file = ts.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(file, new File("./ScreenShot_Folder/" + testName + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void happyPath_ratesAFinishedRideSuccessfully() {
        int rideId = TestDatabase.findUnratedFinishedRideId(TestUsers.BOB_EMAIL);

        new LoginPage(driver).open().loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);
        RateRidePage ratePage = new RateRidePage(driver).open(rideId);
        ratePage.rate(5, 4, "Great ride, thanks!");

        assertTrue(ratePage.isSuccessShown(), "Expected the success message after rating");
        takeAScreenShot("happyPath");
    }

    @Test
    void emptyForm_isRejectedClientSideWithoutHittingTheServer() {
        int rideId = TestDatabase.findRideIdByStatus(TestUsers.BOB_EMAIL, "FINISHED");

        new LoginPage(driver).open().loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);
        RateRidePage ratePage = new RateRidePage(driver).open(rideId);
        ratePage.submitEmpty();

        assertTrue(ratePage.hasFieldErrors(), "Expected validation errors for the empty rating fields");
        assertFalse(ratePage.isSuccessShown(), "Submission should have been blocked client-side");
        takeAScreenShot("emptyForm");
    }

    @Test
    void ratingARideThatHasNotFinishedYet_isRejectedByTheServer() {
        int activeRideId = TestDatabase.findRideIdByStatus(TestUsers.BOB_EMAIL, "ACTIVE");

        new LoginPage(driver).open().loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);
        RateRidePage ratePage = new RateRidePage(driver).open(activeRideId);
        ratePage.rate(5, 5, "n/a");

        String errorText = ratePage.waitForFormErrorText().toLowerCase();
        assertTrue(errorText.contains("finish"), "Expected a not-finished-yet error, got: " + errorText);
        takeAScreenShot("ratingARideThatHasNotFinishedYet");
    }

    @Test
    void ratingANonExistentRideId_showsANotFoundError() {
        int bogusRideId = 999_999;

        new LoginPage(driver).open().loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);
        RateRidePage ratePage = new RateRidePage(driver).open(bogusRideId);
        ratePage.rate(5, 5, "n/a");

        String errorText = ratePage.waitForFormErrorText().toLowerCase();
        assertTrue(errorText.contains("not found"), "Expected a not-found error, got: " + errorText);
        takeAScreenShot("ratingANonExistentRideId");
    }

    @Test
    void ratingAnotherPassengersRide_isForbidden() {
        int alicesRideId = TestDatabase.findRideIdByStatus(TestUsers.ALICE_EMAIL, "FINISHED");

        new LoginPage(driver).open().loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);
        RateRidePage ratePage = new RateRidePage(driver).open(alicesRideId);
        ratePage.rate(5, 5, "not my ride");

        String errorText = ratePage.waitForFormErrorText().toLowerCase();
        assertTrue(errorText.contains("passenger"), "Expected an ownership error, got: " + errorText);
        takeAScreenShot("ratingAnotherPassengersRide");
    }

    @Test
    void ratingTheSameRideTwice_rejectsTheSecondAttempt() {
        int rideId = TestDatabase.findUnratedFinishedRideId(TestUsers.CAROL_EMAIL);

        new LoginPage(driver).open().loginAs(TestUsers.CAROL_EMAIL, TestUsers.PASSWORD);

        RateRidePage firstAttempt = new RateRidePage(driver).open(rideId);
        firstAttempt.rate(4, 4, "First rating");
        assertTrue(firstAttempt.isSuccessShown(), "First submission should succeed");

        RateRidePage secondAttempt = new RateRidePage(driver).open(rideId);
        secondAttempt.rate(2, 2, "Trying again");

        String errorText = secondAttempt.waitForFormErrorText().toLowerCase();
        assertTrue(errorText.contains("already"), "Expected an already-rated error, got: " + errorText);
        takeAScreenShot("ratingTheSameRideTwice");
    }

    @Test
    void ratingPageWhileLoggedOut_redirectsThroughLoginAndBack() {
        int rideId = TestDatabase.findRideIdByStatus(TestUsers.BOB_EMAIL, "FINISHED");

        RateRidePage ratePage = new RateRidePage(driver).open(rideId);
        assertTrue(ratePage.currentUrl().contains("/login"), "Expected authGuard to redirect to /login");

        new LoginPage(driver).loginAs(TestUsers.BOB_EMAIL, TestUsers.PASSWORD);

        assertTrue(
                ratePage.currentUrl().contains("/rides/" + rideId + "/rate"),
                "Expected the returnUrl redirect to land back on the rate page");
        takeAScreenShot("ratingPageWhileLoggedOut");
    }
}
