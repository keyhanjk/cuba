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

package com.haulmont.cuba.web.gui.components.tokenlist;

import com.haulmont.cuba.gui.components.data.TokenListItems;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;

import java.util.Collection;

public class TokenListDataContainer<I> implements Container, Container.ItemSetChangeNotifier {

    protected TokenListItems tokenListItems;

    public TokenListItems getTokenListItems() {
        return tokenListItems;
    }

    public void unbind() {

    }

    @Override
    public Item getItem(Object itemId) {
        return null;
    }

    @Override
    public Collection<?> getContainerPropertyIds() {
        return null;
    }

    @Override
    public Collection<?> getItemIds() {
        return null;
    }

    @Override
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return null;
    }

    @Override
    public Class<?> getType(Object propertyId) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsId(Object itemId) {
        return false;
    }

    @Override
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public Object addItem() throws UnsupportedOperationException {
        return null;
    }

    @Override
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return false;
    }

    @Override
    public boolean removeAllItems() throws UnsupportedOperationException {
        return false;
    }

    @Override
    public void addItemSetChangeListener(ItemSetChangeListener listener) {

    }

    @Override
    public void addListener(ItemSetChangeListener listener) {

    }

    @Override
    public void removeItemSetChangeListener(ItemSetChangeListener listener) {

    }

    @Override
    public void removeListener(ItemSetChangeListener listener) {

    }
}
