package com.github.mjjaniec.tokenfield.client;

import com.vaadin.shared.communication.ServerRpc;

public interface TokenFieldServerRpc extends ServerRpc {

    void deleteToken();
}
