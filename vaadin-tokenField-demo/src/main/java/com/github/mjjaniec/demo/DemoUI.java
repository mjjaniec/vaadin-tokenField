package com.github.mjjaniec.demo;


import javax.servlet.annotation.WebServlet;

import com.github.mjjaniec.tokenfield.TokenField;
import com.google.common.collect.Lists;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import java.util.*;

@Theme("demo")
@Title("MyComponent Add-on Demo")
@SuppressWarnings("serial")
public class DemoUI extends UI
{

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }


    static class Content extends VerticalLayout {

        Content() {
            // Just add some spacing so it looks nicer
            setSpacing(true);
            setMargin(true);

            {
                /*
                 * This is the most basic use case using all defaults; it's
                 * empty to begin with, the user can enter new tokens.
                 */

                Panel p = new Panel("Basic");
                VerticalLayout l = new VerticalLayout();
                l.setMargin(true);
                p.setContent(l);
                addComponent(p);

                TokenField f = new TokenField("Add tags");
                l.addComponent(f);

            }

            {
                /*
                 * Interpretes "," as token separator
                 */

                Panel p = new Panel("Comma separated");
                VerticalLayout l = new VerticalLayout();
                l.setMargin(true);
                p.setContent(l);
                addComponent(p);

                TokenField f = new TokenField() {

                    @Override
                    protected void onTokenInput(String tokenId) {
                        String[] tokens = ((String) tokenId).split(",");
                        for (String token : tokens) {
                            token = token.trim();
                            if (token.length() > 0) {
                                super.onTokenInput(token);
                            }
                        }
                    }

                    @Override
                    protected void rememberToken(String tokenId) {
                        String[] tokens = ((String) tokenId).split(",");
                        for (String token : tokens) {
                            token = token.trim();
                            if (token.length() > 0) {
                                super.rememberToken(token);
                            }
                        }
                    }

                };
                f.setInputPrompt("tag, another, yetanother");
                l.addComponent(f);

            }

            {
                /*
                 * In this example, most features are exercised. A container
                 * with generated contacts is used. The input has filtering
                 * (a.k.a suggestions) enabled, and the added token button is
                 * configured so that it is in the standard "Name <email>"
                 * -format. New contacts can be added to the container ('address
                 * book'), or added as-is (in which case it's styled
                 * differently).
                 */

                Panel p = new Panel("Full featured example");
                VerticalLayout l = new VerticalLayout();
                l.setMargin(true);
                p.setContent(l);
                l.setStyleName("black");
                addComponent(p);

                // generate container
                ListDataProvider<String> tokens = generateTestContainer();

                // we want this to be vertical
                VerticalLayout lo = new VerticalLayout();
                lo.setSpacing(true);

                final TokenField f = new TokenField(lo) {

                    private static final long serialVersionUID = 5530375996928514871L;

                    // dialog if not in 'address book', otherwise just add
                    protected void onTokenInput(String tokenId) {
                        Set<String> set = (Set<String>) getValue();
                        Contact c = new Contact("", tokenId.toString());
                        if (set != null && set.contains(c)) {
                            // duplicate
                            Notification.show(getTokenCaption(tokenId)
                                    + " is already added");
                        } else {
                            if (!cb.getValue().contains(c.email)) {
                                // don't add directly,
                                // show custom "add to address book" dialog
                                getUI().addWindow(
                                        new EditContactWindow(tokenId
                                                .toString(), this));

                            } else {
                                // it's in the 'address book', just add
                                addToken(tokenId);
                            }
                        }
                    }

                    // show confirm dialog
                    protected void onTokenClick(final String tokenId) {
                        getUI().addWindow(
                                new RemoveWindow((Contact)(Object) tokenId, this));
                    }

                    // just delete, no confirm
                    protected void onTokenDelete(String tokenId) {
                        this.removeToken(tokenId);
                    }

                    // custom caption + style if not in 'address book'
                    protected void configureTokenButton(String tokenId,
                                                        Button button) {
                        super.configureTokenButton(tokenId, button);
                        // custom caption
                        button.setCaption(getTokenCaption(tokenId) + " <"
                                + tokenId + ">");
                        // width
                        button.setWidth("100%");

//                        if (!cb.getValue().contains(tokenId)) {
//                            // it's not in the address book; style
//                            button.addStyleName(TokenField.STYLE_BUTTON_EMPHAZISED);
//                        }
                    }
                };
                l.addComponent(f);
                // This would turn on the "fake tekstfield" look:
                f.setStyleName(TokenField.STYLE_TOKENFIELD);
                f.setWidth("100%");
                f.setInputWidth("100%");
                f.setDataProvider(tokens); // 'address book'
//                f.setFilteringMode(FilteringMode.CONTAINS); // suggest
//                f.setTokenCaptionPropertyId("name"); // use name in input
                f.setInputPrompt("Enter contact name or new email address");
                f.setRememberNewTokens(false); // we'll do this via the dialog
                // Pre-add a few:
//                Iterator<String> it = f.getTokenIds().iterator();
//                f.addToken(it.next());
//                f.addToken(it.next());
                f.addToken("thatnewguy@example.com");

            }

            {
                /*
                 * This example uses to selects to dynamically change the insert
                 * position and the layout used.
                 */

                final Panel p = new Panel("Layout and InsertPosition");
                final VerticalLayout l = new VerticalLayout();
                l.setMargin(true);
                p.setContent(l);
                l.setSpacing(true);
                addComponent(p);

                HorizontalLayout controls = new HorizontalLayout();
                l.addComponent(controls);

                // generate container
                ListDataProvider<String> tokens = generateTestContainer();

                // w/ datasource, no configurator
                final TokenField f = new TokenField();
                /*
                 * f.setContainerDataSource(tokens); //
                 * f.setNewTokensAllowed(false);
                 * f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
                 * f.setInputPrompt("firstname.lastname@example.com"); -
                 */
                l.addComponent(f);

                final NativeSelect<Class<?>> lo = new NativeSelect<>("Layout");
                lo.setDataProvider(new ListDataProvider<>(Lists.newArrayList(HorizontalLayout.class, VerticalLayout.class, GridLayout.class, CssLayout.class)));
                lo.setEmptySelectionAllowed(false);
                lo.setValue(f.getLayout().getClass());
                lo.addValueChangeListener(new HasValue.ValueChangeListener<Class<?>>() {

                    private TokenField curr = f;

                    @Override
                    public void valueChange(HasValue.ValueChangeEvent<Class<?>> event) {
                        try {
                            Layout l = (Layout) (event.getValue().newInstance());
                            if (l instanceof GridLayout) {
                                ((GridLayout) l).setColumns(3);
                            }
                            l.removeComponent(curr);
                            curr = new TokenField(l);
                            l.addComponent(curr);
                        } catch (Exception e) {
                            Notification.show("Ouch!",
                                    "Could not make a " + lo.getValue(),
                                    Notification.Type.ERROR_MESSAGE);
                            lo.setValue(f.getLayout().getClass());
                            e.printStackTrace();
                        }
                    }
                });

                controls.addComponent(lo);


                final NativeSelect<TokenField.InsertPosition> ip = new NativeSelect<>("InsertPosition");
                ip.setDataProvider(new ListDataProvider<>(Lists.newArrayList(TokenField.InsertPosition.AFTER, TokenField.InsertPosition.BEFORE)));
                ip.setEmptySelectionAllowed(false);
                ip.setValue(f.getTokenInsertPosition());
                ip.addValueChangeListener(event -> f.setTokenInsertPosition(ip.getValue()));
                controls.addComponent(ip);

                final CheckBox cb = new CheckBox("Read-only");
                cb.setValue(f.isReadOnly());
                cb.addValueChangeListener(event -> f.setReadOnly(cb.getValue()));
                controls.addComponent(cb);
                controls.setComponentAlignment(cb, Alignment.BOTTOM_LEFT);

            }

            {
                Panel p = new Panel("Data binding and buffering");
                addComponent(p);

                // just for layout; ListSelect left, TokenField right
                HorizontalLayout lo = new HorizontalLayout();
                lo.setWidth("100%");
                lo.setSpacing(true);
                lo.setMargin(true);
                p.setContent(lo);

                // A regular list select
                ListSelect<String> list = new ListSelect<>(
                        "ListSelect, datasource for TokenField");
                list.setWidth("220px");
                lo.addComponent(list);
                ListDataProvider<String> ldp = new ListDataProvider<>(Lists.newArrayList("One", "Two", "Three", "Four", "Five"));
                // Add a few items
                list.setDataProvider(ldp);

                // TokenField bound to the ListSelect above, CssLayout so that
                // it wraps nicely.
                final TokenField f = new TokenField(
                        "TokenField, buffered, click << to commit");
                f.setDataProvider(ldp);
                // f.setNewTokensAllowed(false);
//                f.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
//                f.setPropertyDataSource(list);

//                lo.addComponent(new Button("<<", new Button.ClickListener() {
//
//                    private static final long serialVersionUID = 1375470313147460732L;
//
//                    public void buttonClick(ClickEvent event) {
//                        f.commit();
//                    }
//                }));

                lo.addComponent(f);
                lo.setExpandRatio(f, 1.0f);

            }
        }
    }

    /**
     * This is the window used to add new contacts to the 'address book'. It
     * does not do proper validation - you can add weird stuff.
     */
    public static class EditContactWindow extends Window {
        private Contact contact;

        EditContactWindow(final String t, final TokenField f) {
            super("New Contact");
            VerticalLayout l = new VerticalLayout();
            setContent(l);
            if (t.contains("@")) {
                contact = new Contact("", t);
            } else {
                contact = new Contact(t, "");
            }
            setModal(true);
            center();
            setWidth("250px");
            setStyleName("black");
            setResizable(false);

            // Just bind a Form to the Contact -pojo via BeanItem
            FormLayout form = new FormLayout();
            ////form.setItemDataSource(new BeanItem<Contact>(contact));
            //form.setImmediate(true);
            l.addComponent(form);

            // layout buttons horizontally
            HorizontalLayout hz = new HorizontalLayout();
            l.addComponent(hz);
            hz.setSpacing(true);
            hz.setWidth("100%");

            Button dont = new Button("Don't add", new Button.ClickListener() {

                private static final long serialVersionUID = -1198191849568844582L;

                public void buttonClick(Button.ClickEvent event) {
                    if (contact.getEmail() == null
                            || contact.getEmail().length() < 1) {
                        contact.setEmail(contact.getName());
                    }
                    //  f.addToken(contact);
                    f.getUI().removeWindow(EditContactWindow.this);
                }
            });
            hz.addComponent(dont);
            hz.setComponentAlignment(dont, Alignment.MIDDLE_LEFT);

            Button add = new Button("Add to contacts",
                    new Button.ClickListener() {

                        private static final long serialVersionUID = 1L;

                        public void buttonClick(Button.ClickEvent event) {
                            if (contact.getEmail() == null
                                    || contact.getEmail().length() < 1) {
                                contact.setEmail(contact.getName());
                            }
                            //    ((BeanItemContainer) f.getContainerDataSource())
                            //          .addBean(contact);
                            //f.addToken(contact);
                            f.getUI().removeWindow(EditContactWindow.this);
                        }
                    });
            hz.addComponent(add);
            hz.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);

        }
    }

    /* Used to generate example contents */
    private static final String[] firstnames = new String[] { "John", "Mary",
            "Joe", "Sarah", "Jeff", "Jane", "Peter", "Marc", "Robert", "Paula",
            "Lenny", "Kenny", "Nathan", "Nicole", "Laura", "Jos", "Josie",
            "Linus" };
    private static final String[] lastnames = new String[] { "Torvalds",
            "Smith", "Adams", "Black", "Wilson", "Richards", "Thompson",
            "McGoff", "Halas", "Jones", "Beck", "Sheridan", "Picard", "Hill",
            "Fielding", "Einstein" };

    private static ListDataProvider<String> generateTestContainer() {
        List<String> list = new ArrayList<>();
        ListDataProvider<String> provider = new ListDataProvider<>(list);

        HashSet<String> log = new HashSet<>();
        Random r = new Random(5);
        for (int i = 0; i < 20;) {
            String fn = firstnames[(int) (r.nextDouble() * firstnames.length)];
            String ln = lastnames[(int) (r.nextDouble() * lastnames.length)];
            String name = fn + " " + ln;
            String email = fn.toLowerCase() + "." + ln.toLowerCase()
                    + "@example.com";

            if (!log.contains(email)) {
                log.add(email);
                list.add(email);
                i++;
            }

        }
        return provider;
    }

    /**
     * Example Contact -bean, mostly generated setters/getters.
     */
    public static class Contact {
        private String name;
        private String email;

        public Contact(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String toString() {
            return email;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Contact) {
                return email.equals(((Contact) obj).getEmail());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return email.hashCode();
        }

    }

    /**
     * This is the window used to confirm removal
     */
    public static class RemoveWindow extends Window {

        private static final long serialVersionUID = -7140907025722511460L;

        RemoveWindow(final Contact c, final TokenField f) {
            super("Remove " + c.getName() + "?");

            VerticalLayout l = new VerticalLayout();
            setContent(l);

            setStyleName("black");
            setResizable(false);
            center();
            setModal(true);
            setWidth("250px");
            setClosable(false);

            // layout buttons horizontally
            HorizontalLayout hz = new HorizontalLayout();
            l.addComponent(hz);
            hz.setSpacing(true);
            hz.setWidth("100%");

            Button cancel = new Button("Cancel", new Button.ClickListener() {

                private static final long serialVersionUID = 7675170261217815011L;

                public void buttonClick(Button.ClickEvent event) {
                    f.getUI().removeWindow(RemoveWindow.this);
                }
            });
            hz.addComponent(cancel);
            hz.setComponentAlignment(cancel, Alignment.MIDDLE_LEFT);

            Button remove = new Button("Remove", new Button.ClickListener() {


                public void buttonClick(Button.ClickEvent event) {
                 //   f.removeToken(c.email);
                    f.getUI().removeWindow(RemoveWindow.this);
                }
            });
            hz.addComponent(remove);
            hz.setComponentAlignment(remove, Alignment.MIDDLE_RIGHT);

        }
    }

    @Override
    protected void init(VaadinRequest request) {

        setContent(new Content());
    }
}
