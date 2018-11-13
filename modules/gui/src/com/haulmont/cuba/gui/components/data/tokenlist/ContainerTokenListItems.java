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
import com.haulmont.cuba.gui.components.data.meta.ContainerDataUnit;
import com.haulmont.cuba.gui.model.CollectionContainer;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ContainerTokenListItems<E extends Entity> implements TokenListItems<E>, ContainerDataUnit<E> {

    protected CollectionContainer<E> container;

    protected EventHub events = new EventHub();

    public ContainerTokenListItems(CollectionContainer<E> container) {
        this.container = container;

        this.container.addCollectionChangeListener(this::containerCollectionChanged);
    }

    @Override
    public Collection<?> getItemIds() {
        return container.getItems().stream().map(Entity::getId).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public E getItem(Object itemId) {
        return container.getItemOrNull(itemId);
    }

    @Override
    public Object getItemValue(Object itemId, Object propertyId) {
        MetaPropertyPath propertyPath = (MetaPropertyPath) propertyId;
        return container.getItem(itemId).getValueEx(propertyPath.toPathString());
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Override
    public boolean containsId(Object itemId) {
        return container.getItems().stream().anyMatch(e -> e.getId().equals(itemId));
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

    @Override
    public Subscription addItemSetChangeListener(Consumer<TokenListItems.ItemSetChangeEvent<E>> listener) {
        return events.subscribe(TokenListItems.ItemSetChangeEvent.class, (Consumer) listener);
    }


    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Subscription addStateChangeListener(Consumer<StateChangeEvent<E>> listener) {
        return events.subscribe(StateChangeEvent.class, (Consumer) listener);
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }

    protected void containerCollectionChanged(CollectionContainer.CollectionChangeEvent<E> e) {
        events.publish(TokenListItems.ItemSetChangeEvent.class, new TokenListItems.ItemSetChangeEvent<>(this));
    }
}
