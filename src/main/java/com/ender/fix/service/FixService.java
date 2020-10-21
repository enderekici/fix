package com.ender.fix.service;

import com.ender.fix.server.TradeAppAcceptor;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.NoopStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

@Service
public class FixService {
    @PostConstruct
    public void init() {
        try {
            MessageStoreFactory messageStoreFactory;
            LogFactory logFactory;
            MessageFactory messageFactory;
            SessionSettings executorSettings = new SessionSettings("./acceptorSettings.txt");
            Application application = new TradeAppAcceptor();
            //messageStoreFactory = new FileStoreFactory(executorSettings);
            messageStoreFactory = new NoopStoreFactory();
            //MessageFactory messageFactory = new DefaultMessageFactory("FIX.4.4");
            messageFactory = new quickfix.fix44.MessageFactory();
            //  logFactory = new FileLogFactory(executorSettings);
            logFactory = new ScreenLogFactory(executorSettings);
            SocketAcceptor socketAcceptor = new SocketAcceptor(application, messageStoreFactory, executorSettings, logFactory, messageFactory);
            socketAcceptor.start();
        } catch (ConfigError configError) {
            configError.printStackTrace();
        }
    }
}
