/* 
 *   Copyright 2013 Nate McCall and Edward Capriolo
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
*/
package org.usergrid.vx.experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.IMutation;
import org.apache.cassandra.db.RowMutation;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.exceptions.OverloadedException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.service.StorageProxy;
import org.usergrid.vx.server.operations.HandlerUtils;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

public class TwoExBuilder implements ServiceProcessor {

  @Override
  public void process(JsonObject request, JsonObject state, JsonObject response, EventBus eb) {
    System.out.println("called");
   
    JsonObject params = request.getObject("mpparams");
    String uid = (String) params.getString("userid");
    String fname = (String) params.getString("fname");
    String lname = (String) params.getString("lname");
    String city = (String) params.getString("city");

    RowMutation rm = new RowMutation("myks", HandlerUtils.instance.byteBufferForObject(uid));
    QueryPath qp = new QueryPath("users", null, HandlerUtils.instance.byteBufferForObject("fname"));
    rm.add(qp, HandlerUtils.instance.byteBufferForObject(fname), System.nanoTime());
    QueryPath qp2 = new QueryPath("users", null, HandlerUtils.instance.byteBufferForObject("lname"));
    rm.add(qp2, HandlerUtils.instance.byteBufferForObject(lname), System.nanoTime());
    
 
    RowMutation rm2 = new RowMutation("myks", HandlerUtils.instance.byteBufferForObject(city));
    QueryPath qp3 = new QueryPath("usersbycity", null, HandlerUtils.instance.byteBufferForObject(uid));
    rm2.add(qp3, HandlerUtils.instance.byteBufferForObject(""), System.nanoTime());
    
    QueryPath qp4 = new QueryPath("usersbylast", null, HandlerUtils.instance.byteBufferForObject(lname));
    rm.add(qp4, HandlerUtils.instance.byteBufferForObject(uid), System.nanoTime());
    List<IMutation> mutations = new ArrayList<IMutation>();
    mutations.add(rm);
    mutations.add(rm2);
    try {
      StorageProxy.mutate(mutations, ConsistencyLevel.ONE);
    } catch (WriteTimeoutException | UnavailableException | OverloadedException e) {
      e.printStackTrace();
      response.putString("status", "FAILED");
    }
    response.putString("status", "OK");
    System.out.println("done");
  }
}
