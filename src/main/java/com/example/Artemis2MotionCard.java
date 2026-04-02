package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Artemis2MotionCard extends VBox {
    private static final String API_KEY = "QeXWQrOtzfrEovifTnM5k66fuBc0MjIPBeDUxxzi";
    private static final String TECHPORT_SEARCH_URL =
        "https://api.nasa.gov/techport/api/projects/search?api_key=" + API_KEY;

    private final Label positionValue = new Label("Loading...");
    private final Label speedValue = new Label("Loading...");
    private final Label trajectoryValue = new Label("Loading...");
    private final Label statusValue = new Label("Contacting NASA Open APIs...");

    public Artemis2MotionCard() {
        setSpacing(10);
        setPadding(new Insets(16));
        setStyle(
            "-fx-background-color: #F7F9FC;"
                + "-fx-border-color: #D6DFE8;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );

        Label titleLabel = new Label("Artemis II Motion");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        positionValue.setWrapText(true);
        speedValue.setWrapText(true);
        trajectoryValue.setWrapText(true);
        statusValue.setWrapText(true);

        getChildren().addAll(
            titleLabel,
            buildMetricRow("Position", positionValue),
            buildMetricRow("Speed", speedValue),
            buildMetricRow("Trajectory", trajectoryValue),
            statusValue
        );

        refreshData();
    }

    private VBox buildMetricRow(String label, Label valueLabel) {
        Label heading = new Label(label);
        heading.setFont(Font.font("System", FontWeight.BOLD, 14));
        valueLabel.setFont(Font.font(13));
        return new VBox(4, heading, valueLabel);
    }

    private void refreshData() {
        CompletableFuture
            .supplyAsync(this::fetchArtemisMotionData)
            .thenAccept(data -> Platform.runLater(() -> applyData(data)));
    }

    private MotionData fetchArtemisMotionData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TECHPORT_SEARCH_URL))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return MotionData.error("NASA API request failed with status " + response.statusCode());
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode results = root.path("results");

            String matchedProject = null;
            if (results.isArray()) {
                for (JsonNode node : results) {
                    String title = node.path("title").asText("");
                    if (title.toLowerCase().contains("artemis")) {
                        matchedProject = title;
                        break;
                    }
                }
            }

            String statusMessage;
            if (matchedProject != null) {
                statusMessage = "Fetched from api.nasa.gov (TechPort): " + matchedProject;
            } else {
                statusMessage = "Fetched from api.nasa.gov (TechPort). No Artemis telemetry fields available.";
            }

            // NASA's current public api.nasa.gov endpoints do not expose Artemis II
            // live state vectors (position/speed/trajectory) directly.
            return new MotionData(
                "Not published on api.nasa.gov for Artemis II live tracking.",
                "Not published on api.nasa.gov for Artemis II live tracking.",
                "Awaiting AROW state vectors/ephemeris feed after mission tracking begins.",
                statusMessage + " Last refresh: " + utcNow()
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return MotionData.error("Unable to reach NASA API: " + ex.getMessage());
        } catch (IOException ex) {
            return MotionData.error("Unable to reach NASA API: " + ex.getMessage());
        }
    }

    private void applyData(MotionData data) {
        positionValue.setText(data.position);
        speedValue.setText(data.speed);
        trajectoryValue.setText(data.trajectory);
        statusValue.setText(data.status);
    }

    private String utcNow() {
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(Instant.now());
    }

    private static class MotionData {
        private final String position;
        private final String speed;
        private final String trajectory;
        private final String status;

        private MotionData(String position, String speed, String trajectory, String status) {
            this.position = position;
            this.speed = speed;
            this.trajectory = trajectory;
            this.status = status;
        }

        private static MotionData error(String reason) {
            return new MotionData(
                "Unavailable",
                "Unavailable",
                "Unavailable",
                reason
            );
        }
    }
}
