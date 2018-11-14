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
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.data.BindingState;
import com.haulmont.cuba.gui.components.data.ValueSource;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;

import java.util.Collection;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class CollectionDatasourceValueSource<E extends Entity> implements ValueSource<Collection<E>> {

    protected CollectionDatasource datasource;

    protected EventHub events = new EventHub();

    protected BindingState state = BindingState.INACTIVE;

    public CollectionDatasourceValueSource(CollectionDatasource datasource) {
        this.datasource = datasource;

        this.datasource.addStateChangeListener(this::datasourceStateChanged);
        this.datasource.addItemChangeListener(this::datasourceItemChanged);
    }

    public CollectionDatasource getDatasource() {
        return datasource;
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
    public BindingState getState() {
        return datasource.getState() == Datasource.State.VALID
                ? BindingState.ACTIVE
                : BindingState.INACTIVE;
    }

    public void setState(BindingState state) {
        if (this.state != state) {
            this.state = state;

            events.publish(StateChangeEvent.class, new StateChangeEvent<>(this,  state));
        }
    }

    protected void datasourceStateChanged(Datasource.StateChangeEvent<E> e) {
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

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<Collection<E>>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<Collection<E>>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }
}
