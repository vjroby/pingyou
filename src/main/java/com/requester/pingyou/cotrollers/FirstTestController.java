package com.requester.pingyou.cotrollers;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

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
        WebClient webClient = WebClient.create(url.getText());
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
        });

        new Thread(task).start();
    }


    private String makeRequests(WebClient webClient) {
        HttpStatus status;
        String statusReturn;
        int count = 0;
        log.debug("Doing the request");
        try {
            do {
                status = doRequest(webClient);
                count++;
                rcount.setText(String.valueOf(count) + " HTTP Status: " + status);
                log.debug("count: " + count);
                Thread.sleep(Integer.valueOf(retryInterval.getText()) * 1000);
            }
            while (status != HttpStatus.OK && count < Integer.valueOf(retrytimes.getText()));
            button.setDisable(false);
            statusReturn = setStatusText(status);
            log.debug("Status: " + status);
        } catch (Exception e) {
            statusReturn = "Error";
        }
        return statusReturn;
    }

    private HttpStatus doRequest(WebClient webClient) {
        try {
            return webClient.get().exchange().block().statusCode();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String setStatusText(HttpStatus status) {
        if (status == HttpStatus.OK) {
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
