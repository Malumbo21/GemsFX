package com.dlsc.gemsfx;

import com.dlsc.gemsfx.LoadingPane.Status;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.gridtable.GridTableView;
import com.dlsc.gemsfx.skins.PagingGridTableViewSkin;
import com.dlsc.gemsfx.skins.PagingListViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PagingGridTableView<T> extends PagingControlBase {

    private final LoadingService loadingService = new LoadingService();

    private final ObservableList<T> items = FXCollections.observableArrayList();

    private final ObservableList<T> unmodifiableItems = FXCollections.unmodifiableObservableList(items);

    private final GridTableView<T> gridTableView = new GridTableView<>();

    private final InvalidationListener updateListener = (Observable it) -> refresh();

    private final WeakInvalidationListener weakUpdateListener = new WeakInvalidationListener(updateListener);

    public PagingGridTableView() {
        getStyleClass().add("paging-grid-table-view");

        gridTableView.columnsProperty().bind(columnsProperty());
        gridTableView.setItems(items);
        gridTableView.minNumberOfRowsProperty().bind(Bindings.createIntegerBinding(() -> {
            if (isFillLastPage()) {
                return getPageSize();
            }
            return 0;
        }, fillLastPageProperty(), pageSizeProperty()));

        loadingService.setOnSucceeded(evt -> {
            loadingStatus.set(Status.OK);
            List<T> newList = loadingService.getValue();
            if (newList != null) {
                items.setAll(newList);
            } else {
                items.clear();
            }
        });

        loadingService.setOnRunning(evt -> loadingStatus.set(Status.LOADING));
        loadingService.setOnFailed(evt -> loadingStatus.set(Status.ERROR));

        InvalidationListener loadListener = it -> reload();
        pageProperty().addListener(loadListener);
        pageSizeProperty().addListener(loadListener);
        totalItemCountProperty().addListener(loadListener);
        loaderProperty().addListener(loadListener);

        unmodifiableItems.addListener(weakUpdateListener);

        pageSizeProperty().addListener(weakUpdateListener);
        pageProperty().addListener(weakUpdateListener);
        fillLastPageProperty().addListener(weakUpdateListener);
        totalItemCountProperty().addListener(weakUpdateListener);

        pagingControlsLocation.addListener((it, oldLocation, newLocation) -> {
            if (newLocation.equals(Side.LEFT) || newLocation.equals(Side.RIGHT)) {
                setPagingControlsLocation(Side.BOTTOM);
                throw new IllegalArgumentException("unsupported location for the paging controls: " + newLocation);
            }
        });

        loadDelayInMillis.addListener(it -> {
            if (getLoadDelayInMillis() < 0) {
                throw new IllegalArgumentException("load delay must be >= 0");
            }
        });

        setPlaceholder(new Label("No items"));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingGridTableViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingGridTableView.class.getResource("paging-grid-table-view.css")).toExternalForm();
    }

    private final ListProperty<GridTableColumn<T, ?>> columns = new SimpleListProperty<>(this, "columns", FXCollections.observableArrayList());

    public final ListProperty<GridTableColumn<T, ?>> columnsProperty() {
        return this.columns;
    }

    public final ObservableList<GridTableColumn<T, ?>> getColumns() {
        return this.columns.get();
    }

    public final void setColumns(ObservableList<GridTableColumn<T, ?>> columns) {
        this.columns.set(columns);
    }

    /**
     * Returns the wrapped list view.
     *
     * @return the list view
     */
    public final GridTableView<T> getGridTableView() {
        return gridTableView;
    }

    private final ObjectProperty<Side> pagingControlsLocation = new SimpleObjectProperty<>(this, "pagingControlsLocation", Side.BOTTOM);

    public final Side getPagingControlsLocation() {
        return pagingControlsLocation.get();
    }

    /**
     * Controls on which side the paging controls should be located. Currently only {@link Side#TOP} and
     * {@link Side#BOTTOM} are supported.
     *
     * @return the location where the paging controls will be shown
     */
    public final ObjectProperty<Side> pagingControlsLocationProperty() {
        return pagingControlsLocation;
    }

    public final void setPagingControlsLocation(Side pagingControlsLocation) {
        this.pagingControlsLocation.set(pagingControlsLocation);
    }

    private final BooleanProperty showPagingControls = new SimpleBooleanProperty(this, "showPagingControls", true);

    public final boolean isShowPagingControls() {
        return showPagingControls.get();
    }

    /**
     * A flag used to control the visibility of the paging controls (page buttons, previous, next, etc...).
     *
     * @return a property that is true if the paging controls should be visible
     */
    public final BooleanProperty showPagingControlsProperty() {
        return showPagingControls;
    }

    public final void setShowPagingControls(boolean showPagingControls) {
        this.showPagingControls.set(showPagingControls);
    }

    private final BooleanProperty fillLastPage = new SimpleBooleanProperty(this, "fillLastPage", false);

    public final boolean isFillLastPage() {
        return fillLastPage.get();
    }

    /**
     * The list view might not have enough data to fill its last page with items / cells. This flag can be used
     * to control whether we want the view to become smaller because of missing items or if we want the view to
     * fill the page with empty cells.
     *
     * @return a flag used to control whether the last page will be filled with empty cells if needed
     */
    public final BooleanProperty fillLastPageProperty() {
        return fillLastPage;
    }

    public final void setFillLastPage(boolean fillLastPage) {
        this.fillLastPage.set(fillLastPage);
    }

    private final ObjectProperty<Status> loadingStatus = new SimpleObjectProperty<>(this, "loadingStatus", Status.OK);

    public final Status getLoadingStatus() {
        return loadingStatus.get();
    }

    /**
     * The loading status used for the wrapped {@link LoadingPane}. The loading pane will appear if the
     * loader takes a long time to return the new page items.
     *
     * @return the loading status
     */
    public final ObjectProperty<Status> loadingStatusProperty() {
        return loadingStatus;
    }

    public final void setLoadingStatus(Status loadingStatus) {
        this.loadingStatus.set(loadingStatus);
    }

    private final LongProperty loadDelayInMillis = new SimpleLongProperty(this, "loadDelayInMillis", 200L);

    public final long getLoadDelayInMillis() {
        return loadDelayInMillis.get();
    }

    /**
     * The delay in milliseconds before the loading service will actually try to retrieve the data from (for example)
     * a backend. This delay is around a few hundred milliseconds by default. Delaying the loading has the advantage
     * that sudden property changes will not trigger multiple backend queries but will get batched together to a single
     * reload operation.
     *
     * @return the delay before data will actually be loaded
     */
    public final LongProperty loadDelayInMillisProperty() {
        return loadDelayInMillis;
    }

    public final void setLoadDelayInMillis(long loadDelayInMillis) {
        this.loadDelayInMillis.set(loadDelayInMillis);
    }

    private final LongProperty commitLoadStatusDelay = new SimpleLongProperty(this, "commitLoadStatusDelay", 400L);

    public final long getCommitLoadStatusDelay() {
        return commitLoadStatusDelay.get();
    }

    /**
     * The delay in milliseconds before the list view will display the progress indicator for long running
     * load operations.
     *
     * @return the commit delay for the nested loading pane
     * @see LoadingPane#commitDelayProperty()
     */
    public final LongProperty commitLoadStatusDelayProperty() {
        return commitLoadStatusDelay;
    }

    public final void setCommitLoadStatusDelay(long commitLoadStatusDelay) {
        this.commitLoadStatusDelay.set(commitLoadStatusDelay);
    }

    private class LoadingService extends Service<List<T>> {

        @Override
        protected Task<List<T>> createTask() {
            return new Task<>() {

                final LoadRequest loadRequest = new LoadRequest(getPage(), getPageSize());

                @Override
                protected List<T> call() {
                    try {
                        Thread.sleep(getLoadDelayInMillis());
                    } catch (InterruptedException e) {
                        // do nothing
                    }

                    if (!isCancelled()) {
                        Callback<LoadRequest, List<T>> loader = PagingGridTableView.this.loader.get();
                        if (loader != null) {
                            /*
                             * Important to wrap in a list, otherwise we can get a concurrent modification
                             * exception when the result gets applied to the "items" list in the service
                             * event handler for "success". Not sure why this fixes that issue.
                             */
                            return new ArrayList<>(loader.call(loadRequest));
                        }
                    }

                    return Collections.emptyList();
                }
            };
        }
    }

    /**
     * Triggers an explicit reload of the list view.
     */
    public final void reload() {
        loadingService.restart();
    }

    /**
     * Returns an unmodifiable observable list with the items shown by the current
     * page.
     *
     * @return the currently shown items
     */
    public final ObservableList<T> getUnmodifiableItems() {
        return unmodifiableItems;
    }

    /**
     * The input parameter for the loader callback.
     *
     * @see #loaderProperty()
     */
    public static class LoadRequest {

        private final int page;
        private final int pageSize;

        /**
         * Constructs a new load request for the given page and page size.
         *
         * @param page     the index of the page (starts with 0)
         * @param pageSize the size of the page (number of items per page)
         */
        public LoadRequest(int page, int pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }

        /**
         * The index of the page.
         *
         * @return the page index
         */
        public int getPage() {
            return page;
        }

        /**
         * The size of the page.
         *
         * @return the page size
         */
        public int getPageSize() {
            return pageSize;
        }
    }

    private final ObjectProperty<Callback<LoadRequest, List<T>>> loader = new SimpleObjectProperty<>(this, "loader");

    public final Callback<LoadRequest, List<T>> getLoader() {
        return loader.get();
    }

    public final ObjectProperty<Callback<LoadRequest, List<T>>> loaderProperty() {
        return loader;
    }

    public final void setLoader(Callback<LoadRequest, List<T>> loader) {
        this.loader.set(loader);
    }

    private ObjectProperty<Node> placeholder;

    /**
     * The {@code Node} to show to the user when the {@code PagingListView} has no content to show.
     * This happens when the list model has no data or when a filter has been applied to the list model, resulting in
     * there being nothing to show the user.
     */
    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<>(this, "placeholder");
        }
        return placeholder;
    }

    public final void setPlaceholder(Node value) {
        placeholderProperty().set(value);
    }

    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
    }

    private final ObjectProperty<Consumer<T>> onOpenItem = new SimpleObjectProperty<>(this, "onOpenItem");

    public final Consumer<T> getOnOpenItem() {
        return onOpenItem.get();
    }

    /**
     * A callback for opening an item represented by a row in the table view.
     *
     * @return a callback for opening table items
     */
    public final ObjectProperty<Consumer<T>> onOpenItemProperty() {
        return onOpenItem;
    }

    public final void setOnOpenItem(Consumer<T> onOpenItem) {
        this.onOpenItem.set(onOpenItem);
    }

    private final BooleanProperty usingScrollPane = new SimpleBooleanProperty(this, "usingScrollPane", false);

    public final boolean isUsingScrollPane() {
        return usingScrollPane.get();
    }

    public final BooleanProperty usingScrollPaneProperty() {
        return usingScrollPane;
    }

    public final void setUsingScrollPane(boolean usingScrollPane) {
        this.usingScrollPane.set(usingScrollPane);
    }

    // --- Selection Model
    private final ObjectProperty<MultipleSelectionModel<T>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel");

    /**
     * Sets the {@link MultipleSelectionModel} to be used in the ListView.
     * Despite a ListView requiring a <b>Multiple</b>SelectionModel, it is possible
     * to configure it to only allow single selection (see
     * {@link MultipleSelectionModel#setSelectionMode(SelectionMode)}
     * for more information).
     *
     * @param value the MultipleSelectionModel to be used in this ListView
     */
    public final void setSelectionModel(MultipleSelectionModel<T> value) {
        selectionModelProperty().set(value);
    }

    /**
     * Returns the currently installed selection model.
     *
     * @return the currently installed selection model
     */
    public final MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel == null ? null : selectionModel.get();
    }

    /**
     * The SelectionModel provides the API through which it is possible
     * to select single or multiple items within a ListView, as  well as inspect
     * which items have been selected by the user. Note that it has a generic
     * type that must match the type of the ListView itself.
     *
     * @return the selectionModel property
     */
    public final ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }

    /**
     * Triggers a rebuild of the view without reloading data.
     */
    public final void refresh() {
        gridTableView.refresh();
    }
}
