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

package com.haulmont.cuba.gui.components.data.tokenlist;

import com.haulmont.bali.events.EventHub;
import com.haulmont.bali.events.Subscription;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.data.BindingState;
import com.haulmont.cuba.gui.components.data.TokenListItems;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class DatasourceTokenListItems<E extends Entity<K>, K> implements TokenListItems<E> {

    protected CollectionDatasource datasource;

    protected EventHub events = new EventHub();

    protected BindingState state = BindingState.INACTIVE;

    public DatasourceTokenListItems(CollectionDatasource datasource) {
        this.datasource = datasource;

        this.datasource.addStateChangeListener(this::datasourceStateChanged);
        this.datasource.addCollectionChangeListener(this::datasourceCollectionChanged);
    }

    protected void datasourceCollectionChanged(@SuppressWarnings("unused") CollectionDatasource.CollectionChangeEvent<E, K> e) {
        events.publish(TokenListItems.ItemSetChangeEvent.class, new TokenListItems.ItemSetChangeEvent<>(this));
    }

    protected void datasourceStateChanged(Datasource.StateChangeEvent<E> e) {
        if (e.getState() == Datasource.State.VALID) {
            setState(BindingState.ACTIVE);
        } else {
            setState(BindingState.INACTIVE);
        }
    }

    public void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent<>(this, state));
        }
    }

    public CollectionDatasource getDatasource() {
        return datasource;
    }

    @Override
    public Collection<?> getItemIds() {
        return datasource.getItemIds();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public E getItem(Object itemId) {
        return (E) datasource.getItem(itemId);
    }

    @Override
    public Object getItemValue(Object itemId, Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return datasource.getItemNN(itemId).getValueEx(propertyPath.toPathString());
    }

    @Override
    public int size() {
        return datasource.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsId(Object itemId) {
        return datasource.containsItem(itemId);
    }

    @Override
    public Class<?> getType(Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return propertyPath.getRangeJavaClass();
    }

    @Override
    public boolean supportsProperty(Object propertyId) {
        return propertyId instanceof MetaPropertyPath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addItemSetChangeListener(Consumer listener) {
        return events.subscribe(ItemSetChangeEvent.class, listener);
    }


    @Override
    public BindingState getState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addStateChangeListener(Consumer listener) {
        return events.subscribe(StateChangeEvent.class, listener);
    }
}
