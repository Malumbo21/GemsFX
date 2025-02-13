package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.BeforeAfterViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A view capable of managing / displaying two nodes in such a way that the user can show more
 * or less of each node at the same time. This is very useful to display before and after scenarios.
 */
public class BeforeAfterView extends Control {

    private static final Orientation DEFAULT_ORIENTATION = Orientation.HORIZONTAL;
    private static final PseudoClass PSEUDO_CLASS_HORIZONTAL = PseudoClass.getPseudoClass("horizontal");
    private static final PseudoClass PSEUDO_CLASS_VERTICAL = PseudoClass.getPseudoClass("vertical");

    /**
     * Constructs a new view. the two nodes have to be set or bound later.
     *
     * @see #beforeProperty()
     * @see #afterProperty()
     */
    public BeforeAfterView() {
        getStyleClass().add("before-after-view");

        beforeProperty().addListener(it -> {
            Node node = getBefore();
            if (node != null) {
                node.setMouseTransparent(true);
            }
        });

        afterProperty().addListener(it -> {
            Node node = getAfter();
            if (node != null) {
                node.setMouseTransparent(true);
            }
        });

        setFocusTraversable(false);
        updatePseudoClass();
    }

    /**
     * Constructs a new view with the given before and after nodes.
     *
     * @param beforeNode the node showing the "before" state
     * @param afterNode the node showing the "after" state
     */
    public BeforeAfterView(Node beforeNode, Node afterNode) {
        this();
        setBefore(beforeNode);
        setAfter(afterNode);
    }

    /**
     * Constructs a new view with the given before and after images.
     *
     * @param beforeImage a "before" image that will be wrapped in an image view
     * @param afterImage an "after" image that will be wrapped in an image view
     */
    public BeforeAfterView(Image beforeImage, Image afterImage) {
        this(new ImageView(beforeImage), new ImageView(afterImage));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new BeforeAfterViewSkin(this);
    }

    private void updatePseudoClass() {
        pseudoClassStateChanged(PSEUDO_CLASS_HORIZONTAL, getOrientation().equals(Orientation.HORIZONTAL));
        pseudoClassStateChanged(PSEUDO_CLASS_VERTICAL, getOrientation().equals(Orientation.VERTICAL));
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(BeforeAfterView.class.getResource("before-after-view.css")).toExternalForm();
    }

    private final DoubleProperty dividerPosition = new SimpleDoubleProperty(this, "dividerPosition", .5);

    public final double getDividerPosition() {
        return dividerPosition.get();
    }

    /**
     * Stores the position of the divider within the value range of zero to 1.
     *
     * @return the divider position
     */
    public final DoubleProperty dividerPositionProperty() {
        return dividerPosition;
    }

    public final void setDividerPosition(double dividerPosition) {
        this.dividerPosition.set(dividerPosition);
    }

    private final ObjectProperty<Node> before = new SimpleObjectProperty<>(this, "before", new Label("Before"){
        {
            setPrefSize(600, 400);
            setStyle("-fx-background-color: red;");
        }
    });

    public final Node getBefore() {
        return before.get();
    }

    /**
     * Stores the node used for displaying the "before" state.
     *
     * @return the node used to visualize "before"
     */
    public final ObjectProperty<Node> beforeProperty() {
        return before;
    }

    public final void setBefore(Node before) {
        this.before.set(before);
    }

    private final ObjectProperty<Node> after = new SimpleObjectProperty<>(this, "after", new Label("After"){
        {
            setPrefSize(600, 400);
            setStyle("-fx-background-color: green;");
            setAlignment(Pos.CENTER_RIGHT);
        }
    });

    public final Node getAfter() {
        return after.get();
    }

    /**
     * Stores the node used for displaying the "after" state.
     *
     * @return the node used to visualize "after"
     */
    public final ObjectProperty<Node> afterProperty() {
        return after;
    }

    public final void setAfter(Node after) {
        this.after.set(after);
    }

    private ObjectProperty<Orientation> orientation;

    public final Orientation getOrientation() {
        return orientation == null ? DEFAULT_ORIENTATION : orientation.get();
    }

    /**
     * Sets the orientation of the before / after view.
     * <p>
     * Default value is {@link Orientation#HORIZONTAL}
     *
     * @return the orientation property
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new StyleableObjectProperty<>(DEFAULT_ORIENTATION) {

                @Override
                protected void invalidated() {
                    updatePseudoClass();
                }

                @Override
                public Object getBean() {
                    return BeforeAfterView.this;
                }

                @Override
                public String getName() {
                    return "orientation";
                }

                @Override
                public CssMetaData<? extends Styleable, Orientation> getCssMetaData() {
                    return StyleableProperties.ORIENTATION;
                }
            };
        }
        return orientation;
    }

    public final void setOrientation(Orientation orientation) {
        orientationProperty().set(orientation);
    }

    private static class StyleableProperties {

        private static final CssMetaData<BeforeAfterView, Orientation> ORIENTATION =
                new CssMetaData<>("-fx-orientation", new EnumConverter<>(Orientation.class), DEFAULT_ORIENTATION) {
                    @Override
                    public boolean isSettable(BeforeAfterView view) {
                        return view.orientation == null || !view.orientation.isBound();
                    }

                    @Override
                    public StyleableProperty<Orientation> getStyleableProperty(BeforeAfterView view) {
                        return (StyleableProperty<Orientation>) view.orientationProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ORIENTATION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return BeforeAfterView.StyleableProperties.STYLEABLES;
    }
}
