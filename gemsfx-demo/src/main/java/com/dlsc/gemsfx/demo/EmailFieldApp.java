package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EmailField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Random;

public class EmailFieldApp extends GemApplication {

    private final Random random = new Random();

    @Override
    public void start(Stage stage) { super.start(stage);
        EmailField view = new EmailField();
        view.setPromptText("Enter an email address");

        CheckBox required = new CheckBox("Required");
        required.selectedProperty().bindBidirectional(view.requiredProperty());

        // When user types '@' in the email field, show a list of suggestions
        CheckBox autoCompletion = new CheckBox("Auto-Complete Domain");
        autoCompletion.selectedProperty().bindBidirectional(view.autoDomainCompletionEnabledProperty());

        CheckBox multipleAddresses = new CheckBox("Multiple addresses");
        multipleAddresses.selectedProperty().bindBidirectional(view.supportingMultipleAddressesProperty());

        CheckBox enableCustomCell = new CheckBox("Enable Custom Cell");
        enableCustomCell.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                view.setDomainListCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item + "     (Custom)");
                            setTextFill(Color.rgb(random.nextInt(210), random.nextInt(210), random.nextInt(210)));
                            setGraphic(new FontIcon(MaterialDesign.MDI_EMAIL));
                        }
                    }
                });
            } else {
                view.setDomainListCellFactory(null);
            }
        });

        CheckBox showMailIcon = new CheckBox("Show Mail Icon");
        showMailIcon.selectedProperty().bindBidirectional(view.showMailIconProperty());

        CheckBox showValidationIcon = new CheckBox("Show Validation Icon");
        showValidationIcon.selectedProperty().bindBidirectional(view.showValidationIconProperty());

        TextField invalidTextField = new TextField(view.getInvalidText());
        invalidTextField.setPromptText("Invalid text for the tooltip");
        view.invalidTextProperty().bind(invalidTextField.textProperty());

        Label resultLabel = new Label("Entered address(es)");
        resultLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (view.isValid()) {
                if (view.isSupportingMultipleAddresses()) {
                    return String.join(", ", view.getMultipleEmailAddresses());
                }
                return view.getEmailAddress();
            } else {
                if (view.isSupportingMultipleAddresses()) {
                    return "Invalid: " + String.join(", ", view.getMultipleEmailAddresses());
                }
                return "Invalid " + view.getEmailAddress();
            }
        }, view.supportingMultipleAddressesProperty(), view.emailAddressProperty(), view.getMultipleEmailAddresses()));

        VBox topBox = new VBox(10, required, autoCompletion, multipleAddresses, enableCustomCell, showMailIcon, showValidationIcon, new Label("Text to show when invalid:"), invalidTextField);

        VBox box = new VBox(20, topBox, view, resultLabel);
        box.setPrefWidth(300);
        box.setPadding(new Insets(10));
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle("Email Field");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
