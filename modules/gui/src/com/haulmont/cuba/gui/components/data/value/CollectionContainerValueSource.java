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
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.CollectionPropertyContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CollectionContainerValueSource<E extends Entity> implements CollectionValueSource<E> {

    protected CollectionContainer<E> container;

    protected EventHub events = new EventHub();

    public CollectionContainerValueSource(CollectionContainer container) {
        this.container = container;

        container.addCollectionChangeListener(event ->
                collectionChanged((CollectionContainer.CollectionChangeEvent) event));
    }

    @Override
    public E getItem(Object entityId) {
        return (E) container.getItem(entityId);
    }

    @Override
    public Collection<E> getItems() {
        return container.getItems();
    }

    @Override
    public Collection<Object> getItemIds() {
        return container.getItems().stream().map(Entity::getId).collect(Collectors.toList());
    }

    @Override
    public void addItem(E entity) {
        container.getMutableItems().add(entity);
    }

    @Override
    public void removeItem(E entity) {
        container.getMutableItems().remove(entity);
    }

    @Override
    public void updateItem(E entity) {
        container.setItem(entity);
    }

    @Override
    public boolean containsItem(E entity) {
        return container.getItems().contains(entity);
    }

    @Override
    public boolean containsItem(Object entityId) {
        return container.getItemOrNull(entityId) != null;
    }

    @Override
    public MetaClass getMetaClass() {
        return container.getEntityMetaClass();
    }

    @Override
    public Collection<E> getValue() {
        return container.getItems();
    }

    @Override
    public void setValue(Collection<E> value) {
        container.getMutableItems().removeAll(Collections.emptyList());
        container.getMutableItems().addAll(value);
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
        return null;
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<Collection<E>>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }

    @Override
    public Subscription addCollectionChangeListener(Consumer<CollectionChangeEvent> listener) {
        return events.subscribe(CollectionChangeEvent.class, listener);
    }

    protected void collectionChanged(CollectionContainer.CollectionChangeEvent event) {
        events.publish(CollectionChangeEvent.class, new CollectionChangeEvent(this, event.getChanges()));
    }

    @Override
    public boolean isNested() {
        return container instanceof CollectionPropertyContainer;
    }

    @Override
    public MetaProperty getProperty() {
        if (!isNested()) {
            return null;
        }
        CollectionPropertyContainer propertyContainer = (CollectionPropertyContainer) this.container;

        return propertyContainer.getEntityMetaClass()
                .getProperty(propertyContainer.getProperty());
    }

    @Override
    public Entity getParentEntity() {
        return isNested()
                ? ((CollectionPropertyContainer) container).getParent().getItem()
                : null;
    }
}
