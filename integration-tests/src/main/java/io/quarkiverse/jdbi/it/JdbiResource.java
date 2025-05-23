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
package io.quarkiverse.jdbi.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.jdbi.v3.examples.ArrayAnnotations;
import org.jdbi.v3.examples.CustomSqlArrayType;
import org.jdbi.v3.examples.OtherAnnotations;
import org.jdbi.v3.examples.ResultsAsMultimap;

@Path("/jdbi")
@ApplicationScoped
public class JdbiResource {
    // add some rest methods here

    @GET
    public String hello() throws Exception {
        CustomSqlArrayType.main();
        ResultsAsMultimap.main();
        ArrayAnnotations.main();
        OtherAnnotations.main();

        return "OK";
    }
}
