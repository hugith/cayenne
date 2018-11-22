/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.apache.cayenne.modeler.editor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.cayenne.configuration.xml.DataChannelMetaData;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.modeler.Application;
import org.apache.cayenne.modeler.util.ModelerUtil;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * @since 4.1
 */
public class GeneratorsPanel extends JPanel {

    private JCheckBox checkConfig;
    private JLabel dataMapLabel;
    private JButton toConfigButton;
    private JButton deleteButton;
    private DataMap dataMap;
    private Class type;
    private String icon;

    public GeneratorsPanel(DataMap dataMap, String icon, Class type) {
        this.type = type;
        this.icon = icon;
        this.dataMap = dataMap;
        initView();
    }

    public void initView(){
        setLayout(new BorderLayout());
        FormLayout layout = new FormLayout(
                "left:pref, 4dlu, fill:50dlu, 3dlu, fill:120, 3dlu, fill:120", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        this.checkConfig = new JCheckBox();
        this.dataMapLabel = new JLabel(dataMap.getName());
        DataChannelMetaData metaData = Application.getInstance().getMetaData();
        this.toConfigButton = new JButton();
        this.deleteButton = new JButton("Delete config");
        if(metaData.get(dataMap, type) != null) {
            this.toConfigButton.setText("Edit Config");
        } else {
            this.toConfigButton.setText("Create Config");
            if(type == ReverseEngineering.class) {
                checkConfig.setEnabled(false);
            }
        }
        this.toConfigButton.setIcon(ModelerUtil.buildIcon(icon));
        builder.append(checkConfig, dataMapLabel, toConfigButton);
        if(type == ReverseEngineering.class) {
            builder.append(deleteButton);
        }
        this.add(builder.getPanel(), BorderLayout.CENTER);
    }

    public JCheckBox getCheckConfig() {
        return checkConfig;
    }

    public JButton getToConfigButton() {
        return toConfigButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JLabel getDataMapLabel() {
        return dataMapLabel;
    }

    public DataMap getDataMap() {
        return dataMap;
    }
}