package com.github.mjjaniec.tokenfield;

import com.github.mjjaniec.tokenfield.client.TokenFieldServerRpc;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.ComboBox;

public abstract class TokenComboBox<M> extends ComboBox<M> {

    protected TokenField.InsertPosition insertPosition;

    private TokenFieldServerRpc rpc = new TokenFieldServerRpc() {
        public void deleteToken() {
            onDelete();
        }
    };

    public TokenComboBox(TokenField.InsertPosition insertPosition) {
        this.insertPosition = insertPosition;
        registerRpc(rpc);
    }

//    @Override
//    public void paintContent(PaintTarget target) throws PaintException {
//        super.paintContent(target);
//        target.addVariable(this, "del", false);
//        if (insertPosition == TokenField.InsertPosition.AFTER) {
//            target.addAttribute("after", true);
//        }
//    }

    public void setTokenInsertPosition(TokenField.InsertPosition insertPosition) {
        this.insertPosition = insertPosition;
        requestRepaint();
    }

    abstract protected void onDelete();

}
