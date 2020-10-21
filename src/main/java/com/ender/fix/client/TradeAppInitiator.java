package com.ender.fix.client;

import com.ender.fix.IdGenerator;

import lombok.extern.slf4j.Slf4j;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.AggregatedBook;
import quickfix.field.Currency;
import quickfix.field.DeleteReason;
import quickfix.field.ExpireDate;
import quickfix.field.ExpireTime;
import quickfix.field.MDEntryID;
import quickfix.field.MDEntryOriginator;
import quickfix.field.MDEntryPositionNo;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateAction;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.NoMDEntries;
import quickfix.field.NumberOfOrders;
import quickfix.field.Product;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.Heartbeat;
import quickfix.fix44.Logon;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.TestRequest;

@Slf4j
public class TradeAppInitiator extends MessageCracker implements Application {
    private final MessageStoreFactory messageStoreFactory;
    private final MessageFactory      messageFactory;
    private final LogFactory          logFactory;
    private final SessionSettings     executorSettings;

    public TradeAppInitiator(SessionSettings executorSettings, MessageStoreFactory messageStoreFactory, MessageFactory messageFactory, LogFactory logFactory) {

        this.executorSettings = executorSettings;
        this.messageStoreFactory = messageStoreFactory;
        this.messageFactory = messageFactory;
        this.logFactory = logFactory;
    }

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("CLIENT onCreate: " + sessionID);
        Session.lookupSession(sessionID).logon();
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("CLIENT onLogon: " + sessionID);

        MarketDataRequest marketDataRequest = getMarketDataRequest();
        MarketDataRequest marketDataRequest2 = getMarketDataRequest2();
        //Send message
        try {
            Session.sendToTarget(marketDataRequest, sessionID);
            Session.sendToTarget(marketDataRequest2, sessionID);
        } catch (SessionNotFound sessionNotFound) {
            sessionNotFound.printStackTrace();
        }
    }

    private MarketDataRequest getMarketDataRequest() {
        MarketDataRequest marketDataRequest = new MarketDataRequest();
        marketDataRequest.set(new MDReqID(IdGenerator.genOrderID()));
        marketDataRequest.set(new SubscriptionRequestType('1'));
        //if market depth require
        marketDataRequest.set(new MarketDepth(1));
        marketDataRequest.set(new MDUpdateType(1));
        marketDataRequest.set(new AggregatedBook(true));
        MarketDataRequest.NoMDEntryTypes noMDEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        MDEntryType mdEntryType_bid = new MDEntryType('0');
        noMDEntryTypes.set(mdEntryType_bid);
        marketDataRequest.addGroup(noMDEntryTypes);
        MDEntryType mdEntryType_offer = new MDEntryType('1');
        noMDEntryTypes.set(mdEntryType_offer);
        marketDataRequest.addGroup(noMDEntryTypes);

        MarketDataRequest.NoRelatedSym usdTry = new MarketDataRequest.NoRelatedSym();
        usdTry.set(new Symbol("USDTRY"));
        usdTry.set(new Product(4));
        marketDataRequest.addGroup(usdTry);


        return marketDataRequest;
    }

    private MarketDataRequest getMarketDataRequest2() {
        MarketDataRequest marketDataRequest = new MarketDataRequest();
        marketDataRequest.set(new MDReqID(IdGenerator.genOrderID()));
        marketDataRequest.set(new SubscriptionRequestType('1'));
        //if market depth require
        marketDataRequest.set(new MarketDepth(1));
        marketDataRequest.set(new MDUpdateType(1));
        marketDataRequest.set(new AggregatedBook(true));
        MarketDataRequest.NoMDEntryTypes noMDEntryTypes = new MarketDataRequest.NoMDEntryTypes();
        MDEntryType mdEntryType_bid = new MDEntryType('0');
        noMDEntryTypes.set(mdEntryType_bid);
        marketDataRequest.addGroup(noMDEntryTypes);
        MDEntryType mdEntryType_offer = new MDEntryType('1');
        noMDEntryTypes.set(mdEntryType_offer);
        marketDataRequest.addGroup(noMDEntryTypes);
        MarketDataRequest.NoRelatedSym relatedSymbol = new MarketDataRequest.NoRelatedSym();
        relatedSymbol.set(new Symbol("USDTRY"));
        marketDataRequest.addGroup(relatedSymbol);
        return marketDataRequest;
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("CLIENT onLogout: " + sessionID);

        Session.lookupSession(sessionID).logon();
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        log.info("CLIENT toAdmin: " + message.getClass().getName() + " - " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound {
        log.info("CLIENT fromAdmin: " + message.getClass().getName() + " - " + message);
        if (message instanceof TestRequest) {
            TestRequest testRequest = (TestRequest) message;
            Heartbeat heartbeat = new Heartbeat();
            heartbeat.set(testRequest.getTestReqID());
            try {
                Session.sendToTarget(heartbeat, sessionID);
            } catch (SessionNotFound sessionNotFound) {
                sessionNotFound.printStackTrace();
            }
        }
        else if(message instanceof Logon)
        {
            Logon logon = (Logon) message;
            try {
                Session.sendToTarget(logon,sessionID);
            } catch (SessionNotFound sessionNotFound) {
                sessionNotFound.printStackTrace();
            }
        }
    }

    @Override
    public void toApp(Message message, SessionID sessionID) {
        log.info("CLIENT toApp: " + message.getClass().getName() + " - " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        log.info("CLIENT fromApp: " + message.getClass().getName() + " - " + message);
        crack(message, sessionID);
    }

    @Handler
    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) {
        log.info(message.toString());
    }
    @Handler
    public void onMessage(BusinessMessageReject message, SessionID sessionID) {
        log.info(message.toString());
    }

    @Handler
    public void onMessage(MarketDataIncrementalRefresh message, SessionID sessionID) throws FieldNotFound {

        MDReqID mdreqid = new MDReqID();
        NoMDEntries nomdentries = new NoMDEntries();
        MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
        MDUpdateAction mdupdateaction = new MDUpdateAction();
        DeleteReason deletereason = new DeleteReason();
        MDEntryType mdentrytype = new MDEntryType();
        MDEntryID mdentryid = new MDEntryID();
        Symbol symbol = new Symbol();
        MDEntryOriginator mdentryoriginator = new MDEntryOriginator();
        MDEntryPx mdentrypx = new MDEntryPx();
        Currency currency = new Currency();
        MDEntrySize mdentrysize = new MDEntrySize();
        ExpireDate expiredate = new ExpireDate();
        ExpireTime expiretime = new ExpireTime();
        NumberOfOrders numberoforders = new NumberOfOrders();
        MDEntryPositionNo mdentrypositionno = new MDEntryPositionNo();

        message.get(nomdentries);

        message.getGroup(1, group);

        int list = nomdentries.getValue();

        for (int i = 0; i < list; i++) {

            message.getGroup(i + 1, group);
            group.get(mdupdateaction);
            if (mdupdateaction.getValue() == '2')
                System.out.println("Enter");
            group.get(deletereason);
            group.get(mdentrytype);
            group.get(mdentryid);
            group.get(symbol);
            group.get(mdentryoriginator);
            if (mdupdateaction.getValue() == '0')
                group.get(mdentrypx);
            group.get(currency);
            if (mdupdateaction.getValue() == '0')
                group.get(mdentrysize);
        }

        log.info("Got Symbol {0} Price {1}", symbol.getValue(), mdentrypx.getValue());
    }


}
