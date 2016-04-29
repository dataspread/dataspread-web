package com.opencsv.bean;

/**
 * Copyright 2005 Bytecode Pty Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.opencsv.CSVReader;

/**
 * Builder for creating a IterableCSVToBean.  This should be the preferred method of
 * creating a IterableCSVToBean to keep the number of constructors to a minimum.
 * <p>
 * <code>
 * IterableCSVToBean bean =
 * new IterableCSVToBean()
 * .withReader(csvReader)
 * .withMapper(mappingStrategy)
 * .withFilter(csvToBeanFilter)
 * .build();
 * </code>
 *
 * @param <T>
 * @see IterableCSVToBean
 */
public class IterableCSVToBeanBuilder<T> {

    public static final String NO_MAPPING_STRATEGY_DEFINED = "Unable to instantiate IterableCSVToBeanBuilder because there is no MappingStrategy defined.";
    public static final String NO_READER_DEFINED = "Unable to instantiate IterableCSVToBeanBuilder because there is no CSVReader defined.";

    private MappingStrategy<T> mapper;
    private CSVReader csvReader;
    private CsvToBeanFilter filter;

    /**
     * default constructor
     */
    public IterableCSVToBeanBuilder() {
    }

    /**
     * Creates the IterableCSVToBean.
     * <p>
     * Will throw an run time exception if the MappingStrategy or CSVReader is not set.
     *
     * @return an instance of IterableCSVToBean with
     */
    public IterableCSVToBean<T> build() {
        if (mapper == null) {
            throw new RuntimeException(NO_MAPPING_STRATEGY_DEFINED);
        }
        if (csvReader == null) {
            throw new RuntimeException(NO_READER_DEFINED);
        }
        return new IterableCSVToBean<T>(csvReader, mapper, filter);
    }

    /**
     * Sets the mappingStrategy to be used by the builder.
     *
     * @param mappingStrategy - an class extending MappingStrategy
     * @return the builder with the MappingStrategy set
     */
    public IterableCSVToBeanBuilder<T> withMapper(final MappingStrategy<T> mappingStrategy) {
        this.mapper = mappingStrategy;
        return this;
    }

    /**
     * Sets the reader to be used by the builder.
     *
     * @param reader - CSVReader be be incorporated in the builder.
     * @return the builder with the CSVReader set
     */
    public IterableCSVToBeanBuilder<T> withReader(final CSVReader reader) {
        this.csvReader = reader;
        return this;
    }

    /**
     * used by unit tests
     *
     * @return - the MappingStrategy to be used by the builder.
     */
    protected MappingStrategy getStrategy() {
        return mapper;
    }

    /**
     * used by unit tests
     *
     * @return - the csvReader to be used by the builder.
     */
    protected CSVReader getCsvReader() {
        return csvReader;
    }

    /**
     * used by unit tests
     *
     * @return - filter to be used by the builder.
     */
    protected Object getFilter() {
        return filter;
    }

    /**
     * Sets the filter used to remove unwanted data from csv file
     *
     * @param filter - an class extending CsvToBeanFilter
     * @return the builder with the filter set
     */
    public IterableCSVToBeanBuilder<T> withFilter(final CsvToBeanFilter filter) {
        this.filter = filter;
        return this;
    }
}
