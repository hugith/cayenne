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
package org.apache.cayenne.tools;

import org.apache.cayenne.dbsync.DbSyncModule;
import org.apache.cayenne.dbsync.reverse.configuration.ToolsModule;
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportAction;
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportConfiguration;
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportConfigurationValidator;
import org.apache.cayenne.dbsync.reverse.dbimport.DbImportModule;
import org.apache.cayenne.dbsync.reverse.dbimport.ReverseEngineering;
import org.apache.cayenne.dbsync.reverse.filters.FiltersConfigBuilder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.util.Util;
import org.slf4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Maven mojo to reverse engineer datamap from DB.
 *
 * @since 3.0
 */
@Mojo(name = "cdbimport", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class DbImporterMojo extends AbstractMojo {

    /**
     * Java class implementing org.apache.cayenne.dba.DbAdapter. This attribute
     * is optional, the default is AutoAdapter, i.e. Cayenne would try to guess
     * the DB type.
     */
    @Parameter(defaultValue = "org.apache.cayenne.dba.AutoAdapter")
    private String adapter;

    /**
     * Connection properties.
     *
     * @see DbImportDataSourceConfig
     * @since 4.0
     */
    @Parameter
    private DbImportDataSourceConfig dataSource = new DbImportDataSourceConfig();

    /**
     * DataMap XML file to use as a base for DB importing.
     */
    @Parameter(required = true)
    private File map;

    /**
     * An object that contains reverse engineering rules.
     */
    @Parameter(name = "dbimport", property = "dbimport", alias = "dbImport")
    private ReverseEngineering dbImportConfig = new ReverseEngineering();

    public void execute() throws MojoExecutionException, MojoFailureException {

        Logger logger = new MavenLogger(this);

        // check missing data source parameters
        dataSource.validate();

        DbImportConfiguration config = createConfig(logger);
        Injector injector = DIBootstrap.createInjector(
                new DbSyncModule(), new ToolsModule(logger), new DbImportModule());

        DbImportConfigurationValidator validator = new DbImportConfigurationValidator(
                dbImportConfig, config, injector);
        try {
            validator.validate();
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        try {
            injector.getInstance(DbImportAction.class).execute(config);
        } catch (Exception ex) {
            Throwable th = Util.unwindException(ex);

            String message = "Error importing database schema";

            if (th.getLocalizedMessage() != null) {
                message += ": " + th.getLocalizedMessage();
            }

            getLog().error(message);
            throw new MojoExecutionException(message, th);
        }
    }

    DbImportConfiguration createConfig(Logger logger) {

        DbImportConfiguration config = new DbImportConfiguration();
        config.setAdapter(adapter);
        config.setDefaultPackage(dbImportConfig.getDefaultPackage());
        config.setDriver(dataSource.getDriver());
        config.setFiltersConfig(new FiltersConfigBuilder(dbImportConfig).build());
        config.setForceDataMapCatalog(dbImportConfig.isForceDataMapCatalog());
        config.setForceDataMapSchema(dbImportConfig.isForceDataMapSchema());
        config.setLogger(logger);
        config.setMeaningfulPkTables(dbImportConfig.getMeaningfulPkTables());
        config.setNamingStrategy(dbImportConfig.getNamingStrategy());
        config.setPassword(dataSource.getPassword());
        config.setSkipRelationshipsLoading(dbImportConfig.getSkipRelationshipsLoading());
        config.setSkipPrimaryKeyLoading(dbImportConfig.getSkipPrimaryKeyLoading());
        config.setStripFromTableNames(dbImportConfig.getStripFromTableNames());
        config.setTableTypes(dbImportConfig.getTableTypes());
        config.setTargetDataMap(map);
        config.setUrl(dataSource.getUrl());
        config.setUsername(dataSource.getUsername());
        config.setUsePrimitives(dbImportConfig.isUsePrimitives());
        config.setUseJava7Types(dbImportConfig.isUseJava7Types());

        return config;
    }

    public File getMap() {
        return map;
    }

    /**
     * Used only in tests, Maven will inject value directly into the "map" field
     */
    public void setMap(File map) {
        this.map = map;
    }

    /**
     * This setter is used by Maven when defined {@code <dbimport>} tag
     */
    public void setDbimport(ReverseEngineering dbImportConfig) {
        this.dbImportConfig = dbImportConfig;
    }

    /**
     * This setter is used by Maven {@code <dbImport>} tag
     */
    public void setDbImport(ReverseEngineering dbImportConfig) {
        this.dbImportConfig = dbImportConfig;
    }

    public ReverseEngineering getReverseEngineering() {
        return dbImportConfig;
    }

}


