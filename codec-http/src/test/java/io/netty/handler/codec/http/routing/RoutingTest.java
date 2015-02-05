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

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.routing.StringRouter.*;

import static org.junit.Assert.*;
import org.junit.Test;

public class RoutingTest {
    @Test
    public void testIgnoreSlashesAtBothEnds() {
        assertEquals("index", router.route(GET, "articles").target());
        assertEquals("index", router.route(GET, "/articles").target());
        assertEquals("index", router.route(GET, "//articles").target());
        assertEquals("index", router.route(GET, "articles/").target());
        assertEquals("index", router.route(GET, "articles//").target());
        assertEquals("index", router.route(GET, "/articles/").target());
        assertEquals("index", router.route(GET, "//articles//").target());
    }

    @Test
    public void testHandleEmptyParams() {
        Routed<String> routed = router.route(GET, "/articles");
        assertEquals("index", routed.target());
        assertEquals(0,       routed.params().size());
    }

    @Test
    public void testHandleParams() {
        Routed<String> routed = router.route(GET, "/articles/123");
        assertEquals("show", routed.target());
        assertEquals(1,      routed.params().size());
        assertEquals("123",  routed.params().get("id"));
    }

    @Test
    public void testHandleNone() {
        MethodlessRouter<String> router = new MethodlessRouter<String>().pattern("/articles", "index");
        Routed<String> routed = router.route("/noexist");
        assertEquals(true, routed == null);
    }

    @Test
    public void testHandleSplatWildcard() {
        Routed<String> routed = router.route(GET, "/download/foo/bar.png");
        assertEquals("download",    routed.target());
        assertEquals(1,             routed.params().size());
        assertEquals("foo/bar.png", routed.params().get("*"));
    }

    @Test
    public void testHandleOrder() {
        Routed<String> routed1 = router.route(GET, "/articles/new");
        assertEquals("new", routed1.target());
        assertEquals(0,     routed1.params().size());

        Routed<String> routed2 = router.route(GET, "/articles/123");
        assertEquals("show",    routed2.target());
        assertEquals(1,         routed2.params().size());
        assertEquals("123",     routed2.params().get("id"));

        Routed<String> routed3 = router.route(GET, "/notfound");
        assertEquals("404", routed3.target());
        assertEquals(0,     routed3.params().size());
    }

    @Test
    public void testHandleAnyMethod() {
        Routed<String> routed1 = router.route(GET, "/anyMethod");
        assertEquals("anyMethod", routed1.target());
        assertEquals(0,           routed1.params().size());

        Routed<String> routed2 = router.route(POST, "/anyMethod");
        assertEquals("anyMethod", routed2.target());
        assertEquals(0,           routed2.params().size());
    }

    @Test
    public void testHandleRemoveByTarget() {
        MethodlessRouter<String> router = new MethodlessRouter<String>().pattern("/articles", "index");
        router.removeTarget("index");
        Routed<String> routed = router.route("/articles");
        assertEquals(true, routed == null);
    }

    @Test
    public void testHandleRemoveByPath() {
        MethodlessRouter<String> router = new MethodlessRouter<String>().pattern("/articles", "index");
        router.removePath("/articles");
        Routed<String> routed = router.route("/articles");
        assertEquals(true, routed == null);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testHandleSubclasses() {
        MethodlessRouter<Class<? extends Action>> router = new MethodlessRouter<Class<? extends Action>>();
        router.pattern("/articles",     Index.class);
        router.pattern("/articles/:id", Show.class);

        Routed<Class<? extends Action>> routed1 = router.route("/articles");
        Routed<Class<? extends Action>> routed2 = router.route("/articles/123");
        assertEquals(Index.class, routed1.target());
        assertEquals(Show .class, routed2.target());
    }
}
