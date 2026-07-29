/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.examples;

import static org.jdbi.v3.examples.support.DatabaseSupport.withDatabase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.jdbi.v3.meta.Legacy;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * Test case for JDBI 3.52+ Legacy annotation proxy support.
 *
 * This example ensures that the org.jdbi.v3.meta.Legacy proxy
 * is properly registered for native image compilation.
 *
 * The Legacy annotation is used explicitly to force legacy behavior
 * for java.time types, and it uses Proxy.newProxyInstance() internally
 * to create annotation instances, which requires proper native image
 * proxy registration.
 */
public final class JavaTimeExample {

    public static void main(String... args) throws Exception {
        // Test 1: Original java.time support (implicit legacy handling via LegacyArgumentFactory)
        withDatabase(jdbi -> {
            // Create test table with various time columns
            jdbi.withHandle(handle -> handle.execute(
                    "CREATE TABLE IF NOT EXISTS time_tests (id INT, local_date DATE, local_time TIMESTAMP, zoned_time TIMESTAMP)"));

            // Test basic LocalDate/LocalDateTime support
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            jdbi.withHandle(handle -> {
                handle.createUpdate("INSERT INTO time_tests (id, local_date, local_time) VALUES (?, ?, ?)")
                        .bind(0, 1)
                        .bind(1, today)
                        .bind(2, now)
                        .execute();
                return null;
            });

            LocalDate retrieved = jdbi.withHandle(handle -> handle
                    .createQuery("SELECT local_date FROM time_tests WHERE id = ?")
                    .bind(0, 1)
                    .mapTo(LocalDate.class)
                    .one());
            assert retrieved != null : "Failed to retrieve local date";
            assert retrieved.equals(today) : "Retrieved date doesn't match inserted date";

            LocalDateTime retrievedTime = jdbi.withHandle(handle -> handle
                    .createQuery("SELECT local_time FROM time_tests WHERE id = ?")
                    .bind(0, 1)
                    .mapTo(LocalDateTime.class)
                    .one());
            assert retrievedTime != null : "Failed to retrieve local time";
        });

        // Test 2: Explicit @Legacy annotation support
        withDatabase(jdbi -> {
            // Create test table with timestamp
            jdbi.withHandle(handle -> handle
                    .execute("CREATE TABLE IF NOT EXISTS events (id INT, event_time TIMESTAMP)"));

            // Test java.time support with @Legacy annotation
            // This triggers the annotation proxy creation and tests that the
            // @Legacy proxy is properly registered for native image compilation
            jdbi.useExtension(EventDao.class, dao -> {
                ZonedDateTime now = ZonedDateTime.now();

                // Insert using @Legacy annotation on parameter binding
                dao.insertLegacyTime(now);

                // Retrieve using @Legacy annotation on return type
                ZonedDateTime retrieved = dao.getLegacyTime(1);
                assert retrieved != null : "Failed to retrieve event time";
                assert retrieved.toLocalDateTime().toLocalDate()
                        .equals(now.toLocalDateTime().toLocalDate()) : "Retrieved date doesn't match inserted date";
            });
        });
    }

    interface EventDao {
        @SqlUpdate("INSERT INTO events (id, event_time) VALUES (1, :time)")
        // Force legacy behavior for this specific parameter binding
        void insertLegacyTime(@Bind("time") @Legacy ZonedDateTime time);

        @SqlQuery("SELECT event_time FROM events WHERE id = :id")
        // Force legacy behavior when extracting the result column
        @Legacy
        ZonedDateTime getLegacyTime(@Bind("id") int id);
    }
}
