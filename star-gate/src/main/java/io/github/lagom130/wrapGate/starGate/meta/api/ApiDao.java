package io.github.lagom130.wrapGate.starGate.meta.api;

import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.UUID;

public class ApiDao {

  private PgPool pool;

  public ApiDao(PgPool pool) {
    this.pool = pool;
  }

  public Future<ApiDO> getById(String id) {
    return pool.getConnection().compose(conn -> {
        return SqlTemplate.forQuery(conn, "select id, name, host, port, path, method, enabled, tenant_id, updated_time, created_time from  where id=#{id}")
          .mapTo(ApiDO.class)
          .execute(Map.of("id", id)).onComplete(ar ->conn.close());
      }).compose(rs -> rs.size() == 1 ? Future.succeededFuture(rs.iterator().next()): Future.failedFuture(new Throwable("not found")));
  }

  public Future<Void> add(String name, String host, int port, String path, String method, boolean enabled, String tenantId) {
    String id = UUID.randomUUID().toString();
    return pool.getConnection().compose(conn -> {
        return SqlTemplate.forUpdate(conn, "insert into gateway.api (id, name, host, port, path, method, enabled, tenant_id, updated_time, created_time) values(#{id}, #{name}, #{host}, #{port}, #{path}, #{method}, #{enabled}, #{tenant_id}, now(), now()})")
          .execute(Map.of(
            "id", id,
            "name", name,
            "host", host,
            "path", path,
            "method", method,
            "enabled", enabled,
            "tenantId", tenantId
          ));
      }).compose(rs -> rs.rowCount() == 1 ? Future.succeededFuture() : Future.failedFuture("insert failed"));
  }

}
