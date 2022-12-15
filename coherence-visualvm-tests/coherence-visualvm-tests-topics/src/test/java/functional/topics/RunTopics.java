/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package functional.topics;


import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import com.tangosol.net.Session;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import com.tangosol.util.Base;

import static com.tangosol.net.topic.Subscriber.Name.inGroup;


/**
 * Starts topics processes for tests.
 *
 * @author tam 2022.11.31
 *
 */
public class RunTopics {

    private static Publisher<String>  publisherPublic;
    private static Subscriber<String> subscriberPublic;
    private static Publisher<String>  publisherPrivate;
    private static Subscriber<String> subscriberPrivate;

    private static Thread thread = null;

    public RunTopics()
        {
        }

    public void startTopics()
        {
        System.out.println("Start topics");

        thread = new Thread(new Runner());
        thread.setDaemon(false);
        thread.start();
        }

    public void stopTopics()
        {
        System.out.println("Stop topics");
        thread.stop();
        }

    public static Void receive(Subscriber.Element<String> element, Throwable throwable, Subscriber<String> subscriber)
        {
        if (throwable != null)
            {
            if (throwable instanceof CancellationException)
                {
                // exiting process, ignore.
                }
            }
        else
            {
            System.out.println("received: " + element.getValue());
            element.commit();
            subscriber.receive().handle((v, err)->receive(v, err, subscriber));
            }
        return null;
        }

    public static class Runner
            implements Runnable
        {

        public Runner()
            {
            try
                {
                Session session = Session.create();
                publisherPublic   = session.createPublisher("public-messages");
                subscriberPublic  = session.createSubscriber("public-messages");
                subscriberPrivate = session.createSubscriber("private-messages", inGroup("1"));

                subscriberPublic.receive().handle((v, err)->receive(v, err, subscriberPublic));
                subscriberPrivate.receive().handle((v, err)->receive(v, err, subscriberPrivate));

                publisherPrivate = session.createPublisher("private-messages");

                }
            catch (Exception e)
                {
                throw new RuntimeException("Failed to start RunTopics", e);
                }
            }

        @Override
        public void run()
            {
            Random random = new Random();

            while (true)
                {
                publisherPublic.publish(UUID.randomUUID().toString()).join();
                System.out.println("Publish");
                Base.sleep((long) random.nextInt(1000));
                }
            }
        }
    }
