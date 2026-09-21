package com.dlsc.gemsfx;

import javafx.util.Duration;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DialogPane}.
 */
public class DialogPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.getStyleClass().contains("dialog-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        DialogPane pane = invoke(DialogPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultAnimateDialogs() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.isAnimateDialogs());
    }

    @Test
    public void defaultFadeInOut() {
        DialogPane pane = invoke(DialogPane::new);
        assertTrue(pane.isFadeInOut());
    }

    @Test
    public void defaultNotShowingDialog() {
        DialogPane pane = invoke(DialogPane::new);
        assertFalse(pane.isShowingDialog());
    }

    @Test
    public void defaultAnimationDuration() {
        DialogPane pane = invoke(DialogPane::new);
        assertEquals(Duration.millis(100), pane.getAnimationDuration());
    }

    @Test
    public void getGlassPaneNotNull() {
        DialogPane pane = invoke(DialogPane::new);
        assertNotNull(pane.getGlassPane());
    }

    @Test
    public void showDialogAddsToList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        // Turn off animations so dialog is added synchronously
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("Title", "Message"));
        assertFalse(pane.getDialogs().isEmpty());
    }

    @Test
    public void hideDialogRemovesFromList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("Title", "Message"));
        runFx(() -> pane.hideDialog(dialog));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
    }

    @Test
    public void hideAllDialogsClearsAll() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> {
            pane.setAnimateDialogs(false);
            pane.showWarning("A", "msg");
            pane.showWarning("B", "msg");
        });
        runFx(pane::hideAllDialogs);
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
    }

    @Test
    public void showingDialogBindsToDialogList() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> d = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(pane.isShowingDialog());
        runFx(() -> pane.hideDialog(d));
        waitForFxEvents();
        assertFalse(pane.isShowingDialog());
    }

    @Test
    public void dialogWithCancelButtonIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void confirmationDialogIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showConfirmation("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void singleButtonDialogIsCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showInformation("T", "M"));
        assertTrue(dialog.isCancellable());
    }

    @Test
    public void dialogWithoutButtonsIsNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Collections.emptyList()));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void dialogWithMultipleNonCancelButtonsIsNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Arrays.asList(ButtonType.OK, ButtonType.APPLY)));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void requestCancelDoesNothingWhenNotCancellable() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<Void> dialog = invoke(() -> pane.showNode(DialogPane.Type.BLANK, "T", new Label("content"), Collections.emptyList()));
        assertFalse(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertFalse(pane.getDialogs().isEmpty());
    }

    @Test
    public void requestCancelCommitsCancelWhenCancelButtonPresent() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        List<ButtonType> pressed = new ArrayList<>();
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        runFx(() -> dialog.setOnClose(pressed::add));
        assertTrue(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
        assertEquals(Collections.singletonList(ButtonType.CANCEL), pressed);
    }

    @Test
    public void requestCancelPressesSingleButton() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        List<ButtonType> pressed = new ArrayList<>();
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showInformation("T", "M"));
        runFx(() -> dialog.setOnClose(pressed::add));
        assertTrue(invoke(dialog::requestCancel));
        waitForFxEvents();
        assertTrue(pane.getDialogs().isEmpty());
        assertEquals(Collections.singletonList(ButtonType.OK), pressed);
    }

    @Test
    public void cancellableUpdatesWhenButtonTypesChange() {
        DialogPane pane = layout(invoke(DialogPane::new));
        runFx(() -> pane.setAnimateDialogs(false));
        DialogPane.Dialog<ButtonType> dialog = invoke(() -> pane.showWarning("T", "M"));
        assertTrue(dialog.isCancellable());
        runFx(() -> dialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.APPLY));
        assertFalse(dialog.isCancellable());
    }

    @Test
    public void animateDialogsPropertyRoundTrip() {
        DialogPane pane = invoke(DialogPane::new);
        runFx(() -> pane.setAnimateDialogs(false));
        assertFalse(pane.isAnimateDialogs());
    }
}
