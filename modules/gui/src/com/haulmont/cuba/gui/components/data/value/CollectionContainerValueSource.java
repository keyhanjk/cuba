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
import com.haulmont.cuba.gui.components.data.CollectionValueSource;
import com.haulmont.cuba.gui.model.CollectionContainer;

import java.util.Collection;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class CollectionContainerValueSource<E extends Entity> implements CollectionValueSource<E> {

    protected CollectionContainer collectionContainer;

    protected EventHub events = new EventHub();

    public CollectionContainerValueSource(CollectionContainer collectionContainer) {
        this.collectionContainer = collectionContainer;
    }

    @Override
    public Collection<E> getValue() {
        return collectionContainer.getItems();
    }

    @Override
    public void setValue(Collection<E> value) {
        collectionContainer.setItems(value);
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
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<Collection<E>>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }
}
