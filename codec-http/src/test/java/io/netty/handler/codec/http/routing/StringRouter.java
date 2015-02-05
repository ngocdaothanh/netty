/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.http.routing;

import io.netty.handler.codec.http.HttpMethod;

public class StringRouter extends Router<HttpMethod, String, StringRouter> {
  protected StringRouter getThis() { return this; }

  protected HttpMethod CONNECT() { return HttpMethod.CONNECT; }
  protected HttpMethod DELETE()  { return HttpMethod.DELETE; }
  protected HttpMethod GET()     { return HttpMethod.GET; }
  protected HttpMethod HEAD()    { return HttpMethod.HEAD; }
  protected HttpMethod OPTIONS() { return HttpMethod.OPTIONS; }
  protected HttpMethod PATCH()   { return HttpMethod.PATCH; }
  protected HttpMethod POST()    { return HttpMethod.POST; }
  protected HttpMethod PUT()     { return HttpMethod.PUT; }
  protected HttpMethod TRACE()   { return HttpMethod.TRACE; }

  public static final StringRouter router = new StringRouter()
    .GET("/articles",             "index")
    .GET("/articles/:id",         "show")
    .GET("/articles/:id/:format", "show")
    .GET_FIRST("/articles/new",   "new")
    .POST("/articles",            "post")
    .PATCH("/articles/:id",       "patch")
    .DELETE("/articles/:id",      "delete")
    .ANY("/anyMethod",            "anyMethod")
    .GET("/download/:*",          "download")
    .notFound("404");

  static {
      // Visualize the routes
      System.out.println(router.toString());
  }

  abstract static class Action { }
  static class Index extends Action { }
  static class Show  extends Action { }
}
