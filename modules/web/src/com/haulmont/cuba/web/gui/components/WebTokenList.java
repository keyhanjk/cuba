/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.web.gui.components;

import com.haulmont.bali.events.Subscription;
import com.haulmont.chile.core.model.*;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.*;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.*;
import com.haulmont.cuba.gui.components.data.meta.EntityOptions;
import com.haulmont.cuba.gui.components.data.meta.EntityValueSource;
import com.haulmont.cuba.gui.components.data.value.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.model.InstanceContainer;
import com.haulmont.cuba.gui.model.Nested;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.web.widgets.CubaScrollBoxLayout;
import com.haulmont.cuba.web.widgets.CubaTokenListLabel;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.haulmont.cuba.gui.WindowManager.OpenType;

public class WebTokenList<V extends Entity> extends WebV8AbstractField<WebTokenList.CubaTokenList<V>, V, Collection<V>>
        implements TokenList<V>, InitializingBean {

    protected String captionProperty;
    protected CaptionMode captionMode;

    protected ItemChangeHandler itemChangeHandler;
    protected ItemClickListener itemClickListener;

    protected AfterLookupCloseHandler afterLookupCloseHandler;
    protected AfterLookupSelectionHandler afterLookupSelectionHandler;

    protected Button addButton;
    protected Button clearButton;

    protected LookupPickerField<V> lookupPickerField;
    protected PickerField.LookupAction lookupAction;
    protected String lookupScreen;
    protected Map<String, Object> lookupScreenParams;
    protected Screens.LaunchMode lookupOpenMode = OpenMode.THIS_TAB;

    protected Position position = Position.TOP;
    protected boolean inline;
    protected boolean lookup = false;
    protected boolean clearEnabled = true;
    protected boolean simple = false;
    protected boolean multiselect;

    protected UiComponents uiComponents;
    protected Messages messages;
    protected Metadata metadata;
    protected WindowConfig windowConfig;
    protected LookupScreens lookupScreens;

    protected Function<Object, String> tokenStyleGenerator;

    protected final Consumer<ValueChangeEvent<V>> lookupSelectListener = e -> {
        if (isEditable()) {
            addValueFromLookupPickerField();
        }
    };

    public WebTokenList() {
        component = new CubaTokenList<>(this);
    }

    @Inject
    public void setUiComponents(UiComponents uiComponents) {
        this.uiComponents = uiComponents;
    }

    @Inject
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Inject
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Inject
    public void setWindowConfig(WindowConfig windowConfig) {
        this.windowConfig = windowConfig;
    }

    @Inject
    public void setLookupScreens(LookupScreens lookupScreens) {
        this.lookupScreens = lookupScreens;
    }

    @Override
    public void afterPropertiesSet() {
        createComponents();
        initComponentsCaptions();
    }

    protected void createComponents() {
        addButton = uiComponents.create(Button.class);
        clearButton = uiComponents.create(Button.class);

        //noinspection unchecked
        lookupPickerField = uiComponents.create(LookupPickerField.class);
        lookupPickerField.addValueChangeListener(lookupSelectListener);

        setMultiSelect(false);
    }

    protected void initComponentsCaptions() {
        addButton.setCaption(messages.getMessage(TokenList.class, "actions.Add"));
        clearButton.setCaption(messages.getMessage(TokenList.class, "actions.Clear"));
    }

    @Override
    public void setValueSource(ValueSource<Collection<V>> valueSource) {
        super.setValueSource(valueSource);

        if (valueSource != null) {
            valueSource.addValueChangeListener(e ->
                    valueCollectionChanged());
        }
    }

    protected void valueCollectionChanged() {
        if (lookupPickerField != null) {
            if (isLookup()) {
                if (getLookupScreen() != null) {
                    lookupAction.setLookupScreen(getLookupScreen());
                } else {
                    lookupAction.setLookupScreen(null);
                }

                lookupAction.setLookupScreenOpenType(getLookupOpenMode());
                lookupAction.setLookupScreenParams(lookupScreenParams);
            }
        }
        component.refreshComponent();
        component.refreshClickListeners(itemClickListener);
    }

    @Override
    public void setFrame(Frame frame) {
        super.setFrame(frame);
        lookupPickerField.setFrame(frame);
    }

    @Override
    public String getCaptionProperty() {
        return captionProperty;
    }

    @Override
    public void setCaptionProperty(String captionProperty) {
        this.captionProperty = captionProperty;
    }

    @Override
    public OpenType getLookupOpenMode() {
        OpenType openType = null;
        if (lookupOpenMode instanceof OpenMode) {
            openType = OpenType.valueOf(((OpenMode) lookupOpenMode).name());
        }
        return openType;
    }

    @Override
    public void setLookupOpenMode(OpenType lookupOpenMode) {
        this.lookupOpenMode = lookupOpenMode != null
                ? lookupOpenMode.getOpenMode()
                : null;
    }

    @Override
    public Screens.LaunchMode getLookupLaunchMode() {
        return lookupOpenMode;
    }

    @Override
    public void setLookupLaunchMode(Screens.LaunchMode launchMode) {
        this.lookupOpenMode = launchMode;
    }

    @Override
    public CaptionMode getCaptionMode() {
        return captionMode;
    }

    @Override
    public void setCaptionMode(CaptionMode captionMode) {
        this.captionMode = captionMode;
    }

    @Override
    public LookupField.FilterMode getFilterMode() {
        return lookupPickerField.getFilterMode();
    }

    @Override
    public void setFilterMode(LookupField.FilterMode mode) {
        lookupPickerField.setFilterMode(mode);
    }

    @Override
    public String getOptionsCaptionProperty() {
        return lookupPickerField.getCaptionProperty();
    }

    @Override
    public void setOptionsCaptionProperty(String captionProperty) {
        lookupPickerField.setCaptionProperty(captionProperty);
    }

    @Override
    public CaptionMode getOptionsCaptionMode() {
        return lookupPickerField.getCaptionMode();
    }

    @Override
    public void setOptionsCaptionMode(CaptionMode captionMode) {
        lookupPickerField.setCaptionMode(captionMode);
    }

    @Override
    public void setRefreshOptionsOnLookupClose(boolean refresh) {
        lookupPickerField.setRefreshOptionsOnLookupClose(refresh);
    }

    @Override
    public boolean isRefreshOptionsOnLookupClose() {
        return lookupPickerField.isRefreshOptionsOnLookupClose();
    }

    @Override
    public void setOptions(Options<V> options) {
        if (options != null
                && !(options instanceof EntityOptions)) {
            throw new IllegalArgumentException("TokenList supports only EntityOptions");
        }
        lookupPickerField.setOptions(options);
    }

    @Override
    public Options<V> getOptions() {
        return lookupPickerField.getOptions();
    }

    @Override
    public MetaProperty getMetaProperty() {
        return null;
    }

    @Override
    public MetaPropertyPath getMetaPropertyPath() {
        return null;
    }

    @Override
    public boolean isClearEnabled() {
        return clearEnabled;
    }

    @Override
    public void setClearEnabled(boolean clearEnabled) {
        if (this.clearEnabled != clearEnabled) {
            clearButton.setVisible(clearEnabled);
            this.clearEnabled = clearEnabled;
            component.refreshComponent();
        }
    }

    @Override
    public boolean isLookup() {
        return lookup;
    }

    @Override
    public void setLookup(boolean lookup) {
        if (this.lookup != lookup) {
            if (lookup) {
                lookupAction = createLookupAction();
                lookupPickerField.addAction(lookupAction);

                if (getLookupScreen() != null) {
                    lookupAction.setLookupScreen(getLookupScreen());
                }
                lookupAction.setLookupScreenOpenType(getLookupOpenMode());
                lookupAction.setLookupScreenParams(lookupScreenParams);
            } else {
                lookupPickerField.removeAction(lookupAction);
            }

            lookupAction.setAfterLookupCloseHandler((window, actionId) -> {
                if (afterLookupCloseHandler != null) {
                    afterLookupCloseHandler.onClose(window, actionId);
                }
            });

            lookupAction.setAfterLookupSelectionHandler(items -> {
                if (afterLookupSelectionHandler != null) {
                    afterLookupSelectionHandler.onSelect(items);
                }
            });
        }
        this.lookup = lookup;
        component.refreshComponent();
    }

    protected PickerField.LookupAction createLookupAction() {
        return new PickerField.LookupAction(lookupPickerField) {
            @Nonnull
            @Override
            protected Map<String, Object> prepareScreenParams() {
                Map<String, Object> screenParams = super.prepareScreenParams();

                if (isMultiSelect()) {
                    screenParams = new HashMap<>(screenParams);
                    WindowParams.MULTI_SELECT.set(screenParams, true);
                    // for backward compatibility
                    screenParams.put("multiSelect", "true");
                }

                return screenParams;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void handleLookupWindowSelection(Collection items) {
                if (items.isEmpty()) {
                    return;
                }

                @SuppressWarnings("unchecked")
                Collection<Entity> selected = items;

                ValueSource<Collection<V>> valueSource = getValueSourceInternal();
                EntityOptions<V> options = (EntityOptions<V>) lookupPickerField.getOptions();
                if (options != null && lookupPickerField.isRefreshOptionsOnLookupClose()) {
                    options.refresh();
                    if (valueSource != null) {
                        Collection<V> value = valueSource.getValue();
                        valueSource.getValue().forEach(vsv ->
                                options.getOptions()
                                        .filter(e -> Objects.equals(e.getId(), vsv.getId()))
                                        .findFirst()
                                        .ifPresent(v -> {
                                            value.remove(vsv);
                                            value.add(v);

                                            valueSource.setValue(value);
                                        }));
                    }
                }

                // add all selected items to tokens
                if (itemChangeHandler != null) {
                    selected.forEach(itemChangeHandler::addItem);
                } else if (valueSource != null) {
                    // get master entity and inverse attribute in case of nested valueSource
                    Entity masterEntity = getMasterEntity(valueSource);
                    MetaProperty inverseProp = getInverseProperty(valueSource);

                    for (Entity newItem : selected) {
                        Collection<V> value = valueSource.getValue();
                        if (!value.contains((V) newItem)) {
                            // Initialize reference to master entity
                            if (inverseProp != null && isInitializeMasterReference(inverseProp)) {
                                newItem.setValue(inverseProp.getName(), masterEntity);
                            }

                            value.add((V) newItem);
                            valueSource.setValue(value);
                        }
                    }
                }

                afterSelect(items);
                if (afterLookupSelectionHandler != null) {
                    afterLookupSelectionHandler.onSelect(items);
                }
            }
        };
    }

    @Override
    public String getLookupScreen() {
        return lookupScreen;
    }

    @Override
    public void setLookupScreen(String lookupScreen) {
        this.lookupScreen = lookupScreen;
        if (lookupAction != null) {
            lookupAction.setLookupScreen(lookupScreen);
        }
    }

    @Override
    public void setLookupScreenParams(Map<String, Object> params) {
        this.lookupScreenParams = params;
        if (lookupAction != null) {
            lookupAction.setLookupScreenParams(params);
        }
    }

    @Override
    public Map<String, Object> getLookupScreenParams() {
        return lookupScreenParams;
    }

    @Override
    public boolean isMultiSelect() {
        return multiselect;
    }

    @Override
    public void setMultiSelect(boolean multiselect) {
        this.multiselect = multiselect;
        component.refreshComponent();
    }

    @Override
    public String getAddButtonCaption() {
        return addButton.getCaption();
    }

    @Override
    public void setAddButtonCaption(String caption) {
        addButton.setCaption(caption);
    }

    @Override
    public String getAddButtonIcon() {
        return addButton.getIcon();
    }

    @Override
    public void setAddButtonIcon(String icon) {
        addButton.setIcon(icon);
    }

    @Override
    public String getClearButtonCaption() {
        return clearButton.getCaption();
    }

    @Override
    public void setClearButtonCaption(String caption) {
        clearButton.setCaption(caption);
    }

    @Override
    public String getClearButtonIcon() {
        return clearButton.getIcon();
    }

    @Override
    public void setClearButtonIcon(String icon) {
        clearButton.setIcon(icon);
    }

    @Override
    public ItemChangeHandler getItemChangeHandler() {
        return itemChangeHandler;
    }

    @Override
    public void setItemChangeHandler(ItemChangeHandler handler) {
        this.itemChangeHandler = handler;
    }

    @Override
    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    @Override
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        this.component.refreshClickListeners(itemClickListener);
    }

    @Override
    public AfterLookupCloseHandler getAfterLookupCloseHandler() {
        return afterLookupCloseHandler;
    }

    @Override
    public void setAfterLookupCloseHandler(AfterLookupCloseHandler afterLookupCloseHandler) {
        this.afterLookupCloseHandler = afterLookupCloseHandler;
    }

    @Override
    public AfterLookupSelectionHandler getAfterLookupSelectionHandler() {
        return afterLookupSelectionHandler;
    }

    @Override
    public void setAfterLookupSelectionHandler(AfterLookupSelectionHandler afterLookupSelectionHandler) {
        this.afterLookupSelectionHandler = afterLookupSelectionHandler;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = position;

        component.refreshComponent();
    }

    @Override
    public boolean isInline() {
        return inline;
    }

    @Override
    public void setInline(boolean inline) {
        this.inline = inline;

        component.refreshComponent();
    }

    @Override
    protected void setEditableToComponent(boolean editable) {
        component.refreshComponent();
    }

    @Override
    public boolean isSimple() {
        return simple;
    }

    @Override
    public void setSimple(boolean simple) {
        this.simple = simple;
        this.addButton.setVisible(simple);
        this.component.refreshComponent();
    }

    @Override
    public void setTokenStyleGenerator(Function<Object, String> tokenStyleGenerator) {
        this.tokenStyleGenerator = tokenStyleGenerator;
    }

    @Override
    public Function<Object, String> getTokenStyleGenerator() {
        return tokenStyleGenerator;
    }

    @Override
    public String getLookupInputPrompt() {
        return lookupPickerField.getInputPrompt();
    }

    @Override
    public void setLookupInputPrompt(String inputPrompt) {
        this.lookupPickerField.setInputPrompt(inputPrompt);
    }

    protected String instanceCaption(Instance instance) {
        if (instance == null) {
            return "";
        }
        if (captionProperty != null) {
            if (instance.getMetaClass().getPropertyPath(captionProperty) != null) {
                Object o = instance.getValueEx(captionProperty);
                return o != null ? o.toString() : " ";
            }

            throw new IllegalArgumentException(String.format("Couldn't find property with name '%s'", captionProperty));
        } else {
            return metadata.getTools().getInstanceName(instance);
        }
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @SuppressWarnings("unchecked")
    protected void addValueFromLookupPickerField() {
        final Entity newItem = lookupPickerField.getValue();
        if (newItem == null) {
            return;
        }

        ValueSource<Collection<V>> valueSource = valueBinding.getSource();

        if (itemChangeHandler != null) {
            itemChangeHandler.addItem(newItem);
        } else {
            if (valueSource != null) {
                // get master entity and inverse attribute in case of nested valueSource
                Entity masterEntity = getMasterEntity(valueSource);
                MetaProperty inverseProp = getInverseProperty(valueSource);

                if (!valueSource.getValue().contains((V) newItem)) {
                    // Initialize reference to master entity
                    if (inverseProp != null && isInitializeMasterReference(inverseProp)) {
                        newItem.setValue(inverseProp.getName(), masterEntity);
                    }

                    List<V> newValue = new ArrayList<>(valueSource.getValue());
                    newValue.add((V) newItem);
                    valueSource.setValue(newValue);
                }
            }
        }
        lookupPickerField.setValue(null);
        lookupPickerField.focus();

        if (lookupPickerField.isRefreshOptionsOnLookupClose()) {
            if (valueSource != null && getOptions() != null) {
                List<V> newValue = new ArrayList<>(valueSource.getValue());

                for (V obj : valueSource.getValue()) {
                    lookupPickerField.getOptions()
                            .getOptions()
                            .filter(e -> Objects.equals(e.getId(), obj.getId()))
                            .findFirst()
                            .ifPresent(item -> {
                                newValue.remove(obj);
                                newValue.add(item);
                            });
                }

                valueSource.setValue(newValue);
            }
        }
    }

    @Nullable
    protected Entity getMasterEntity(ValueSource<Collection<V>> valueSource) {
        Entity masterEntity = null;
        if (valueSource instanceof DatasourceValueSource) {
            Datasource ds = ((DatasourceValueSource) valueSource).getDatasource();
            if (ds instanceof NestedDatasource) {
                masterEntity = ((NestedDatasource) ds).getMaster().getItem();
            }
        } else if (valueSource instanceof ContainerValueSource) {
            InstanceContainer container = ((ContainerValueSource) valueSource).getContainer();
            if (container instanceof Nested) {
                masterEntity = ((Nested) container).getParent().getItem();
            }
        }
        return masterEntity;
    }

    @Nullable
    protected MetaProperty getInverseProperty(ValueSource<Collection<V>> valueSource) {
        MetaProperty inverseProperty = null;

        if (valueSource instanceof DatasourceValueSource) {
            Datasource ds = ((DatasourceValueSource) valueSource).getDatasource();
            if (ds instanceof NestedDatasource) {
                inverseProperty = ((NestedDatasource) ds).getProperty().getInverse();
            }
        } else if (valueSource instanceof ContainerValueSource) {
            InstanceContainer container = ((ContainerValueSource) valueSource).getContainer();
            if (container instanceof Nested) {
                String property = ((Nested) container).getProperty();
                inverseProperty = ((ContainerValueSource) valueSource).getEntityMetaClass()
                        .getPropertyNN(property)
                        .getInverse();
            }
        }

        return inverseProperty;
    }

    protected boolean isInitializeMasterReference(MetaProperty inverseProp) {
        return !inverseProp.getRange().getCardinality().isMany()
                && isInversePropertyAssignableFromDsClass(inverseProp);

    }

    protected boolean isInversePropertyAssignableFromDsClass(MetaProperty inverseProp) {
        ExtendedEntities extendedEntities = metadata.getExtendedEntities();

        Class entityClass = extendedEntities.getEffectiveClass(getMetaClass());
        Class inversePropClass = extendedEntities.getEffectiveClass(inverseProp.getDomain());

        //noinspection unchecked
        return inversePropClass.isAssignableFrom(entityClass);
    }

    @Nonnull
    protected MetaClass getMetaClass() {
        MetaClass metaClass = null;

        if (getValueSourceInternal() != null) {
            ValueSource<Collection<V>> valueSource = getValueSourceInternal();

            if (valueSource instanceof DatasourceValueSource) {
                metaClass = ((DatasourceValueSource) valueSource).getEntityMetaClass();
            } else if (valueSource instanceof ContainerValueSource) {
                metaClass = ((ContainerValueSource) valueSource).getEntityMetaClass();
            }
        } else if (getOptions() != null) {
            metaClass = ((EntityOptions<V>) getOptions()).getEntityMetaClass();
        }

        if (metaClass == null) {
            throw new IllegalStateException("Either ValueSource or Options should be specified");
        }

        return metaClass;
    }

    @Override
    protected boolean isEmpty(Object value) {
        return super.isEmpty(value) || (value instanceof Collection && ((Collection) value).isEmpty());
    }

    @Override
    public void focus() {
        if (simple) {
            addButton.focus();
        } else {
            lookupPickerField.focus();
        }
    }

    protected ValueSource<Collection<V>> getValueSourceInternal() {
        return valueBinding != null
                ? valueBinding.getSource()
                : null;
    }

    public static class CubaTokenList<T> extends CustomField<T> {

        protected static final String TOKENLIST_STYLENAME = "c-tokenlist";
        protected static final String TOKENLIST_SCROLLBOX_STYLENAME = "c-tokenlist-scrollbox";

        protected static final String ADD_BTN_STYLENAME = "add-btn";
        protected static final String CLEAR_BTN_STYLENAME = "clear-btn";
        protected static final String INLINE_STYLENAME = "inline";
        protected static final String READONLY_STYLENAME = "readonly";

        protected WebTokenList owner;

        protected VerticalLayout composition;
        protected CubaScrollBoxLayout tokenContainer;
        protected HorizontalLayout editor;

        protected Map<Instance, CubaTokenListLabel> itemComponents = new HashMap<>();
        protected Map<CubaTokenListLabel, Instance> componentItems = new HashMap<>();

        protected Subscription addButtonSub;

        public CubaTokenList(WebTokenList owner) {
            this.owner = owner;

            setWidthUndefined();

            composition = new VerticalLayout();
            composition.setWidthUndefined();

            tokenContainer = new CubaScrollBoxLayout();
            tokenContainer.setStyleName(TOKENLIST_SCROLLBOX_STYLENAME);
            tokenContainer.setWidthUndefined();
            tokenContainer.setMargin(new MarginInfo(true, false, false, false));

            composition.addComponent(tokenContainer);
            setPrimaryStyleName(TOKENLIST_STYLENAME);
        }

        @Override
        public T getValue() {
            // do nothing
            return null;
        }

        @Override
        protected void doSetValue(T value) {
            // do nothing
        }

        @Override
        public boolean isEmpty() {
            //noinspection unchecked
            ValueSource<Collection<Entity>> valueSource = owner.getValueSourceInternal();

            if (valueSource != null) {
                return valueSource.getValue().isEmpty();
            }
            return super.isEmpty();
        }

        @Override
        public void setHeight(String height) {
            super.setHeight(height);

            if (getHeight() > 0) {
                composition.setHeight("100%");
                composition.setExpandRatio(tokenContainer, 1);
                tokenContainer.setHeight("100%");
            } else {
                composition.setHeightUndefined();
                composition.setExpandRatio(tokenContainer, 0);
                tokenContainer.setHeightUndefined();
            }
        }

        @Override
        public void setWidth(float width, Unit unit) {
            super.setWidth(width, unit);

            if (composition != null && tokenContainer != null) {
                if (getWidth() > 0) {
                    composition.setWidth("100%");
                    editor.setWidth("100%");

                    if (!owner.isSimple()) {
                        owner.lookupPickerField.setWidthFull();
                        editor.setExpandRatio(WebComponentsHelper.getComposition(owner.lookupPickerField), 1);
                    }
                } else {
                    composition.setWidthUndefined();
                    editor.setWidthUndefined();

                    if (!owner.isSimple()) {
                        owner.lookupPickerField.setWidthAuto();
                        editor.setExpandRatio(WebComponentsHelper.getComposition(owner.lookupPickerField), 0);
                    }
                }
            }
        }

        @Override
        protected Component initContent() {
            return composition;
        }

        protected void initField() {
            if (editor == null) {
                editor = new HorizontalLayout();
                editor.setSpacing(true);
                editor.setWidthUndefined();
            }
            editor.removeAllComponents();

            if (!owner.isSimple()) {
                owner.lookupPickerField.setWidthAuto();
                editor.addComponent(WebComponentsHelper.getComposition(owner.lookupPickerField));
            }
            owner.lookupPickerField.setVisible(!owner.isSimple());

            owner.addButton.setVisible(owner.isSimple());
            owner.addButton.setStyleName(ADD_BTN_STYLENAME);

            if (addButtonSub != null) {
                addButtonSub.remove();
            }

            if (!owner.isSimple()) {
                addButtonSub = owner.addButton.addClickListener(e -> {
                    if (owner.isEditable()) {
                        owner.addValueFromLookupPickerField();
                    }
                    owner.addButton.focus();
                });
            } else {
                addButtonSub = owner.addButton.addClickListener(e -> {
                    String windowAlias = null;
                    if (owner.getLookupScreen() != null) {
                        windowAlias = owner.getLookupScreen();
                    } else if (owner.getOptions() != null) {
                        windowAlias = owner.windowConfig.getBrowseScreenId(((EntityOptions) owner.getOptions()).getEntityMetaClass());
                    } else if (owner.getValueSourceInternal() != null){
                        MetaClass entityMetaClass = ((EntityValueSource) owner.getValueSourceInternal()).getEntityMetaClass();
                        windowAlias = owner.windowConfig.getBrowseScreenId(entityMetaClass);
                    }

                    Class<? extends Entity> entityClass = owner.getMetaClass().getJavaClass();

                    Map<String, Object> params = new HashMap<>();
                    params.put("windowOpener", owner.getFrame().getId());
                    if (owner.isMultiSelect()) {
                        WindowParams.MULTI_SELECT.set(params, true);
                        // for backward compatibility
                        params.put("multiSelect", "true");
                    }
                    if (owner.lookupScreenParams != null) {
                        //noinspection unchecked
                        params.putAll(owner.lookupScreenParams);
                    }

                    Screen lookupScreen = owner.lookupScreens.builder(entityClass, owner.getFrame().getFrameOwner())
                            .withScreen(windowAlias)
                            .withLaunchMode(owner.getLookupLaunchMode())
                            .withOptions(new MapScreenOptions(params))
                            .withSelectHandler(selectedItems -> {
                                if (owner.lookupPickerField.isRefreshOptionsOnLookupClose()) {
                                    //noinspection unchecked
                                    ((EntityOptions<Entity>) owner.getOptions()).refresh();
                                }

                                if (owner.isEditable()) {
                                    if (selectedItems == null || selectedItems.isEmpty()) {
                                        return;
                                    }

                                    handleLookupInternal(selectedItems);

                                    if (owner.afterLookupSelectionHandler != null) {
                                        owner.afterLookupSelectionHandler.onSelect(selectedItems);
                                    }
                                }
                                owner.addButton.focus();
                            })
                            .build();

                    if (owner.afterLookupCloseHandler != null) {
                        lookupScreen.addAfterCloseListener(event -> {
                            String actionId = ((StandardCloseAction) event.getCloseAction()).getActionId();
                            owner.afterLookupCloseHandler.onClose(lookupScreen.getWindow(), actionId);
                        });
                    }
                });
            }
            editor.addComponent(owner.addButton.unwrap(com.vaadin.ui.Button.class));

            owner.clearButton.setVisible(owner.clearEnabled);
            owner.clearButton.setStyleName(CLEAR_BTN_STYLENAME);
            owner.clearButton.addClickListener(e -> {
                for (CubaTokenListLabel item : new ArrayList<>(itemComponents.values())) {
                    doRemove(item);
                }
                owner.clearButton.focus();
            });

            com.vaadin.ui.Button vClearButton = owner.clearButton.unwrap(com.vaadin.ui.Button.class);
            if (owner.isSimple()) {
                final HorizontalLayout clearLayout = new HorizontalLayout();
                clearLayout.addComponent(vClearButton);
                editor.addComponent(clearLayout);
                editor.setExpandRatio(clearLayout, 1);
            } else {
                editor.addComponent(vClearButton);
            }
        }

        @SuppressWarnings("unchecked")
        protected void handleLookupInternal(Collection<? extends Entity> entities) {
            // get master entity and inverse attribute in case of nested valueSource
            ValueSource<Collection<Entity>> valueSource = owner.getValueSourceInternal();

            Entity masterEntity = owner.getMasterEntity(valueSource);
            MetaProperty inverseProp = owner.getInverseProperty(valueSource);
            boolean initializeMasterReference = inverseProp != null && owner.isInitializeMasterReference(inverseProp);

            List<Entity> newValue = null;
            if (valueSource != null) {
                newValue = new ArrayList<>(valueSource.getValue());
            }

            for (Entity entity : entities) {
                if (owner.itemChangeHandler != null) {
                    owner.itemChangeHandler.addItem(entity);
                } else {
                    if (valueSource != null && !valueSource.getValue().contains(entity)) {
                        // Initialize reference to master entity
                        if (initializeMasterReference) {
                            entity.setValue(inverseProp.getName(), masterEntity);
                        }
                        newValue.add(entity);
                    }
                }
            }

            if (newValue != null) {
                valueSource.setValue(newValue);
            }
        }

        public void refreshComponent() {
            if (owner.inline) {
                addStyleName(INLINE_STYLENAME);
            } else {
                removeStyleName(INLINE_STYLENAME);
            }

            if (owner.editable) {
                removeStyleName(READONLY_STYLENAME);
            } else {
                addStyleName(READONLY_STYLENAME);
            }

            if (editor != null) {
                composition.removeComponent(editor);
            }

            initField();

            if (owner.isEditable()) {
                if (owner.position == Position.TOP) {
                    composition.addComponentAsFirst(editor);
                } else {
                    composition.addComponent(editor);
                }
            }

            tokenContainer.removeAllComponents();

            //noinspection unchecked
            ValueSource<Collection<Entity>> valueSource = owner.getValueSourceInternal();

            if (valueSource != null) {
                List<Instance> usedItems = new ArrayList<>();

                // New tokens
                for (final Entity entity : valueSource.getValue()) {
                    //noinspection unchecked
                    CubaTokenListLabel f = itemComponents.get(entity);
                    if (f == null) {
                        f = createToken();
                        itemComponents.put(entity, f);
                        componentItems.put(f, entity);
                    }
                    f.setEditable(owner.isEditable());
                    f.setText(owner.instanceCaption(entity));
                    f.setWidthUndefined();

                    setTokenStyle(f, entity.getId());
                    tokenContainer.addComponent(f);
                    usedItems.add(entity);
                }

                // Remove obsolete items
                for (Instance componentItem : new ArrayList<>(itemComponents.keySet())) {
                    if (!usedItems.contains(componentItem)) {
                        componentItems.remove(itemComponents.get(componentItem));
                        itemComponents.remove(componentItem);
                    }
                }
            }

            if (getHeight() < 0) {
                tokenContainer.setVisible(!isEmpty());
            } else {
                tokenContainer.setVisible(true);
            }

            updateEditorMargins();

            updateSizes();
        }

        protected void updateEditorMargins() {
            if (tokenContainer.isVisible()) {
                if (owner.position == Position.TOP) {
                    editor.setMargin(new MarginInfo(false, false, true, false));
                } else {
                    editor.setMargin(new MarginInfo(true, false, false, false));
                }
            } else {
                editor.setMargin(false);
            }
        }

        protected void updateSizes() {
            if (getHeight() > 0) {
                composition.setHeight("100%");
                composition.setExpandRatio(tokenContainer, 1);
                tokenContainer.setHeight("100%");
            } else {
                composition.setHeightUndefined();
                composition.setExpandRatio(tokenContainer, 0);
                tokenContainer.setHeightUndefined();
            }

            if (getWidth() > 0) {
                composition.setWidth("100%");
                editor.setWidth("100%");

                if (!owner.isSimple()) {
                    owner.lookupPickerField.setWidthFull();
                    editor.setExpandRatio(WebComponentsHelper.getComposition(owner.lookupPickerField), 1);
                }
            } else {
                composition.setWidthUndefined();
                editor.setWidthUndefined();

                if (!owner.isSimple()) {
                    owner.lookupPickerField.setWidthAuto();
                    editor.setExpandRatio(WebComponentsHelper.getComposition(owner.lookupPickerField), 0);
                }
            }
        }

        public void refreshClickListeners(ItemClickListener listener) {
            //noinspection unchecked
            ValueSource<Collection<Entity>> valueSource = owner.getValueSourceInternal();
            if (valueSource != null && BindingState.ACTIVE.equals(valueSource.getState())) {
                for (Entity entity : valueSource.getValue()) {
                    //noinspection unchecked
                    final CubaTokenListLabel label = itemComponents.get(entity);
                    if (label != null) {
                        if (listener != null) {
                            label.setClickListener(source ->
                                    doClick(label));
                        } else {
                            label.setClickListener(null);
                        }
                    }
                }
            }
        }

        protected CubaTokenListLabel createToken() {
            final CubaTokenListLabel label = new CubaTokenListLabel();
            label.setWidth("100%");
            label.addListener((CubaTokenListLabel.RemoveTokenListener) source -> {
                if (owner.isEditable()) {
                    doRemove(source);
                }
            });
            return label;
        }

        @SuppressWarnings("unchecked")
        protected void doRemove(CubaTokenListLabel source) {
            ValueSource<Collection<? extends Entity>> valueSource = owner.getValueSourceInternal();

            Instance item = componentItems.get(source);
            if (item != null) {
                itemComponents.remove(item);
                componentItems.remove(source);

                List<? extends Entity> newValue = null;
                if (valueSource != null) {
                    newValue = new ArrayList<>(valueSource.getValue());
                }

                if (owner.itemChangeHandler != null) {
                    owner.itemChangeHandler.removeItem(item);
                } else {
                    if (valueSource != null) {
                        // get inverse attribute in case of nested valueSource
                        MetaProperty inverseProp = owner.getInverseProperty(valueSource);
                        boolean initializeMasterReference = inverseProp != null
                                && owner.isInitializeMasterReference(inverseProp);

                        if (initializeMasterReference) {
                            item.setValue(inverseProp.getName(), null);
                        }
                        newValue.remove((Entity) item);
                    }
                }

                if (newValue != null) {
                    valueSource.setValue(newValue);
                }
            }
        }

        protected void doClick(CubaTokenListLabel source) {
            if (owner.itemClickListener != null) {
                Instance item = componentItems.get(source);
                if (item != null) {
                    owner.itemClickListener.onClick(item);
                }
            }
        }

        protected void setTokenStyle(CubaTokenListLabel label, Object itemId) {
            if (owner.tokenStyleGenerator != null) {
                //noinspection unchecked
                String styleName = ((Function<Object, String>) owner.getTokenStyleGenerator()).apply(itemId);
                if (styleName != null && !styleName.equals("")) {
                    label.setStyleName(styleName);
                }
            }
        }
    }
}