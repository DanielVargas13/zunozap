package me.isaiah.zunozap;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import javafx.scene.Scene;
import me.isaiah.zunozap.UniversalEngine.Engine;

public class Settings {
    private static ZFile settings = new ZFile("settings.txt", false);
    private static ZunoProperties p = new ZunoProperties();
    private static int i = 1;
    private static JButton odf = new JButton("Open data folder");
    public static JFrame f;
    public static JPanel panel;
    protected static Scene s;
    public static HashMap<String, File> b = new HashMap<>();

    public static void set(File folder, Scene sc) {
        s = sc;
    }

    public enum Options {
        forceHTTPS("Force HTTPS", false), blockEventCalls("Block plugin events", false), createPluginDataFolders("Create plugin folders", true),
        onTheDuckSide("Use DuckDuckGO", true), offlineStorage("Store web pages for offline browsing", false), javascript(true),
        blockMalware("Block Malware sites", true);

        private final static HashMap<Integer, Options> map = new HashMap<>();
        public boolean b, def;
        public String n;

        private Options(boolean d) { this.b = d; this.def = d; this.n = toString(); }
        private Options(String n, boolean d) { this.b = d; this.def = d; this.n = n; }

        public static Options getById(int id){ return map.get(id - 1); }

        static { for (Options m : values()) map.put(m.ordinal(), m); }
    }

    public static final boolean initMenu() {
        try {
            settings.createNewFile();
            ZunoProperties p = new ZunoProperties();
            FileInputStream s = new FileInputStream(settings);
            p.load(s);
            addDefaults();

            for (Options e : Options.values()) e.b = p.get(e.toString());

            ZunoAPI.styleName = String.valueOf(p.getStr("style"));
            ZunoAPI.stylesheet = new File(String.valueOf(p.getStr("stylefile")));
            try {
                ZunoAPI.en = Engine.valueOf(p.getStr("engine"));
            } catch (Exception e) {
                e.printStackTrace();
                ZunoAPI.en = Engine.CHROME;
            }

            s.close();
            return true;
        } catch (IOException e) { return false; }
    }

    public static void initCss(File fold) {
        ZunoAPI.exportResource("style.css", ZunoAPI.home);
        ZFile f = new ZFile("style.css", false);
        b.put("ZunoZap default", f);
        if (ZunoAPI.styleName.equalsIgnoreCase("none") || ZunoZapWebView.firstRun || ZunoZap.firstRun) {
            ZunoAPI.stylesheet = f;
            ZunoAPI.styleName = "ZunoZap default";
        } else Settings.initMenu();

        for (File fi : fold.listFiles()) b.put(fi.getName(), fi);

        ZFile temp = new ZFile("blank.css", false);
        temp.deleteOnExit();
        b.put("Java", temp);
    }

    public static void setStyle(String name) {
        try {
            s.getStylesheets().setAll(b.get(name).toURI().toURL().toExternalForm());
        } catch (MalformedURLException e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    public final static void createMenu() throws IOException {
        f = new JFrame("ZunoZap Settings");
        panel = new JPanel();

        settings.createNewFile();

        FileInputStream s = new FileInputStream(settings);
        p.load(s);

        addDefaults();

        for (Options e : Options.values()) e.b = p.get(e.toString());

        p.store(new FileOutputStream(settings), "conf");

        i = 1; // Reset
        for (Options e : Options.values()) addCheckBox(e.n, e.b);

        odf.setEnabled(Desktop.isDesktopSupported());
        odf.addActionListener((a) -> { try { Desktop.getDesktop().open(ZunoAPI.home); } catch (IOException e) {}});

        JTextField t = new JTextField("Style:");
        t.setEditable(false);
        panel.setBorder(new EmptyBorder(2, 10, 2, 2));
        t.setBorder(new EmptyBorder(0, 0, 0, 0));
        t.setMargin(new Insets(20, 0, 0, 0));
        t.setMaximumSize(new Dimension(50, 25));

        JTextField e = new JTextField("Engine:");
        e.setEditable(false);
        panel.setBorder(new EmptyBorder(2, 10, 2, 2));
        e.setBorder(new EmptyBorder(0, 0, 0, 0));
        e.setMargin(new Insets(20, 0, 0, 0));
        e.setMaximumSize(new Dimension(50, 25));

        JComboBox<Object> style = new JComboBox<>(b.keySet().toArray());
        style.setSelectedItem(ZunoAPI.styleName);
        style.setMaximumSize(new Dimension(150, 20));
        style.addActionListener((a) -> {
            String name = (String) ((JComboBox<String>) a.getSource()).getSelectedItem();
            ZunoAPI.stylesheet = b.get(name);
            ZunoAPI.styleName = name;
            setStyle(name);
            save(true);
        });

        JComboBox<Object> en = new JComboBox<>(UniversalEngine.Engine.values());
        en.setSelectedItem(ZunoAPI.en);
        en.setMaximumSize(new Dimension(150, 20));
        en.addActionListener((a) -> {
            ZunoAPI.en = (Engine) ((JComboBox<String>) a.getSource()).getSelectedItem(); 
            save(true);
        });

        JTextField p = new JTextField();
        p.setEditable(false);
        p.setBorder(new EmptyBorder(0, 0, 0, 0));
        p.setMaximumSize(new Dimension(10, 20));

        Component[] cs = {t, style, e, en, p, odf};
        for (Component c : cs) panel.add(c);

        s.close();
        f.setDefaultCloseOperation(2);
        panel.setSize(5500, 2500);

        f.setPreferredSize(new Dimension(400, 300));
        f.setContentPane(panel);
        f.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent w) { save(true); }
        });
        f.pack();
        f.setVisible(true);
    }

    private static void addCheckBox(String text, boolean b) {
        final int it = i;
        JCheckBox box = new JCheckBox(text);
        box.setSelected(b);
        box.setName(String.valueOf(i));
        box.addActionListener((a) -> {
            Options.getById(it).b = box.isSelected();
            save(true);
        });
        panel.add(box);
        i++;
    }

    public static boolean save(boolean all) {
        try {
            settings.createNewFile();
            ZunoProperties p = new ZunoProperties();
            FileInputStream s = new FileInputStream(settings);
            p.load(s);

            for (Options e : Options.values()) p.set(e.toString(), e.b);

            if (all) {
                p.setProperty("style", ZunoAPI.styleName);
                p.setProperty("stylefile", ZunoAPI.stylesheet.getAbsolutePath());
                p.setProperty("engine", ZunoAPI.en.name());
            }
            p.store(new FileOutputStream(settings), null);
            s.close();
            return true;
        } catch (IOException e) { return false; }
    }

    private static void addDefaults() {
        for (Options e : Options.values()) if (!p.containsKey(e.name())) p.set(e.name(), e.def);
    }
}