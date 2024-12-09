package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PagingControlsSkin;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

/**
 * A control for navigating paged information, for example, a paged database table view.
 */
public class PagingControls extends PagingControlBase {

    private static final String DEFAULT_STYLE_CLASS = "paging-controls";

    /**
     * Constructs a new instance.
     */
    public PagingControls() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> requestFocus());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingControlsSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingControls.class.getResource("paging-controls.css")).toExternalForm();
    }
}
