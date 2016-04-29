package com.opencsv.bean;

/**
 Copyright 2007 Kyle Miller.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

import com.opencsv.CSVWriter;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to export Java beans content to a new CSV spreadsheet file.
 *
 * @author Kali &lt;kali.tystrit@gmail.com&gt;
 * @param <T> - Type of object that is being processed.
 */
public class BeanToCsv<T> {

    /**
     * default constructor.
     */
    public BeanToCsv() {
    }

    /**
     * Writes all the objects, one at a time, to a created csvWriter using the passed in Strategy.
     *
     * @param mapper  - Mapping strategy for the bean.
     * @param writer  - Writer object used to construct the CSVWriter.
     * @param objects - list of objects to write.
     * @return - false if there are no objects to process, true otherwise.
     */
    public boolean write(MappingStrategy<T> mapper, Writer writer,
                         List<?> objects) {
        return write(mapper, new CSVWriter(writer), objects);
    }

    /**
     * Writes all the objects, one at a time, to the csvWriter using the passed in Strategy.
     * @param mapper - Mapping strategy for the bean.
     * @param csv - CSVWriter
     * @param objects - list of objects to write.
     * @return - false if there are no objects to process, true otherwise.
     */
    public boolean write(MappingStrategy<T> mapper, CSVWriter csv, List<?> objects) {
        if (objects == null || objects.isEmpty()) {
            return false;
        }

        try {
            csv.writeNext(processHeader(mapper));
            List<Method> getters = findGetters(mapper);
            processAndWriteObjects(csv, objects, getters);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error writing CSV !", e);
        }
    }

    /**
     * Processes a list of objects.
     * @param csv - csvWriter
     * @param objects - list of objects to process
     * @param getters - list of getter methods to retrieve the data from the objects.
     * @throws IntrospectionException -  thrown if there is an failure in Introspection.
     * @throws IllegalAccessException -  thrown if there is an failure in Introspection.
     * @throws InvocationTargetException -  thrown if there is an failure in Introspection.
     */
    private void processAndWriteObjects(CSVWriter csv, List<?> objects, List<Method> getters) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (Object obj : objects) {
            String[] line = processObject(getters, obj);
            csv.writeNext(line);
        }
    }

    /**
     * Processes the header for the bean.
     * @param mapper  MappingStrategy for Bean
     * @return String array with header values.
     * @throws IntrospectionException -  thrown if there is an failure in Introspection.
     */
    protected String[] processHeader(MappingStrategy<T> mapper) throws IntrospectionException {
        List<String> values = new ArrayList<String>();
        int i = 0;
        PropertyDescriptor prop = mapper.findDescriptor(i);
        while (prop != null) {
            values.add(prop.getName());
            i++;
            prop = mapper.findDescriptor(i);
        }
        return values.toArray(new String[0]);
    }

    /**
     * Retrieve all the information out of an object.
     * @param getters - List of methods to retrieve information.
     * @param bean - object to get the information from.
     * @return String array containing the information from the object
     * @throws IntrospectionException - thrown by error in introspection.
     * @throws IllegalAccessException - thrown by error in introspection.
     * @throws InvocationTargetException - thrown by error in introspection.
     */
    protected String[] processObject(List<Method> getters, Object bean) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException {
        List<String> values = new ArrayList<String>();
        // retrieve bean values
        for (Method getter : getters) {
            Object value = getter.invoke(bean, (Object[]) null);
            if (value == null) {
                values.add("null");
            } else {
                values.add(value.toString());
            }
        }
        return values.toArray(new String[0]);
    }

    /**
     * Build getters list from provided mapper.
     * @param mapper MappingStrategy for Bean
     * @return - list of methods for getting the data in the bean.
     * @throws IntrospectionException - thrown if there is an failure in Introspection.
     */
    private List<Method> findGetters(MappingStrategy<T> mapper)
            throws IntrospectionException {
        int i = 0;
        PropertyDescriptor prop = mapper.findDescriptor(i);
        // build getters methods list
        List<Method> readers = new ArrayList<Method>();
        while (prop != null) {
            readers.add(prop.getReadMethod());
            i++;
            prop = mapper.findDescriptor(i);
        }
        return readers;
    }
}
