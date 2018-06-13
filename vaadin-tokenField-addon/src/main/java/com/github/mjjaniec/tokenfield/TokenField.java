package com.github.mjjaniec.tokenfield;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.*;


public class TokenField extends CustomField<Set<String>> {


    public enum InsertPosition {
        /**
         * Tokens will be added after the input
         */
        AFTER,
        /**
         * Add tokens before the input
         */
        BEFORE
    }

    public static final String STYLE_TOKENFIELD = "tokenfield";
    public static final String STYLE_TOKENTEXTFIELD = "tokentextfield";


    /**
     * The layout currently in use
     */
    protected Layout layout;

    /**
     * Current insert position
     */
    protected InsertPosition insertPosition = InsertPosition.BEFORE;

    private ListDataProvider<String> dataProvider = new ListDataProvider<>(new ArrayList<>());

    /**
     * The ComboBox used for input - should probably not be touched.
     */
    protected TokenComboBox<String> cb = new TokenComboBox<String>(insertPosition) {

        protected void onDelete() {
            if (!buttons.isEmpty()) {
                List<String> keys = new ArrayList<>(buttons.keySet());
                onTokenDelete(keys.get(keys.size() - 1));
                cb.focus();
            }
        }

        {
            setDataProvider(dataProvider);
        }
    };

    /**
     * Maps the tokenId (itemId) to the token button
     */
    protected LinkedHashMap<String, Button> buttons = new LinkedHashMap<>();

    protected boolean rememberNewTokens = true;

    /**
     * Create a new TokenField with a caption and a {@link InsertPosition}.
     *
     * @param caption        the desired caption
     * @param insertPosition the desired insert position
     */
    public TokenField(String caption, InsertPosition insertPosition) {
        this();
        this.insertPosition = insertPosition;
        setCaption(caption);
    }

    /**
     * Create a new TokenField with a caption.
     *
     * @param caption the desired caption
     */
    public TokenField(String caption) {
        this();
        setCaption(caption);
    }

    /**
     * Create a new TokenField.
     */
    public TokenField() {
        this(new CssLayout());
    }

    /**
     * Create a new TokenField with a caption and a given layout.
     *
     * @param caption the desired caption
     * @param lo      the desired layout
     */
    public TokenField(String caption, Layout lo) {
        this(lo);
        setCaption(caption);
    }

    /**
     * Create a new TokenField with a caption, a given layout, and the specified
     * token insert position.
     *
     * @param caption        the desired caption
     * @param lo             the desired layout
     * @param insertPosition the desired token insert position
     */
    public TokenField(String caption, Layout lo, InsertPosition insertPosition) {
        this(lo);
        setCaption(caption);
        this.insertPosition = insertPosition;
    }

    /**
     * Create a new TokenField with the given layout, and the specified token
     * insert position.
     *
     * @param lo             the desired layout
     * @param insertPosition the desired token insert position
     */
    public TokenField(Layout lo, InsertPosition insertPosition) {
        this(lo);
        this.insertPosition = insertPosition;
    }

    /**
     * Create a new TokenField with the given layout.
     *
     * @param lo the desired layout
     */
    public TokenField(Layout lo) {
        setStyleName(STYLE_TOKENFIELD + " " + STYLE_TOKENTEXTFIELD);

        cb.setTextInputAllowed(true);
        cb.setEmptySelectionAllowed(false);
        cb.addValueChangeListener(event -> {
            String tokenId = event.getValue();
            if (tokenId != null) {
                onTokenInput(tokenId);
                cb.setValue(null);
                cb.focus();
            }
        });


        cb.setNewItemProvider((ComboBox.NewItemProvider<String>) tokenId -> {
            onTokenInput(tokenId);
            if (rememberNewTokens) {
                rememberToken(tokenId);
            }
            cb.focus();
            return Optional.ofNullable(tokenId);

        });

        setLayout(lo);

    }

    protected void rememberToken(String tokenId) {
        dataProvider.getItems().add(tokenId);
        dataProvider.refreshAll();
    }

    /*
     * Rebuilds from scratch
     */
    private void rebuild() {
        layout.removeAllComponents();
        if (!isReadOnly() && insertPosition == InsertPosition.AFTER) {
            layout.addComponent(cb);
        }
        for (Button b2 : buttons.values()) {
            layout.addComponent(b2);
        }
        if (!isReadOnly() && insertPosition == InsertPosition.BEFORE) {
            layout.addComponent(cb);
        }
        if (layout instanceof HorizontalLayout) {
            ((HorizontalLayout) layout).setExpandRatio(cb, 1.0f);
        }
    }

