package org.jdbi.v3.examples;

import static org.jdbi.v3.examples.order.OrderSupport.withOrders;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import org.jdbi.v3.core.argument.CharSequenceArgumentFactory;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.GenericMapMapperFactory;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.examples.order.Order;
import org.jdbi.v3.examples.order.OrderBean;
import org.jdbi.v3.examples.order.OrderFields;
import org.jdbi.v3.postgres.JavaTimeMapperFactory;
import org.jdbi.v3.sqlobject.SingleValue;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactories;
import org.jdbi.v3.sqlobject.config.RegisterArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterBeanMappers;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactories;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterColumnMappers;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMappers;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMappers;
import org.jdbi.v3.sqlobject.config.RegisterObjectArgumentFactories;
import org.jdbi.v3.sqlobject.config.RegisterObjectArgumentFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactories;
import org.jdbi.v3.sqlobject.config.RegisterRowMapperFactory;
import org.jdbi.v3.sqlobject.config.RegisterRowMappers;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public final class ArrayAnnotations {
    private ArrayAnnotations() {
        throw new AssertionError("ArrayAnnotations can not be instantiated");
    }

    public static void main(String... args) throws Exception {
        withOrders(jdbi -> {
            jdbi.useExtension(TestDao.class, dao -> {
                assert "Hello".equals(dao.getCarseq("Hello"));
                assert dao.getOrderBean() != null;
                assert dao.getOrderConstructor() != null;
                assert dao.getOrderId() >= 0;
                assert dao.getOrderIdAsTime().equals(LocalDateTime.of(2004, 10, 19, 10, 23, 54));
                assert "Hello".equals(dao.getStringArgument("Hello"));
                assert dao.getNumericLevels().get("high").equals(BigDecimal.valueOf(3.0));
                assert dao.getOrderInMap() != null;
            });
        });
    }

    interface TestDao {
        @SqlQuery("SELECT :charseq")
        @RegisterArgumentFactories({
                @RegisterArgumentFactory(CharSequenceArgumentFactory.class)
        })
        String getCarseq(CharSequence charseq);

        @SqlQuery("SELECT * FROM orders LIMIT 1")
        @RegisterBeanMappers(@RegisterBeanMapper(OrderBean.class))
        OrderBean getOrderBean();

        @SqlQuery("SELECT * FROM orders LIMIT 1")
        @RegisterConstructorMappers(@RegisterConstructorMapper(Order.class))
        Order getOrderConstructor();

        @SqlQuery("SELECT * FROM orders LIMIT 1")
        @RegisterFieldMappers(@RegisterFieldMapper(OrderFields.class))
        OrderFields getOrderFields();

        @SqlQuery("SELECT id FROM orders LIMIT 1")
        @RegisterColumnMappers(@RegisterColumnMapper(IdMapper.class))
        Integer getOrderId();

        @SqlQuery("SELECT TIMESTAMP '2004-10-19 10:23:54'")
        @RegisterColumnMapperFactories(@RegisterColumnMapperFactory(JavaTimeMapperFactory.class))
        LocalDateTime getOrderIdAsTime();

        @SqlQuery("SELECT :string")
        @RegisterObjectArgumentFactories(@RegisterObjectArgumentFactory(String.class))
        String getStringArgument(String string);

        @SqlQuery("SELECT * FROM orders LIMIT 1")
        @RegisterRowMappers(@RegisterRowMapper(MapMapper.class))
        @SingleValue
        Map<String, Object> getOrderInMap();

        // Example from docs
        @SqlQuery("SELECT 1.0 AS LOW, 2.0 AS MEDIUM, 3.0 AS HIGH")
        @RegisterRowMapperFactories(@RegisterRowMapperFactory(GenericMapMapperFactory.class))
        @SingleValue
        Map<String, BigDecimal> getNumericLevels();
    }

    public static class IdMapper implements ColumnMapper<Integer> {
        @Override
        public Integer map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
            return Integer.valueOf(r.getInt(columnNumber));
        }
    }
}
