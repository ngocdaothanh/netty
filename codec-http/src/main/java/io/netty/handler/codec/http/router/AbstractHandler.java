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
package io.netty.handler.codec.http.router;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

@ChannelHandler.Sharable
public abstract class AbstractHandler<T, RouteLike extends MethodRouter<T, RouteLike>>
extends SimpleChannelInboundHandler<HttpRequest> {
    private static final ByteBuf CONTENT_404 = Unpooled.wrappedBuffer("Not Found".getBytes());

    private final MethodRouter<T, RouteLike> router;

    public AbstractHandler(MethodRouter<T, RouteLike> router) {
        if (router == null) {
          throw new NullPointerException("router must not be null");
        }
        this.router = router;
    }

    public MethodRouter<T, RouteLike> router() {
        return router;
    }

    //--------------------------------------------------------------------------

    /** @param routed Will automatically be released. Please call routed.retain() if you want. */
    protected abstract void routed(ChannelHandlerContext ctx, MethodRouted<T> routed) throws Exception;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
        // "100-continue" header:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec8.html
        //
        // We can experiment sending the header with this command:
        // curl -v -X POST -F "a=b" http://server/
        //
        // We need to write response and flush immediately so that the client
        // knows that it should send the rest of the request.
        //
        // At this point, this request only contains headers, the request body
        // hasn't been set yet. So the processing behind this point shouldn't be
        // run yet.
        if (HttpHeaders.is100ContinueExpected(req)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            return;
        }

        // Route
        HttpMethod                                    method  = req.getMethod();
        QueryStringDecoder                            qsd     = new QueryStringDecoder(req.getUri());
        io.netty.handler.codec.http.routing.Routed<T> jrouted = router.route(method, qsd.path());

        if (jrouted == null) {
            respondNotFound(ctx, req);
            return;
        }

        MethodRouted<T> routed = new MethodRouted<T>(
            jrouted.target(), jrouted.notFound(), req, qsd.path(), jrouted.params(), qsd.parameters()
        );
        routed(ctx, routed);
    }

    protected void respondNotFound(ChannelHandlerContext ctx, HttpRequest req) {
        HttpResponse res = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.NOT_FOUND,
            CONTENT_404.retain()
        );

        HttpHeaders headers = res.headers();
        headers.set(HttpHeaders.Names.CONTENT_TYPE,   "text/plain");
        headers.set(HttpHeaders.Names.CONTENT_LENGTH, CONTENT_404.readableBytes());

        KeepAliveWrite.flush(ctx, req, res);
    }
}
