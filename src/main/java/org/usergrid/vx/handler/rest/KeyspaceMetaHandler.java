package org.usergrid.vx.handler.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usergrid.vx.experimental.IntraReq;
import org.usergrid.vx.experimental.Operations;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * @author zznate
 * @author boneill42
 */
public class KeyspaceMetaHandler extends IntraHandlerRest {

  private Logger log = LoggerFactory.getLogger(KeyspaceMetaHandler.class);

  public KeyspaceMetaHandler(Vertx vertx) {
    super(vertx);
  }

  @Override
  public void handleRequestAsync(final HttpServerRequest request, Buffer buffer) {
    log.debug("In ColumnFamilyMetaHandler#handleRequestAsync");
    IntraReq req = new IntraReq();
    if (request.method.equals("GET")) {
      handleGet(request, buffer, req);
    } else if (request.method.equals("POST")) {
      handlePost(request, buffer, req);
    }    
    delegateAndReply(request, req);
  }

  public void handleGet(final HttpServerRequest request, Buffer buffer, IntraReq req) {
    req.add(Operations.listColumnFamilyOp(request.params().get("ks")));
  }

  public void handlePost(final HttpServerRequest request, Buffer buffer, IntraReq req) {
    // TODO: where should replication come from?
    req.add(Operations.createKsOp(request.params().get("ks"), 1));
  }
}
