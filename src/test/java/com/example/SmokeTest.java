package com.example;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.function.Supplier;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.junit5.BrowserPerTestStrategyExtension;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.CollectionCondition.exactTexts;
import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.shadowCss;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;


@QuarkusTest
@ExtendWith({BrowserPerTestStrategyExtension.class})
class SmokeTest {

    @TestHTTPResource()
    private String baseURL;

    @BeforeEach
    void setup() {
        Configuration.headless = runHeadless();
        System.setProperty("chromeoptions.args", "--remote-allow-origins=*");
    }

    @Test
    void rootPath_mainViewDisplayed() {
        openAndWait(() -> $("vaadin-app-layout"));
        $("section.view vaadin-text-field").shouldBe(visible);
        $$("section.view vaadin-button").filter(Condition.text("Say Hello"))
                .first().shouldBe(visible);
    }

    @Test
    void mainView_sayHelloButtonClicked_notificationShown() {
        openAndWait(() -> $("vaadin-app-layout"));

        SelenideElement textField = $("section.view vaadin-text-field").shouldBe(visible);
        SelenideElement button = $$("section.view vaadin-button")
                .filter(Condition.text("Say Hello"))
                .first().shouldBe(visible);

        button.click();
        ElementsCollection messages = $$("section.view li.message")
                .shouldHave(size(1))
                .shouldHave(exactTexts("Hello stranger"));

        String name = "John";
        textField.$("input").setValue(name);
        button.click();

        messages.shouldHave(size(2))
                .shouldHave(exactTexts("Hello John", "Hello stranger"));
    }


    protected void openAndWait(Supplier<SelenideElement> selector) {
        openAndWait(baseURL, selector);
    }

    protected void openAndWait(String url, Supplier<SelenideElement> selector) {
        Selenide.open(url);
        waitForDevServer();
        selector.get().shouldBe(Condition.visible, Duration.ofSeconds(10));
        $(shadowCss("div.dev-tools.error", "vaadin-dev-tools")).shouldNot(Condition.exist);
        $(shadowCss("main", "vite-plugin-checker-error-overlay")).shouldNot(Condition.exist);
    }

    protected void waitForDevServer() {
        Selenide.Wait()
                .withTimeout(Duration.ofMinutes(20))
                .until(d -> !Boolean.TRUE.equals(Selenide.executeJavaScript(
                        "return window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.devServerIsNotLoaded;")));
    }

    protected boolean runHeadless() {
        return !isJavaInDebugMode();
    }

    static boolean isJavaInDebugMode() {
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .toString()
                .contains("jdwp");
    }

}
