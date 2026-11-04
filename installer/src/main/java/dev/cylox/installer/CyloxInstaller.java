package dev.cylox.installer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.*;

public final class CyloxInstaller {
    private static final String API = "https://api.github.com/repos/dap144p/Cylox-Clients/releases";
    private static final String FORGE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-installer.jar";
    private static final Color BG = new Color(13, 16, 20), PANEL = new Color(23, 28, 34), GREEN = new Color(80, 220, 140), BLUE = new Color(65, 165, 255), ORANGE = new Color(255, 155, 65);
    private JFrame frame;
    private JComboBox<Release> versions;
    private JTextField directory;
    private JTextArea log;
    private JButton install;
    private List<Release> releases = new ArrayList<Release>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() { public void run() { new CyloxInstaller().show(); } });
    }

    private void show() {
        frame = new JFrame("Cylox Client Installer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(520, 610);
        frame.setMinimumSize(new Dimension(360, 500));
        frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel header = new JPanel(new GridLayout(0, 1, 4, 4)); header.setOpaque(false);
        JLabel title = new JLabel("CYLOX CLIENT"); title.setForeground(GREEN); title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel sub = new JLabel("JAVA EDITION • ANDROID INSTALLER"); sub.setForeground(new Color(165, 178, 190));
        header.add(title); header.add(sub); root.add(header, BorderLayout.NORTH);
        JPanel center = new JPanel(); center.setOpaque(false); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(label("CLIENT VERSION", BLUE));
        versions = new JComboBox<Release>(); versions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); styleCombo(versions); center.add(versions);
        center.add(Box.createVerticalStrut(16)); center.add(label("MINECRAFT DIRECTORY", ORANGE));
        JPanel path = new JPanel(new BorderLayout(6, 0)); path.setOpaque(false);
        directory = new JTextField(defaultDirectory().getAbsolutePath()); directory.setForeground(Color.WHITE); directory.setBackground(PANEL); directory.setCaretColor(GREEN);
        JButton browse = button("Browse", BLUE); browse.addActionListener(e -> chooseDirectory());
        path.add(directory, BorderLayout.CENTER); path.add(browse, BorderLayout.EAST); center.add(path);
        center.add(Box.createVerticalStrut(14));
        log = new JTextArea(10, 30); log.setEditable(false); log.setLineWrap(true); log.setWrapStyleWord(true); log.setBackground(new Color(8, 11, 14)); log.setForeground(new Color(205, 215, 225)); log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(log); scroll.setBorder(BorderFactory.createLineBorder(new Color(48, 60, 70))); center.add(scroll);
        root.add(center, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(8, 8)); bottom.setOpaque(false);
        install = button("INSTALL SELECTED VERSION", GREEN); install.setForeground(new Color(8, 18, 12)); install.setFont(new Font("SansSerif", Font.BOLD, 14));
        install.addActionListener(e -> beginInstall()); bottom.add(install, BorderLayout.CENTER);
        JLabel footer = new JLabel("Developed by dapdev • Forge 1.8.9"); footer.setForeground(new Color(125, 140, 150)); footer.setHorizontalAlignment(SwingConstants.CENTER); bottom.add(footer, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);
        frame.setContentPane(root); frame.setVisible(true);
        log("Loading Cylox releases from GitHub...");
        loadReleases();
    }

    private JLabel label(String text, Color color) { JLabel l = new JLabel(text); l.setForeground(color); l.setFont(new Font("SansSerif", Font.BOLD, 11)); l.setBorder(new EmptyBorder(10, 0, 6, 0)); return l; }
    private JButton button(String text, Color c) { JButton b = new JButton(text); b.setBackground(c); b.setForeground(Color.WHITE); b.setFocusPainted(false); b.setBorder(new EmptyBorder(10, 14, 10, 14)); return b; }
    private void styleCombo(JComboBox<?> c) { c.setBackground(PANEL); c.setForeground(Color.WHITE); c.setFont(new Font("SansSerif", Font.PLAIN, 14)); }
    private File defaultDirectory() {
        String home = System.getProperty("user.home", ".");
        File a = new File(home, ".minecraft");
        if (a.isDirectory()) return a;
        File b = new File(System.getProperty("user.dir", "."));
        if (new File(b, "versions").isDirectory() || new File(b, "libraries").isDirectory()) return b;
        return a;
    }
    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser(directory.getText()); chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Minecraft game directory");
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) directory.setText(chooser.getSelectedFile().getAbsolutePath());
    }
    private void log(final String s) { SwingUtilities.invokeLater(() -> { log.append(s + "\n"); log.setCaretPosition(log.getDocument().getLength()); }); }
    private void loadReleases() {
        new Thread(() -> {
            try {
                String json = readUrl(API);
                Matcher blocks = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL).matcher(json);
                while (blocks.find()) {
                    String block = blocks.group();
                    String tag = field(block, "tag_name"), name = field(block, "name");
                    if (tag == null || tag.equals("latest")) continue;
                    Matcher assets = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL).matcher(section(block, "assets"));
                    String url = null;
                    while (assets.find()) {
                        String asset = assets.group(), file = field(asset, "name");
                        if (file != null && file.startsWith("Cylox-Client-Forge-1.8.9-") && file.endsWith(".jar")) url = field(asset, "browser_download_url");
                    }
                    if (url != null) releases.add(new Release(name == null ? tag : name, tag, url));
                }
                if (releases.isEmpty()) throw new IOException("No versioned client releases found yet.");
                SwingUtilities.invokeLater(() -> {
                    for (Release r : releases) versions.addItem(r);
                    versions.setSelectedIndex(0); log("Found " + releases.size() + " client version(s).");
                });
            } catch (Exception ex) { log("Release check failed: " + ex.getMessage()); log("Check your internet connection or GitHub Releases."); }
        }, "cylox-release-check").start();
    }
    private static String section(String s, String key) { int i=s.indexOf("\"" + key + "\""); if(i<0)return ""; int a=s.indexOf('[',i), b=s.indexOf(']',a); return a<0||b<0?"":s.substring(a+1,b); }
    private static String field(String s, String key) {
        Matcher m=Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(s);
        return m.find()?m.group(1).replace("\\/", "/").replace("\\u0026","&"):null;
    }
    private static String readUrl(String address) throws IOException {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection(); c.setConnectTimeout(15000); c.setReadTimeout(20000); c.setRequestProperty("User-Agent","Cylox-Client-Installer");
        try { InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buf=new byte[8192]; int n; while((n=in.read(buf))!=-1)out.write(buf,0,n); return new String(out.toByteArray(),"UTF-8"); } finally { c.disconnect(); }
    }
    private void beginInstall() {
        final Release r=(Release)versions.getSelectedItem();
        final File mc=new File(directory.getText().trim());
        if(r==null) { log("No client version selected."); return; }
        if(!mc.isDirectory()) { JOptionPane.showMessageDialog(frame,"Minecraft directory does not exist. Select a valid directory first."); return; }
        install.setEnabled(false);
        new Thread(() -> {
            try {
                log("Selected " + r.name + " (" + r.tag + ")");
                log("Preparing Forge 1.8.9...");
                File forge=new File(System.getProperty("java.io.tmpdir"),"cylox-forge-1.8.9-installer.jar");
                download(FORGE_URL, forge);
                log("Running official Forge installer...");
                runForgeInstaller(forge, mc);
                log("Forge installer finished. Preparing Cylox mod...");
                File mods=new File(mc,"mods"); if(!mods.exists() && !mods.mkdirs()) throw new IOException("Cannot create mods directory: "+mods);
                File mod=new File(mods,"Cylox-Client-"+r.tag+"-Forge-1.8.9.jar");
                download(r.url,mod);
                log("Installed client mod: "+mod.getName());
                log("Installation finished. Select Forge 1.8.9 in MJ Launcher.");
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,"Cylox Client "+r.tag+" installed. Select the Forge 1.8.9 profile in MJ Launcher.","Cylox Installer",JOptionPane.INFORMATION_MESSAGE));
            } catch(Exception ex) { log("INSTALL ERROR: "+ex.toString()); SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,ex.toString(),"Installation failed",JOptionPane.ERROR_MESSAGE)); }
            finally { SwingUtilities.invokeLater(() -> install.setEnabled(true)); }
        },"cylox-install").start();
    }
    private void download(String url, File target) throws IOException {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection(); c.setConnectTimeout(20000); c.setReadTimeout(30000); c.setRequestProperty("User-Agent","Cylox-Client-Installer");
        try { int code=c.getResponseCode(); if(code<200||code>=300) throw new IOException("HTTP "+code+" while downloading "+url); File parent=target.getParentFile(); if(parent!=null)parent.mkdirs(); InputStream in=c.getInputStream(); FileOutputStream out=new FileOutputStream(target); try { byte[] buf=new byte[16384]; int n; while((n=in.read(buf))!=-1)out.write(buf,0,n); } finally { out.close(); in.close(); } } finally { c.disconnect(); }
    }
    private void runForgeInstaller(File jar, File mc) throws Exception {
        URLClassLoader loader=new URLClassLoader(new URL[]{jar.toURI().toURL()}, ClassLoader.getSystemClassLoader());
        Class<?> main=Class.forName("net.minecraftforge.installer.SimpleInstaller",true,loader);
        SecurityManager previous=System.getSecurityManager();
        try {
            System.setSecurityManager(new ExitBlocker());
            try { main.getMethod("main",String[].class).invoke(null,(Object)new String[]{"--installClient",mc.getAbsolutePath()}); }
            catch(java.lang.reflect.InvocationTargetException e) { Throwable cause=e.getCause(); if(!(cause instanceof SecurityException)) throw new Exception(cause); }
        } finally { try { System.setSecurityManager(previous); } catch(Exception ignored) {} loader.close(); }
        File profile=new File(mc,"versions/1.8.9-forge1.8.9-11.15.1.2318-1.8.9");
        if(!profile.exists()) {
            File versionsDir=new File(mc,"versions"); boolean found=false;
            if(versionsDir.isDirectory()) { File[] fs=versionsDir.listFiles(); if(fs!=null) for(File f:fs) if(f.getName().toLowerCase(Locale.ROOT).contains("forge")&&f.getName().contains("1.8.9")) found=true; }
            if(!found) throw new IOException("Forge profile was not created. This launcher/runtime may not support the embedded Forge installer.");
        }
    }
    private static final class ExitBlocker extends SecurityManager {
        public void checkPermission(java.security.Permission p) {}
        public void checkExit(int status) { throw new SecurityException("Blocked installer exit " + status); }
    }
    private static final class Release {
        final String name,tag,url;
        Release(String n,String t,String u){name=n;tag=t;url=u;}
        public String toString(){return name+"  •  "+tag;}
    }
}