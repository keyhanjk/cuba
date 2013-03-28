/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.desktop.gui.components.filter;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.desktop.App;
import com.haulmont.cuba.desktop.gui.components.*;
import com.haulmont.cuba.desktop.sys.vcl.Picker;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.DateField;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author devyatkin
 * @version $Id$
 */
public class ListEditComponent extends Picker {
    protected JButton pickerButton;
    protected JButton clearButton;

    private Class itemClass;
    private MetaClass metaClass;

    private CollectionDatasource collectionDatasource;
    private List<String> runtimeEnum;

    private List listValue;
    private Map<Object, String> values = new LinkedHashMap<>();

    private List<ValueListener> listeners = new ArrayList<>();

    protected UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.class);

    public ListEditComponent(Class itemClass) {
        setOpaque(false);
        contentPanel.setOpaque(false);
        actionsPanel.setOpaque(false);

        pickerButton = new JButton(App.getInstance().getResources().getIcon("pickerfield/img/lookup-btn.png"));
        addButton(pickerButton);
        this.itemClass = itemClass;

        pickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListEditWindow window = new ListEditWindow(values);
                DesktopComponentsHelper.getTopLevelFrame(ListEditComponent.this).deactivate(null);
                window.setVisible(true);
            }
        });
        clearButton = new JButton(App.getInstance().getResources().getIcon("pickerfield/img/clear-btn.png"));
        clearButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setValue(null);
                        values.clear();
                        setText(null);
                    }
                }
        );
        addButton(clearButton);
    }

    public ListEditComponent(CollectionDatasource collectionDatasource) {
        this(collectionDatasource.getMetaClass().getJavaClass());
        this.collectionDatasource = collectionDatasource;
    }

    public ListEditComponent(MetaClass metaClass) {
        this(metaClass.getJavaClass());
        this.metaClass = metaClass;
    }

    public ListEditComponent(List<String> values) {
        this(String.class);
        this.runtimeEnum = values;
    }

    public void addListener(ValueListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void setText(String text){
        super.setValue(text);
    }

    public void setValue(Object newValue) {
        if (!ObjectUtils.equals(listValue, newValue)) {
            listValue = (List) newValue;
            for (ValueListener listener : listeners) {
                listener.valueChanged(this, "", newValue, listValue);
            }
        }
    }

    public void setValues(Map<Object, String> values) {
        this.values = values;
        if (values.isEmpty())
            setValue(null);
        else
            setValue(new ArrayList(values.keySet()));

        String caption = new StrBuilder().appendWithSeparators(values.values(), ",").toString();
        setText(caption);
    }

    private class ListEditWindow extends JDialog {
        protected static final String MESSAGES_PACK = "com.haulmont.cuba.gui.components.filter";

        private static final String COMPONENT_WIDTH = "180";
        private Map<Object, String> values;
        private JPanel mainPanel = new JPanel(new MigLayout("alignx center"));
        private JPanel listPanel;

        private ListEditWindow(Map<Object, String> values) {
            super(App.getInstance().getMainFrame(), MessageProvider.getMessage(MESSAGES_PACK, "ListEditWindow.caption"));
            setLocationRelativeTo(App.getInstance().getMainFrame());
            add(mainPanel);
            this.values = new HashMap<Object, String>(values);
            listPanel = new JPanel(new MigLayout());
            for (Map.Entry<Object, String> entry : values.entrySet()) {
                addItemLayout(entry.getKey(), entry.getValue());
            }
            mainPanel.add(listPanel, "wrap, alignx center");

            final DesktopAbstractComponent field;

            if (collectionDatasource != null) {
                final DesktopLookupField lookup = new DesktopLookupField();
                lookup.setWidth(COMPONENT_WIDTH);
                lookup.setOptionsDatasource(collectionDatasource);

                collectionDatasource.addListener(
                        new CollectionDsListenerAdapter<Entity>() {
                            @Override
                            public void collectionChanged(CollectionDatasource ds, Operation operation, List<Entity> items) {
                                lookup.setValue(null);
                            }
                        }
                );

                lookup.addListener(new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (value != null) {
                            String str = addEntityInstance((Instance) value);
                            addItemLayout(value, str);
                            lookup.setValue(null);
                        }
                    }
                });

                field = lookup;

            } else if (metaClass != null) {
                final DesktopPickerField picker = new DesktopPickerField();
                picker.setWidth(COMPONENT_WIDTH);
                picker.setMetaClass(metaClass);
                PickerField.LookupAction action = (PickerField.LookupAction) picker.getAction(PickerField.LookupAction.NAME);
                action.setLookupScreenOpenType(WindowManager.OpenType.DIALOG);

                picker.addListener(
                        new ValueListener() {
                            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                                if (value != null) {
                                    String str = addEntityInstance((Instance) value);
                                    addItemLayout(value, str);
                                    picker.setValue(null);
                                }
                            }
                        }
                );

                field = picker;

            } else if (runtimeEnum != null) {
                final DesktopLookupField lookup = new DesktopLookupField();
                lookup.setWidth(COMPONENT_WIDTH);
                lookup.setOptionsList(runtimeEnum);

                lookup.addListener(new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (value != null) {
                            String str = addRuntimeEnumValue((String) value);
                            addItemLayout(value, str);
                            lookup.setValue(null);
                        }
                    }
                });

                field = lookup;
            } else if (itemClass.isEnum()) {
                Map<String, Object> options = new HashMap<String, Object>();
                for (Object obj : itemClass.getEnumConstants()) {
                    options.put(MessageProvider.getMessage((Enum) obj), obj);
                }

                final DesktopLookupField lookup = new DesktopLookupField();
                lookup.setWidth(COMPONENT_WIDTH);
                lookup.setOptionsMap(options);

                lookup.addListener(new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (value != null) {
                            String str = addEnumValue((Enum) value);
                            addItemLayout(value, str);
                            lookup.setValue(null);
                        }
                    }
                });

                field = lookup;

            } else if (Date.class.isAssignableFrom(itemClass)) {
                field = new DesktopDateField();

                if (itemClass.equals(java.sql.Date.class))
                    ((DesktopDateField) field).setResolution(DateField.Resolution.DAY);
                else
                    ((DesktopDateField) field).setResolution(DateField.Resolution.MIN);
                ((DesktopDateField) field).addListener(new ValueListener() {
                    @Override
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (value != null) {
                            String str = addDate((Date) value);
                            addItemLayout(value, str);
                            ((DesktopDateField) field).setValue(null);
                        }
                    }
                });

            } else
                throw new UnsupportedOperationException();

            listPanel.add(field.getComposition(), "wrap, dock north, gapy 10px 10px");
            JPanel bottomPanel = new JPanel(new MigLayout());

            JButton okBtn = new JButton(MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok"));
            okBtn.setIcon(App.getInstance().getResources().getIcon("icons/ok.png"));
            DesktopComponentsHelper.adjustSize(okBtn);
            okBtn.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            commitList();
                        }
                    }
            );
            bottomPanel.add(okBtn);

            JButton cancelBtn = new JButton(MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Cancel"));
            cancelBtn.setIcon(App.getInstance().getResources().getIcon("icons/cancel.png"));
            DesktopComponentsHelper.adjustSize(cancelBtn);
            cancelBtn.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose();
                            DesktopComponentsHelper.getTopLevelFrame(ListEditComponent.this).activate();
                        }
                    }
            );
            bottomPanel.add(cancelBtn);

            mainPanel.add(bottomPanel, "alignx center, dock south");
            pack();

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                   DesktopComponentsHelper.getTopLevelFrame(ListEditComponent.this).activate();
                }
            });
        }

        private void addItemLayout(final Object value, String str) {
            final JPanel itemPanel = new JPanel(new MigLayout("insets 0 0 0 0"));
            JLabel itemLab = new JLabel(str);

            itemPanel.add(itemLab);

            JButton delItemBtn = new JButton();
            delItemBtn.setIcon(App.getInstance().getResources().getIcon("icons/tab-remove.png"));
            DesktopComponentsHelper.adjustSize(delItemBtn);
            delItemBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    values.remove(value);
                    listPanel.remove(itemPanel);
                    listPanel.revalidate();
                    listPanel.repaint();
                }
            });
            itemPanel.add(delItemBtn);
            listPanel.add(itemPanel, "wrap");
            listPanel.revalidate();
            listPanel.repaint();
            pack();
        }

        private String addRuntimeEnumValue(String value) {
            values.put(value, value);
            return value;
        }

        private String addEnumValue(Enum en) {
            String str = MessageProvider.getMessage(en);
            values.put(en, str);
            return str;
        }

        private String addEntityInstance(Instance value) {
            String str = value.getInstanceName();
            values.put(value, str);
            return str;
        }

        private String addDate(Date date) {
            //noinspection unchecked
            String str = Datatypes.getNN(itemClass).format(date, userSessionSource.getLocale());

            values.put(date, str);
            return str;
        }

        private void commitList() {
            setValues(values);
            this.dispose();
        }
    }
}