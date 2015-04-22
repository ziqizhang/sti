/*
 * Copyright 2010 Milan Stankovic <milstan@hypios.com>
 * Hypios.com, STIH, University Paris-Sorbonne &
 * Davide Palmisano,  Fondazione Bruno Kessler <palmisano@fbk.eu>
 * Michele Mostarda,  Fondazione Bruno Kessler <mostarda@fbk.eu>
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

package com.sindice.result;

import org.json.simple.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class for mapping a <i>JSON</i> object to a given java object.
 * The mapping is performed on the object's fields, the association is done
 * using the {@link com.sindice.result.CacheResultMapper.JSONMapping} annotation.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class CacheResultMapper {

    /**
     * Internal date formatter for <i>timestamp</i> date format.
     */
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Returns the name of the mapping of the given field.
     *
     * @param field field for which retrieve mapping.
     * @return the mapping name.
     * @see com.sindice.result.CacheResultMapper.JSONMapping
     */
    public static String getFieldMapping(Field field) {
        JSONMapping mapping = field.getAnnotation(JSONMapping.class);
        if(mapping == null) {
            return field.getName();
        }
        final String value = mapping.value();
        if(value.trim().length() == 0) {
            throw new IllegalArgumentException( String.format("Invalid mapping value: '%s'.", value) );
        }
        return value;
    }

    /**
     * Performs a mapping of the content of <code>node</code> <i>JSON object</i>
     * on <code>cacheResult</code>.
     *
     * @param cacheResult the object to be populated.
     * @param node the data source of the mapping.
     */
    @SuppressWarnings("unchecked")
    public static void mapTo(CacheResult cacheResult, JSONObject node) {
        Field[] fields = cacheResult.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            final String fieldMapping = getFieldMapping(field);
            final Object fieldValue = node.get(fieldMapping);
            final Class fieldType = field.getType();
            try {
                if (fieldType.isArray()) {
                    final Object array;
                    if (fieldValue != null) {
                        array = instantiateArray(
                                fieldType.getComponentType(),
                                fieldValue instanceof List ? (List) fieldValue : Arrays.asList(fieldValue)
                        );
                    } else {
                        array = Array.newInstance(fieldType.getComponentType(), 0);
                    }
                    field.set(cacheResult, array);
                } else if (fieldValue == null) {
                    continue;
                } else if (fieldType.isPrimitive()) {
                    field.set(cacheResult, getConvertedType(fieldValue.toString(), fieldType));
                } else if (fieldType.equals(String.class)) {
                    field.set(cacheResult, fieldValue);
                } else if (fieldType.isEnum()) {
                    field.set( cacheResult, getEnumValue(fieldType, fieldValue.toString().toLowerCase()) );
                } else if (fieldType.equals(Date.class)) {
                    try {
                        field.set(cacheResult, dateFormat.parse(fieldValue.toString()));
                    } catch (ParseException pe) {
                        throw new RuntimeException(
                                String.format("Invalid format for date at field %s ", field),
                                pe
                        );
                    }
                } else {
                    throw new IllegalArgumentException(
                            String.format("Cannot find binding for field '%s'", field)
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format(
                                "An error occurred while mapping value '%s' to field '%s'.",
                                fieldValue,
                                field
                        ),e
                );
            }
        }
    }

    /**
     * Maps the given <i>JSON object</i> to a new instance
     * of {@link com.sindice.result.CacheResult}.
     *
     * @param node JSON object to be mapped.
     * @return a new instance of {@link com.sindice.result.CacheResult} with valorized fields.
     */
    public static CacheResult mapToCacheResult(JSONObject node) {
        final CacheResult cacheResult = new CacheResult();
        mapTo(cacheResult, node);
        return cacheResult;
    }

    /**
     * Converts the input type in the given type instance.
     *
     * @param in input type expressed as string.
     * @param type expected type for returned instance.
     * @return instance of type <code>type</code>.
     * @throws IllegalArgumentException if type is not supported.
     */
    private static Object getConvertedType(String in, Class type) {
        if(type.equals(String.class)) {
            return in;
        }
        if(type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(in);
        }
        if(type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(in);
        }
        if(type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(in);
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    /**
     * Returns <code>true</code> if the given class represents a primitive or {@link String} class.
     *
     * @param c input type.
     * @return <code>true</code> if <code>type</code> is primitive, <code>false</code> otherwise. 
     */
    private static boolean isPrimitive(Class c) {
        return c.isPrimitive() || c.equals(String.class);
    }

    /**
     * Instantiates an array of elements of type <code>targetType</code> containing the
     * values of the given <code>jsonArray</code> list.
     *
     * @param targetType the type of the returned array element.
     * @param jsonArray the input data to be copied within the output array.
     * @return the new array instance.
     */
    @SuppressWarnings("unchecked")
    private static Object[] instantiateArray(Class targetType, List jsonArray) {
        final int jsonArraySize = jsonArray == null ? 0 : jsonArray.size();
        Object[] result = (Object[]) Array.newInstance(targetType, jsonArraySize);
        for(int i = 0; i < jsonArraySize; i++) {
            final Object jsonElement = jsonArray.get(i);
            if(isPrimitive(targetType)) {
                result[i] = jsonElement;
            } else if(targetType.isEnum()) {
                final Object value = getEnumValue(targetType, jsonElement.toString().toLowerCase());
                Array.set(result, i, value);
            }
        }
        return result;
    }

    /**
     * Converts an enum string to the representing object.
     * If the mapping is not found will be returned
     * the last ordinal value.
     *
     * @param enumType enumeration class.
     * @param valueStr enumeration string.
     * @return the enum instance representing <i>valueStr</i>.
     */
    @SuppressWarnings("unchecked")
    private static Object getEnumValue(Class enumType, String valueStr) {
        try {
            return Enum.valueOf(enumType, valueStr);
        } catch (Exception e) {
            final Object[] enums = enumType.getEnumConstants();
            return enums[enums.length - 1];
        }
    }

    /**
     * Preventing instantiation.
     */
    private CacheResultMapper(){}

    /**
     * Provides a mapping of <i>Java object instance fields</i> to <i>JSON object names</code>.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface JSONMapping {
        /**
         * @return name of expected JSON key.
         */
        String value();
    }

}
