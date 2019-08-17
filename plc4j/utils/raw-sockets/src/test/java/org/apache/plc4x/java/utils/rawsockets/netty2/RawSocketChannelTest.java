/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.plc4x.java.utils.rawsockets.netty2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO write comment
 *
 * @author julian
 * Created by julian on 2019-08-16
 */
public class RawSocketChannelTest {

    private static final Logger logger = LoggerFactory.getLogger(RawSocketChannelTest.class);

    @Test
    public void doConnect() throws InterruptedException {
        Channel channel = null;
        final EventLoopGroup workerGroup = new OioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(RawSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            // TODO we should use an explicit (configurable?) timeout here
            // bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
            bootstrap.handler(new ChannelInitializer<RawSocketChannel>() {
                @Override
                protected void initChannel(RawSocketChannel ch) throws Exception {
                    System.out.println("Initialize Buffer!");
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println("Hello, I received some bytes!");
                            // System.out.println(ByteBufUtil.prettyHexDump(((ByteBuf) msg)));
                        }
                    });
                    ch.pipeline().addLast(new ChannelHandlerAdapter() {
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            cause.printStackTrace();
                        }
                    });
                }
            });
            // Start the client.
            final ChannelFuture f = bootstrap.connect("127.0.0.1", 1234);
            // Wait for sync
            f.sync();
            // Wait till the session is finished initializing.
            channel = f.channel();

            System.out.println("Channel is connected and ready to use...");


            channel.writeAndFlush(Unpooled.wrappedBuffer("Hallo".getBytes()));


            Thread.sleep(10_000);
        } finally {
            if (channel != null) {
                channel.close().sync();
            }
            workerGroup.shutdownGracefully().sync();
        }
    }
}