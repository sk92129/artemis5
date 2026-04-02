package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DashboardScreen extends Application {
    private static final double CARD_MIN_WIDTH = 300;
    private double zoomFactor = 1.0;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar(stage, root));
        root.setCenter(createDashboardContent());
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 1100, 680);
        stage.setTitle("Artemis Mission Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar(Stage stage, BorderPane root) {
        Menu fileMenu = new Menu("File");
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(event -> {
            Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
            saveAlert.setTitle("Save");
            saveAlert.setHeaderText("Dashboard State");
            saveAlert.setContentText("Mission dashboard preferences saved.");
            saveAlert.showAndWait();
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(event -> stage.close());
        fileMenu.getItems().addAll(saveItem, new SeparatorMenuItem(), exitItem);

        Menu viewMenu = new Menu("View");
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setOnAction(event -> applyZoom(root, 0.1));
        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setOnAction(event -> applyZoom(root, -0.1));
        viewMenu.getItems().addAll(zoomInItem, zoomOutItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
            aboutAlert.setTitle("About");
            aboutAlert.setHeaderText("Artemis Mission Dashboard");
            aboutAlert.setContentText(
                "A JavaFX desktop dashboard showing Artemis program highlights."
            );
            aboutAlert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);

        return new MenuBar(fileMenu, viewMenu, helpMenu);
    }

    private HBox createDashboardContent() {
        HBox columns = new HBox(16);
        columns.setPadding(new Insets(16));
        columns.setAlignment(Pos.TOP_CENTER);

        VBox missionOverviewCard = createMissionOverviewCard();

        Artemis2MotionCard artemis2MotionCard = new Artemis2MotionCard();

        VBox goalsCard = createCard(
            "Science & Future Goals",
            "Artemis missions will investigate lunar ice, deploy surface science "
                + "instruments, and test technologies for sustainable exploration. "
                + "Lessons learned directly support eventual crewed Mars expeditions."
        );

        configureResizableCard(missionOverviewCard);
        configureResizableCard(artemis2MotionCard);
        configureResizableCard(goalsCard);

        columns.getChildren().addAll(missionOverviewCard, artemis2MotionCard, goalsCard);
        return columns;
    }

    private VBox createMissionOverviewCard() {
        VBox card = createCard(
            "Mission Overview",
            "NASA's Artemis program returns humans to the Moon, establishes long-term "
                + "lunar presence, and prepares for future missions to Mars. Artemis I "
                + "validated deep-space systems with an uncrewed Orion flight."
        );

        var imageUrl = getClass().getResource("/0525-cw-news-nasa-orion-breakdown.png");
        if (imageUrl != null) {
            Image missionImage = new Image(imageUrl.toExternalForm());
            ImageView missionImageView = new ImageView(missionImage);
            missionImageView.setPreserveRatio(true);
            missionImageView.setFitWidth(360);
            missionImageView.setSmooth(true);
            card.getChildren().add(1, missionImageView);
        } else {
            Label imageMissing = new Label("Mission image could not be loaded.");
            imageMissing.setFont(Font.font(13));
            card.getChildren().add(1, imageMissing);
        }
        return card;
    }

    private void configureResizableCard(Region card) {
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(CARD_MIN_WIDTH);
        card.setMaxWidth(Double.MAX_VALUE);
    }

    private VBox createCard(String title, String bodyText) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        Label bodyLabel = new Label(bodyText);
        bodyLabel.setWrapText(true);
        bodyLabel.setFont(Font.font(14));

        VBox card = new VBox(12, titleLabel, bodyLabel);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: #F7F9FC;"
                + "-fx-border-color: #D6DFE8;"
                + "-fx-border-radius: 8;"
                + "-fx-background-radius: 8;"
        );
        return card;
    }

    private void applyZoom(BorderPane root, double delta) {
        zoomFactor = Math.max(0.6, Math.min(1.8, zoomFactor + delta));
        root.setScaleX(zoomFactor);
        root.setScaleY(zoomFactor);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
