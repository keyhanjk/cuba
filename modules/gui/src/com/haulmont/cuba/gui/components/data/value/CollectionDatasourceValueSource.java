/*
 * Copyright (c) 2008-2018 Haulmont.
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
 */

package com.haulmont.cuba.gui.components.data.value;

import com.haulmont.bali.events.EventHub;
import com.haulmont.bali.events.Subscription;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.data.BindingState;
import com.haulmont.cuba.gui.components.data.CollectionValueSource;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.NestedDatasource;

import java.util.Collection;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class CollectionDatasourceValueSource<E extends Entity> implements CollectionValueSource<E> {

    protected CollectionDatasource datasource;

    protected EventHub events = new EventHub();

    protected BindingState state = BindingState.INACTIVE;

    public CollectionDatasourceValueSource(CollectionDatasource datasource) {
        this.datasource = datasource;

        this.datasource.addStateChangeListener(this::datasourceStateChanged);
        this.datasource.addItemChangeListener(this::datasourceItemChanged);
        this.datasource.addCollectionChangeListener(this::datasourceCollectionChanged);
    }

    public CollectionDatasource getDatasource() {
        return datasource;
    }

    @Override
    public E getItem(Object entityId) {
        return (E) datasource.getItem(entityId);
    }

    @Override
    public Collection<E> getItems() {
        return datasource.getItems();
    }

    @Override
    public Collection<Object> getItemIds() {
        return datasource.getItemIds();
    }

    @Override
    public void addItem(E entity) {
        datasource.addItem(entity);
    }

    @Override
    public void removeItem(E entity) {
        datasource.removeItem(entity);
    }

    @Override
    public void updateItem(E entity) {
        datasource.updateItem(entity);
    }

    @Override
    public boolean containsItem(E entity) {
        return datasource.containsItem(entity.getId());
    }

    @Override
    public boolean containsItem(Object entityId) {
        return datasource.containsItem(entityId);
    }

    @Override
    public MetaClass getMetaClass() {
        return datasource.getMetaClass();
    }

    @Override
    public Collection<E> getValue() {
        return datasource.getItems();
    }

    @Override
    public void setValue(Collection<E> value) {
        datasource.clear();
        value.forEach(datasource::addItem);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Class<Collection<E>> getType() {
        return null;
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<Collection<E>>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<Collection<E>>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }

    @Override
    public BindingState getState() {
        return datasource.getState() == Datasource.State.VALID
                ? BindingState.ACTIVE
                : BindingState.INACTIVE;
    }

    @Override
    public Subscription addCollectionChangeListener(Consumer<CollectionChangeEvent> listener) {
        return events.subscribe(CollectionChangeEvent.class, listener);
    }

    public void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent<>(this,  state));
        }
    }

    protected void datasourceStateChanged(Datasource.StateChangeEvent e) {
        if (e.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }
    }

    protected void datasourceItemChanged(Datasource.ItemChangeEvent e) {
        if (e.getItem() != null && datasource.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }

        events.publish(ValueChangeEvent.class, new ValueChangeEvent(this, e.getPrevItem(), e.getItem()));
    }

    protected void datasourceCollectionChanged(CollectionDatasource.CollectionChangeEvent event) {
        events.publish(CollectionChangeEvent.class, new CollectionChangeEvent(this, event.getItems()));
    }

    @Override
    public boolean isNested() {
        return datasource instanceof NestedDatasource;
    }

    @Override
    public MetaProperty getProperty() {
        return isNested()
                ? ((NestedDatasource) datasource).getProperty()
                : null;
    }

    @Override
    public Entity getParentEntity() {
        return isNested()
                ? ((NestedDatasource) datasource).getMaster().getItem()
                : null;
    }
}
