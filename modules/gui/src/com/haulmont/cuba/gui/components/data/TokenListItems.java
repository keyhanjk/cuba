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

package com.haulmont.cuba.gui.components.data;

import com.haulmont.bali.events.Subscription;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EventObject;
import java.util.function.Consumer;

public interface TokenListItems<I> extends DataUnit<I> {

    Collection<?> getItemIds();

    @Nullable
    I getItem(Object itemId);

    default I getItemNN(Object itemId) {
        I item = getItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Unable to find item with id " + itemId);
        }
        return item;
    }

    Object getItemValue(Object itemId, Object propertyId);

    int size();

    boolean containsId(Object itemId);

    Class<?> getType(Object propertyId);

    boolean supportsProperty(Object propertyId);

    Subscription addItemSetChangeListener(Consumer<TokenListItems.ItemSetChangeEvent<I>> listener);

    class ItemSetChangeEvent<T> extends EventObject {

        public ItemSetChangeEvent(TokenListItems<T> source) {
            super(source);
        }

        @SuppressWarnings("unchecked")
        @Override
        public TokenListItems<T> getSource() {
            return (TokenListItems<T>) super.getSource();
        }
    }
}
