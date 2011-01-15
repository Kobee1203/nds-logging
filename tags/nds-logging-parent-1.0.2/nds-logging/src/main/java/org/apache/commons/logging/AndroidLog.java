/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging;

/**
 * <p>
 * An Android logging interface abstracting logging APIs. In order to be instantiated successfully by {@link AndroidLogFactory}, classes that
 * implement this interface must have a constructor that takes a single String parameter representing the "name" of this AndroidLog.
 * </p>
 * 
 * <p>
 * This interface extends {@link Log} and use the same six logging levels used by <code>Log</code>.
 * </p>
 * 
 * @author Nicolas Dos Santos
 */
public interface AndroidLog extends org.apache.commons.logging.Log {

}
