/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.desktop.gui.components;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.CaptionMode;
import com.haulmont.cuba.gui.components.OptionsField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author krivopustov
 */
public abstract class DesktopAbstractOptionsField<C extends JComponent>
        extends DesktopAbstractField<C>
        implements OptionsField
{

    protected CaptionMode captionMode = CaptionMode.ITEM;
    protected String captionProperty;
    protected String descProperty;
    protected CollectionDatasource<Entity<Object>, Object> optionsDatasource;
    protected List<Object> optionsList;
    protected Map<String, Object> optionsMap;
    protected Datasource datasource;
    protected MetaProperty metaProperty;
    protected MetaPropertyPath metaPropertyPath;
    protected boolean updatingInstance;

    @Override
    public CollectionDatasource getOptionsDatasource() {
        return optionsDatasource;
    }

    @Override
    public void setOptionsDatasource(CollectionDatasource datasource) {
        this.optionsDatasource = datasource;
    }

    @Override
    public List getOptionsList() {
        return optionsList;
    }

    @Override
    public void setOptionsList(List optionsList) {
        this.optionsList = optionsList;
    }

    @Override
    public Map<String, Object> getOptionsMap() {
        return optionsMap;
    }

    @Override
    public void setOptionsMap(Map<String, Object> map) {
        optionsMap = map;
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
    public String getCaptionProperty() {
        return captionProperty;
    }

    @Override
    public void setCaptionProperty(String captionProperty) {
        this.captionProperty = captionProperty;
    }

    @Override
    public String getDescriptionProperty() {
        return descProperty;
    }

    @Override
    public void setDescriptionProperty(String descProperty) {
        this.descProperty = descProperty;
    }

    @Override
    public void setDatasource(Datasource datasource, String property) {
        this.datasource = datasource;

        if (datasource == null) {
            setValue(null);
            return;
        }

        final MetaClass metaClass = datasource.getMetaClass();
        metaPropertyPath = metaClass.getPropertyPath(property);
        try {
            metaProperty = metaPropertyPath.getMetaProperty();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Metaproperty name is possibly wrong: " + property, e);
        }

        datasource.addListener(
                new DsListenerAdapter() {
                    @Override
                    public void itemChanged(Datasource ds, Entity prevItem, Entity item) {
                        if (updatingInstance)
                            return;
                        Object value = InstanceUtils.getValueEx(item, metaPropertyPath.getPath());
                        setValue(value);

//                        if (value == null) {
//                            setSelectedItem(null);
//                            return;
//                        }
//
//                        Object selectedItem;
//                        if (optionsMap != null) {
//                            for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
//                                if (value.equals(entry.getValue())) {
//                                    setSelectedItem(new MapKeyWrapper(entry.getKey()));
//                                    return;
//                                }
//                            }
//                            setSelectedItem(null);
//                            return;
//                        }
//
//                        if (value instanceof Entity) {
//                            selectedItem = new EntityWrapper((Entity) value);
//                        } else
//                            selectedItem = new ObjectWrapper(value);
//                        setSelectedItem(selectedItem);
                    }

                    @Override
                    public void valueChanged(Entity source, String property, Object prevValue, Object value) {
                        if (updatingInstance)
                            return;
                        if (property.equals(metaPropertyPath.toString())) {
                            setValue(value);
//                            updatingInstance = true;
//                            try {
//                                Object selectedItem;
//                                if (value instanceof Entity) {
//                                    selectedItem = new EntityWrapper((Entity) value);
//                                } else
//                                    throw new UnsupportedOperationException(); // TODO
//                                setSelectedItem(selectedItem);
//                            } finally {
//                                updatingInstance = false;
//                            }
                        }
                    }
                }
        );

        if ((datasource.getState() == Datasource.State.VALID) && (datasource.getItem() != null)) {
            Object newValue = InstanceUtils.getValueEx(datasource.getItem(), metaPropertyPath.getPath());
            setValue(newValue);
        }
    }

    @Override
    public Datasource getDatasource() {
        return datasource;
    }

    @Override
    public MetaProperty getMetaProperty() {
        return metaProperty;
    }

    @Override
    public <T> T getValue() {
        ValueWrapper selectedItem = (ValueWrapper) getSelectedItem();
        return selectedItem != null ? (T) selectedItem.getValue() : null;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            setSelectedItem(null);
            return;
        }

        Object selectedItem;
        if (optionsMap != null) {
            for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                if (value.equals(entry.getValue())) {
                    setSelectedItem(new MapKeyWrapper(entry.getKey()));
                    return;
                }
            }
            setSelectedItem(null);
            return;
        }

        if (value instanceof Entity) {
            selectedItem = new EntityWrapper((Entity) value);
        } else
            selectedItem = new ObjectWrapper(value);
        setSelectedItem(selectedItem);
    }

    protected abstract Object getSelectedItem();
    protected abstract void setSelectedItem(Object item);

    public interface ValueWrapper<T> {
        public T getValue();
    }

    public class EntityWrapper implements ValueWrapper<Entity> {

        private Entity entity;

        public EntityWrapper(Entity entity) {
            this.entity = entity;
        }

        @Override
        public Entity getValue() {
            return entity;
        }

        @Override
        public String toString() {
            if (entity == null)
                return "";
            if (captionMode.equals(CaptionMode.PROPERTY) && !StringUtils.isBlank(captionProperty))
                return entity.getValueEx(captionProperty);
            else
                return entity.getInstanceName();
        }
    }

    public class MapKeyWrapper implements ValueWrapper<Object> {

        private String key;

        public MapKeyWrapper(String key) {
            this.key = key;
        }

        @Override
        public Object getValue() {
            return optionsMap.get(key);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public class ObjectWrapper implements ValueWrapper<Object> {

        private Object obj;

        public ObjectWrapper(Object obj) {
            this.obj = obj;
        }

        @Override
        public Object getValue() {
            return obj;
        }

        @Override
        public String toString() {
            if (obj == null)
                return "";
            return obj.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DesktopAbstractOptionsField.ObjectWrapper) {
                ObjectWrapper anotherWrapper = (ObjectWrapper) obj;
                return ObjectUtils.equals(this.obj, anotherWrapper.obj);
            }
            return false;
        }
    }
}
