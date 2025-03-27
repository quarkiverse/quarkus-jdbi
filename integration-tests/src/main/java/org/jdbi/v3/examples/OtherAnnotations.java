package org.jdbi.v3.examples;

import static org.jdbi.v3.examples.order.OrderSupport.withOrders;

import java.util.Map;

import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.sqlobject.config.KeyColumn;
import org.jdbi.v3.sqlobject.config.UseEnumStrategy;
import org.jdbi.v3.sqlobject.config.ValueColumn;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public final class OtherAnnotations {
    private OtherAnnotations() {
        throw new AssertionError("OtherAnnotations can not be instantiated");
    }

    public static void main(String... args) throws Exception {
        withOrders(jdbi -> {
            jdbi.useExtension(TestDao.class, dao -> {
                assert dao.getBoolEnum() == BoolEnum.TRUE;
                assert dao.getMap().get("key").equals("value");
            });
        });
    }

    interface TestDao {
        @SqlQuery("SELECT 'TRUE'")
        @UseEnumStrategy(EnumStrategy.BY_NAME)
        BoolEnum getBoolEnum();

        @SqlQuery("SELECT 'key' AS key, 'value' AS value")
        @KeyColumn("key")
        @ValueColumn("value")
        Map<String, String> getMap();
    }

    public enum BoolEnum {
        TRUE,
        FALSE
    }
}
