package com.requester.pingyou.cotrollers;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

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
    private ProgressBar progress;


    public void onPress() {
        log.debug("Button pressed");
        infoLabel.setText("Requests are started.");
        WebClient webClient = WebClient.create(url.getText());
        log.info("URL: " + url.getText());

        Alert oKAlert = new Alert(Alert.AlertType.INFORMATION, "Request finished");
        animateProgressBar();
        makeRequests(webClient, oKAlert);
        oKAlert.show();
    }


    private void makeRequests(WebClient webClient, Alert oKAlert) {
        HttpStatus status;
        int count = 0;
        log.debug("Doing the request");
        try {
            do {
                status = webClient.get().exchange().block().statusCode();
                count++;
                log.debug("count: " + count);
            }
            while (status != HttpStatus.OK && count <= Integer.valueOf(retrytimes.getText()));

            if (status == HttpStatus.OK) {
                oKAlert.setContentText("Status  ok");
            } else {
                oKAlert.setContentText("Status not ok");
            }

            log.debug("Status: " + status);
        } catch (Exception e) {
            oKAlert.setContentText("Error");
        }
    }

    private void animateProgressBar() {
        final Float[] values = new Float[]{-1.0f, 0f, 0.6f, 1.0f};
        final ProgressIndicator pins = new ProgressIndicator(values.length);

        for (int i = 0; i < values.length; i++) {
            progress.setProgress(values[i]);
            pins.setProgress(values[i]);
        }
    }
}