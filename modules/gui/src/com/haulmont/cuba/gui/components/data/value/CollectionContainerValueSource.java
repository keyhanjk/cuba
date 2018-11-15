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
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionContainer.CollectionChangeEvent;

import java.util.Collection;
import java.util.function.Consumer;

public class CollectionContainerValueSource<E extends Entity> implements ValueSource<Collection<E>> {

    protected CollectionContainer<E> container;

    protected EventHub events = new EventHub();

    public CollectionContainerValueSource(CollectionContainer<E> container) {
        this.container = container;

        container.addCollectionChangeListener(this::collectionChanged);
    }

    @SuppressWarnings("unchecked")
    protected void collectionChanged(CollectionChangeEvent<E> event) {
        events.publish(ValueChangeEvent.class,
                new ValueChangeEvent<>(this, null, (Collection<E>) event.getChanges()));
    }

    @Override
    public Collection<E> getValue() {
        return container.getItems();
    }

    @Override
    public void setValue(Collection<E> value) {
        container.setItems(value);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Class<Collection<E>> getType() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<Collection<E>>> listener) {
        return events.subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<Collection<E>>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }
}