    /*
     * Might create a HashSet or two unnecessarily from time to time, but seems
     * clearer that way.
     *
     * @see org.vaadin.tokenfield.CustomField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void doSetValue(Set<String> newValue) {
        Set<String> old = getValue();

        Set<String> remove = new HashSet<>(old);
        Set<String> add = new HashSet<>(newValue);
        remove.removeAll(newValue);
        add.removeAll(old);

        for (String tokenId : remove) {
            removeTokenButton(tokenId);
        }
        for (String tokenId : add) {
            addTokenButton(tokenId);
        }
    }

    @Override
    public Set<String> getValue() {
        return buttons.keySet();
    }

    /**
     * Called when the user is adding a new token via the UI; called after the
     * newItemHandler. Can be used to make customize the adding process; e.g to
     * notify that the token was not added because it's duplicate, to ask for
     * additional information, or to disallow addition due to some heuristics
     * (not both A and Q).
     * The default is to call {@link #addToken(String)} which will add the token
     * if it's not a duplicate.
     *
     * @param tokenId the token id selected (or input)
     */
    protected void onTokenInput(String tokenId) {
        addToken(tokenId);
    }

    /**
     * Called when the token button is clicked, which by default removes the
     * token by calling {@link #removeToken(String)}. The behavior can be
     * customized, e.g present a confirmation dialog.
     *
     * @param tokenId the id of the token that was clicked
     */
    protected void onTokenClick(String tokenId) {
        removeToken(tokenId);
    }


    protected void onTokenDelete(String tokenId) {
        onTokenClick(tokenId);
    }

    private void addTokenButton(String val) {
        Button b = new Button();
        configureTokenButton(val, b);
        b.addListener(event -> onTokenClick(val));

        buttons.put(val, b);

        if (insertPosition == InsertPosition.BEFORE) {
            layout.replaceComponent(cb, b);
            layout.addComponent(cb);
        } else {
            layout.addComponent(b);
        }
        if (layout instanceof HorizontalLayout) {
            ((HorizontalLayout) layout).setExpandRatio(cb, 1.0f);
        }

    }

    /**
     * Adds a token if that token does not already exist.
     * <p>
     * Note that tokens are not automatically added to the token container. This
     * means you can add tokens without adding them to the container (that might
     * be bound to some data store), and without making them available to the
     * user in the suggestion dropdown.
     * This also means that when new tokens are disallowed (
     * {@link #setNewTokensAllowed(boolean)}) you can programmatically add
     * tokens that the user can not add him/herself.
     * Consider adding the token to the container before calling
     * {@link #addToken(String)} if you're using a custom captions based on
     * container/item properties, or if you want the token to be available to
     * the user as a suggestion later.
     * </p>
     *
     * @param tokenId the token to add
     */
    public void addToken(String tokenId) {
        Set<String> set = getValue();
        if (set == null) {
            set = new LinkedHashSet<>();
        }
        if (set.contains(tokenId)) {
            return;
        }
        HashSet<String> newSet = new LinkedHashSet<>(set);
        newSet.add(tokenId);
        setValue(newSet);
    }

    /**
     * Removes the given token.
     * <p>
     * Note that the token is not removed from the container, so if it exists in
     * the container, the token will still be available to the user.
     * </p>
     *
     * @param tokenId the token to remove
     */
    public void removeToken(String tokenId) {
        Set<String> set = getValue();
        LinkedHashSet<String> newSet = new LinkedHashSet<>(set);
        newSet.remove(tokenId);

        setValue(newSet);
    }

    private void removeTokenButton(String tokenId) {
        Button button = buttons.get(tokenId);
        layout.removeComponent(button);
        buttons.remove(tokenId);
    }

    /**
     * Configures the token button.
     * <p>
     * By default, the caption, icon, description, and style is set. Override to
     * customize.
     * Note that the default click-listener is added elsewhere and can not be
     * changed here.
     * </p>
     *
     * @param tokenId the token this button pertains to
     * @param button  the button to be configured
     */
    protected void configureTokenButton(String tokenId, Button button) {
        button.setCaption(getTokenCaption(tokenId) + " ×");
        button.setDescription("Click to remove");
        button.setStyleName(ValoTheme.BUTTON_LINK);
    }

    /**
     * Gets the layout currently in use.
     *
     * @return the current layout
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets layout used for laying out the tokens and the input.
     *
     * @param newLayout the layout to use
     */
    protected void setLayout(Layout newLayout) {
        if (layout != null) {
            layout.removeAllComponents();
        }
        layout = newLayout;
        rebuild();
    }

    /**
     * Gets the current token {@link InsertPosition}.
     * The token buttons are be placed at this position, relative to the input
     * box.
     *
     * @return the current token insert position
     * @see #setTokenInsertPosition(InsertPosition)
     * @see InsertPosition
     */
    public InsertPosition getTokenInsertPosition() {
        return insertPosition;
    }


