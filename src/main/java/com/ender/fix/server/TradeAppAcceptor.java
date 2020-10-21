package com.ender.fix.server;

import lombok.extern.slf4j.Slf4j;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.UnsupportedMessageType;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.NoRelatedSym;
import quickfix.field.SenderCompID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.fix42.MarketDataSnapshotFullRefresh;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequest;

@Slf4j
public class TradeAppAcceptor extends MessageCracker implements Application {
    @Override
    public void onCreate(SessionID sessionID) {
        log.info("SERVER onCreate: " + sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("SERVER onLogon: " + sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("SERVER onLogout: " + sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        log.info("SERVER toAdmin: " + message.getClass().getName() + " - " + message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound {
        log.info("SERVER fromAdmin: " + message.getClass().getName() + " - " + message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) {
        log.info("SERVER toApp: " + message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectTagValue, UnsupportedMessageType {
        log.info("SERVER fromApp: " + message.getClass().getName() + " - " + message);
        crack(message, sessionID);
    }

    @Handler
    public void onMessage(MarketDataRequest message, SessionID sessionID) throws SessionNotFound, FieldNotFound {
        System.out.println(message);
        Session.sendToTarget(new MarketDataIncrementalRefresh(), sessionID);

        MarketDataRequest.NoRelatedSym noRelatedSyms = new MarketDataRequest.NoRelatedSym();

        //String mdReqId = message.getString(MDReqID.FIELD);
        char subscriptionRequestType = message.getChar(SubscriptionRequestType.FIELD);

        //int marketDepth = message.getInt(MarketDepth.FIELD);
        int relatedSymbolCount = message.getInt(NoRelatedSym.FIELD);

        MarketDataSnapshotFullRefresh fixMD = new MarketDataSnapshotFullRefresh();
        fixMD.setString(MDReqID.FIELD, message.getString(MDReqID.FIELD));

        for (int i = 1; i <= relatedSymbolCount; ++i) {
            message.getGroup(i, noRelatedSyms);
            String symbol = noRelatedSyms.getString(Symbol.FIELD);
            fixMD.setString(Symbol.FIELD, symbol);
        }

        MarketDataSnapshotFullRefresh.NoMDEntries noMDEntries = new MarketDataSnapshotFullRefresh.NoMDEntries();
        noMDEntries.setChar(MDEntryType.FIELD, '0');
        noMDEntries.setDouble(MDEntryPx.FIELD, 123.45);
        fixMD.addGroup(noMDEntries);
        String senderCompId = message.getHeader().getString(SenderCompID.FIELD);
        String targetCompId = message.getHeader().getString(TargetCompID.FIELD);
        fixMD.getHeader().setString(SenderCompID.FIELD, targetCompId);
        fixMD.getHeader().setString(TargetCompID.FIELD, senderCompId);
        try {
            Session.sendToTarget(fixMD, targetCompId, senderCompId);
        } catch (SessionNotFound e) {
        }


    }
}
