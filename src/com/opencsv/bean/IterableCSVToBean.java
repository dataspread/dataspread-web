package com.opencsv.bean;

/**
 * Copyright 2015 Bytecode Pty Ltd.
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Converts CSV strings to objects.  Unlike CsvToBean it returns a single record at a time.
 *
 * @param <T> - class to convert the objects to.
 */

public class IterableCSVToBean<T> extends AbstractCSVToBean implements Iterable<T> {
    private MappingStrategy<T> strategy;
    private CSVReader csvReader;
    private CsvToBeanFilter filter;
    private Map<Class<?>, PropertyEditor> editorMap = null;
    private boolean hasHeader;

    /**
     * IterableCSVToBean constructor
     *
     * @param csvReader - CSVReader.  Should not be null.
     * @param strategy  - MappingStrategy used to map csv data to the bean.  Should not be null.
     * @param filter    - Optional CsvToBeanFilter used remove unwanted data from reads.
     */
    public IterableCSVToBean(CSVReader csvReader, MappingStrategy<T> strategy, CsvToBeanFilter filter) {
        this.csvReader = csvReader;
        this.strategy = strategy;
        this.filter = filter;
        this.hasHeader = false;
    }

    /**
     * retrieves the MappingStrategy.
     *
     * @return - the MappingStrategy being used by the IterableCSVToBean.
     */
    protected MappingStrategy<T> getStrategy() {
        return strategy;
    }

    /**
     * retrieves the CSVReader.
     *
     * @return - the CSVReader being used by the IterableCSVToBean.
     */
    protected CSVReader getCSVReader() {
        return csvReader;
    }

    /**
     * retrieves the CsvToBeanFilter
     *
     * @return - the CsvToBeanFilter being used by the IterableCSVToBean.
     */
    protected CsvToBeanFilter getFilter() {
        return filter;
    }

    /**
     * Reads and processes a single line.
     *
     * @return Object of type T with the requested information or null if there is no more lines to process.
     * @throws IllegalAccessException    -  thrown if there is an failure in Introspection.
     * @throws InstantiationException    - thrown when getting the PropertyDescriptor for the class.
     * @throws IOException               - thrown when there is an unexpected error reading the file.
     * @throws IntrospectionException    -  thrown if there is an failure in Introspection.
     * @throws InvocationTargetException -  thrown if there is an failure in Introspection.
     */
    public T nextLine() throws IllegalAccessException, InstantiationException, IOException, IntrospectionException, InvocationTargetException {
        if (!hasHeader) {
            strategy.captureHeader(csvReader);
            hasHeader = true;
        }
        T bean = null;
        String[] line;
        do {
            line = csvReader.readNext();
        } while (line != null && (filter != null && !filter.allowLine(line)));
        if (line != null) {
            bean = strategy.createBean();
            for (int col = 0; col < line.length; col++) {
                PropertyDescriptor prop = strategy.findDescriptor(col);
                if (null != prop) {
                    String value = checkForTrim(line[col], prop);
                    Object obj = convertValue(value, prop);
                    prop.getWriteMethod().invoke(bean, obj);
                }
            }
        }
        return bean;
    }

    /**
     * Attempt to find custom property editor on descriptor first, else try the propery editor manager.
     *
     * @param desc - PropertyDescriptor.
     * @return - the PropertyEditor for the given PropertyDescriptor.
     * @throws InstantiationException - thrown when getting the PropertyEditor for the class.
     * @throws IllegalAccessException - thrown when getting the PropertyEditor for the class.
     */
    protected PropertyEditor getPropertyEditor(PropertyDescriptor desc) throws InstantiationException, IllegalAccessException {
        Class<?> cls = desc.getPropertyEditorClass();
        if (null != cls) {
            return (PropertyEditor) cls.newInstance();
        }
        return getPropertyEditorValue(desc.getPropertyType());
    }

    private PropertyEditor getPropertyEditorValue(Class<?> cls) {
        if (editorMap == null) {
            editorMap = new HashMap<Class<?>, PropertyEditor>();
        }

        PropertyEditor editor = editorMap.get(cls);

        if (editor == null) {
            editor = PropertyEditorManager.findEditor(cls);
            addEditorToMap(cls, editor);
        }

        return editor;
    }

    private void addEditorToMap(Class<?> cls, PropertyEditor editor) {
        if (editor != null) {
            editorMap.put(cls, editor);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return iterator(this);
    }

    private Iterator<T> iterator(final IterableCSVToBean<T> bean) {
        return new Iterator<T>() {
            private T nextBean;

            @Override
            public boolean hasNext() {
                if (nextBean != null) {
                    return true;
                }

                try {
                    nextBean = bean.nextLine();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                return nextBean != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    return null;
                }

                T holder = nextBean;
                nextBean = null;
                return holder;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("This is a read only iterator.");
            }
        };
    }
}