    public void setTokenInsertPosition(InsertPosition insertPosition) {
        if (this.insertPosition != insertPosition) {
            this.insertPosition = insertPosition;
            cb.setTokenInsertPosition(insertPosition);
            rebuild();
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (readOnly == isReadOnly()) {
            return;
        }
//        for (Button b : buttons.values()) {
//            b.setReadOnly(readOnly);
//        }
        super.setReadOnly(readOnly);
        if (readOnly) {
            layout.removeComponent(cb);
        } else {
            rebuild();
        }
    }


    /**
     * Sets whether or not tokens entered by the user that not present in the
     * container are allowed. When true, the token is added, and if
     * {@link #setRememberNewTokens(boolean)} is true, the new token will be
     * added to the container as well.
     */
    public void setNewTokensAllowed(boolean allowNewTokens) {
        cb.setTextInputAllowed(allowNewTokens);
    }


    public boolean isNewTokensAllowed() {
        return cb.isTextInputAllowed();
    }

    /**
     * If true, new tokens entered by the user are automatically added to the
     * container.
     *
     * @return true if tokens are automatically added
     */
    public boolean isRememberNewTokens() {
        return rememberNewTokens;
    }

    /**
     * Provided new tokens are allowed ({@link #setNewTokensAllowed(boolean)}),
     * this sets whether or not new tokens entered by the user are automatically
     * added to the container.
     *
     * @param rememberNewTokens true to add new tokens automatically
     */
    public void setRememberNewTokens(boolean rememberNewTokens) {
        this.rememberNewTokens = rememberNewTokens;
    }

    public void setDataProvider(ListDataProvider<String> provider) {
        this.cb.setDataProvider(provider);
    }

//    /**
//     * Works as {@link ComboBox#setFilteringMode(int)}.
//     *
//     * @see ComboBox#setFilteringMode(int)
//     * @param filteringMode
//     *            the desired filtering mode
//     */
//    public void setFilteringMode(FilteringMode filteringMode) {
//        cb.setFilteringMode(filteringMode);
//    }
//
//    /**
//     * Works as {@link ComboBox#getFilteringMode()}.
//     *
//     * @see ComboBox#getFilteringMode()
//     * @param filteringMode
//     *            the desired filtering mode
//     */
//    public FilteringMode getFilteringMode() {
//        return cb.getDataProvider()();
//    }

    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.tokenfield.CustomField#focus()
     */
    public void focus() {
        cb.focus();
    }


    public String getInputPrompt() {
        return cb.getPlaceholder();
    }

    /**
     * Gets the caption for the given token; the caption can be based on a
     * property, just as in a ComboBox. Note that the string representation of
     * the tokenId itself is always used if the container does not contain the
     * id.
     *
     * @param tokenId the id of the token
     * @return the caption
     */
    public String getTokenCaption(String tokenId) {
        return tokenId;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.vaadin.tokenfield.CustomField#getTabIndex()
     */
    public int getTabIndex() {
        return cb.getTabIndex();
    }


    @Override
    public void setHeight(float height, Unit unit) {
        if (this.layout != null) {
            this.layout.setHeight(height, unit);
        }
        super.setHeight(height, unit);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        if (this.layout != null) {
            this.layout.setWidth(width, unit);
        }
        super.setWidth(width, unit);
    }

    @Override
    public void setSizeFull() {
        if (this.layout != null) {
            this.layout.setSizeFull();
        }
        super.setSizeFull();
    }

    @Override
    public void setSizeUndefined() {
        if (this.layout != null) {
            this.layout.setSizeUndefined();
        }
        super.setSizeUndefined();
    }

    public void setInputHeight(String height) {
        this.cb.setHeight(height);
    }

    public void setInputWidth(String width) {
        this.cb.setWidth(width);
    }

    public void setInputHeight(float height, Unit unit) {
        this.cb.setHeight(height, unit);
    }

    public void setInputWidth(float width, Unit unit) {
        this.cb.setWidth(width, unit);
    }

    public void setInputSizeFull() {
        this.cb.setSizeFull();
    }

    public void setInputSizeUndefined() {
        this.cb.setSizeUndefined();
    }

    public void setInputPrompt(String inputPrompt) {
        cb.setPlaceholder(inputPrompt);
    }

    public void setTabIndex(int tabIndex) {
        cb.setTabIndex(tabIndex);
    }

    public Collection<String> getTokenIds() {
        return dataProvider.getItems();
    }


    @Override
    protected Component initContent() {
        return layout;
    }

}
