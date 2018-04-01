package com.requester.pingyou.cotrollers;

import de.felixroske.jfxsupport.FXMLController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@FXMLController
public class FirstTestController {

    @FXML
    private Label infoLabel = new Label("Specify retry times and url.");

    @FXML
    private Button button;

    @FXML
    private TextField url;

    @FXML
    private TextField retrytimes;
    @FXML
    private TextField retryInterval;
    @FXML
    private TextField cookie;
    @FXML
    private TextField expectedStatus;
    @FXML
    private TextField expectedText;
    @FXML
    private Text rcount;

    @FXML
    private ProgressBar progress;

    private AtomicReference<String> atomicStringReference =
            new AtomicReference<String>();
    String response;


    public void onPress() throws ExecutionException, InterruptedException {
        log.debug("Button pressed");
        button.setDisable(true);

        infoLabel.setText("Requests are started.");

        WebClient webClient = createWebClient();
        log.info("URL: " + url.getText());

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                atomicStringReference.set(makeRequests(webClient));
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Alert oKAlert = new Alert(Alert.AlertType.INFORMATION, "Request finished");
            oKAlert.setContentText(atomicStringReference.get());
            oKAlert.show();

            Stage stage = (Stage) oKAlert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            button.setDisable(false);
        });
        new Thread(task).start();
    }

    private WebClient createWebClient() {
        String[] cookie = this.cookie.getText().split("=");

        WebClient.Builder builder = WebClient.builder();

        if (cookie.length == 2) {
            builder.defaultCookie(cookie[0], cookie[1]);
        }
        return builder.baseUrl(url.getText()).build();
    }


    private String makeRequests(WebClient webClient) {
        HttpStatus status;
        String statusReturn;
        int count = 0;
        log.debug("Doing the request");
        HttpStatus expectedHttpStatus = (expectedStatus.getText().length() != 0)
                ? HttpStatus.valueOf(Integer.valueOf(expectedStatus.getText())) : HttpStatus.OK;
        try {
            do {
                ClientResponse response = doRequest(webClient);
                status = response.statusCode();
                Mono<String> fluxString = response.bodyToMono(String.class);
                status = checkResponse(fluxString, status);
                count++;
                rcount.setText(String.valueOf(count) + " HTTP Status: " + status);
                log.debug("count: " + count);
                Thread.sleep(Integer.valueOf(retryInterval.getText()) * 1000);
            }
            while (status != expectedHttpStatus && count < Integer.valueOf(retrytimes.getText()));
            button.setDisable(false);
            statusReturn = setStatusText(status, expectedHttpStatus);
            log.debug("Status: " + status);
        } catch (Exception e) {
            log.error("Error doing the request", e);
            statusReturn = "Error";
        }
        return statusReturn;
    }

    private HttpStatus checkResponse(Mono<String> response, HttpStatus status) {
        if (expectedText.getText().length() > 0) {
            Optional<String> check = Optional.ofNullable(response
                    .filter(s -> s.contains(expectedText.getText()))
                    .block());
            if (!check.isPresent()) {
                return HttpStatus.BAD_REQUEST;
            }
        }
        return status;
    }

    private ClientResponse doRequest(WebClient webClient) {
        return webClient.get()
                .exchange()
                .block();
    }

    private String setStatusText(HttpStatus status, HttpStatus expectedHttpStatus) {
        if (status == expectedHttpStatus) {
            return "Status  ok";
        } else {
            return "Status not ok";
        }
    }

    private void animateProgressBar() {
        final Float[] values = new Float[]{-1.0f, 0f, 0.6f, 1.0f};
        final ProgressIndicator pins = new ProgressIndicator(values.length);

        for (Float value : values) {
            progress.setProgress(value);
            pins.setProgress(value);
        }
    }
}
