package com.ender.fix.client;

import java.util.concurrent.CountDownLatch;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

public class Client {
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws ConfigError, InterruptedException {
        SessionSettings executorSettings = new SessionSettings("./initiatorSettings.txt");
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(executorSettings);//new NoopStoreFactory();
        MessageFactory messageFactory = new quickfix.fix44.MessageFactory();//new DefaultMessageFactory("FIX.4.4");
        LogFactory logFactory = new FileLogFactory(executorSettings);//new ScreenLogFactory(executorSettings);
        Application application = new TradeAppInitiator(executorSettings, messageStoreFactory, messageFactory, logFactory);
        SocketInitiator socketInitiator = new SocketInitiator(application, messageStoreFactory, executorSettings, logFactory, messageFactory);
        socketInitiator.start();
        SessionID sessionId = socketInitiator.getSessions().get(0);
        Session.lookupSession(sessionId).logon();
        shutdownLatch.await();
    }

    private static void stop() {
        shutdownLatch.countDown();
    }
}
