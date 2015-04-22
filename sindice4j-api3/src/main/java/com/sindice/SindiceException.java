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

package com.sindice;

/**
 * SindiceException - usually thrown where there is an error in contacting
 * the <i>Sindice.com</i> website or while processing its results.
 * 
 * @author milstan
 */
public class SindiceException extends Exception {

    public SindiceException(String message) {
        super(message);
    }

    /**
     * @param exception
     */
    public SindiceException(Exception exception) {
        super(exception);
    }

    /**
     * @param message
     * @param exception
     */
    public SindiceException(String message, Exception exception) {
        super(message, exception);
    }

}
