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

package com.sindice.query;

/**
 * This interface provides a custom mechanism for generating the <i>HTTP parameters</i>
 * string from the implementing class.
 *
 * @see com.sindice.query.v3.CacheQuery
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public interface HTTPParameter {

    /**
     * @return the HTTP parameters associated to the implementing class.
     */
    String getParameters();

}
