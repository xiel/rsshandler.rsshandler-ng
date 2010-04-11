package com.rsshandler;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

public class Gui implements ClipboardOwner {
  private PodcastServer server;
  private JMenuItem startServer;
  private JMenuItem stopServer;
  private JFrame frame;
  public static final int FLV = 35;
  private int port = -1;
  private boolean proxyMode = false;
  private static final String PROXY_MODE_PREF = "proxyMode";
  private static final String PORT_PREF = "port";

  public void setServer(PodcastServer server) {
    this.server = server;
  }

  public void createGui() {
    frame = new JFrame("RSS Handler");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setJMenuBar(createMenu());
    frame.getContentPane().add(createGeneratorPanel(), BorderLayout.CENTER);
    frame.pack();
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    proxyMode = prefs.getBoolean(PROXY_MODE_PREF, false);
    port = prefs.getInt(PORT_PREF, 8083);
    frame.setLocationByPlatform(true);
    frame.setVisible(true);
    startServer();
  }

  private JMenuBar createMenu() {
    JMenuBar greenMenuBar = new JMenuBar();
    greenMenuBar.setOpaque(true);
    JMenu file = new JMenu("File");
    JMenuItem exit = new JMenuItem("Exit");
    startServer = new JMenuItem("Start server");
    stopServer = new JMenuItem("Stop server");
    startServer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startServer();
      }
    });
    stopServer.setEnabled(false);
    stopServer.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopServer();
      }
    });

    JMenuItem settingsMenu = new JMenuItem("Settings...");
    settingsMenu.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        JDialog settings = new JDialog(frame, "Settings", true);
        settings.setContentPane(createStartStopPanel(settings, port, proxyMode));
        settings.pack();
        settings.setLocationRelativeTo(frame);
        settings.setVisible(true);
      }
    });

    JMenu help = new JMenu("Help");
    JMenuItem about = new JMenuItem("About");
    about.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        String version = this.getClass().getPackage().getImplementationVersion();
        JOptionPane.showMessageDialog(frame, "Version: " + version, "About", JOptionPane.INFORMATION_MESSAGE);
      }
    });

    exit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        System.exit(0);
      }
    });

    file.add(settingsMenu);
    file.addSeparator();
    file.add(startServer);
    file.add(stopServer);
    file.addSeparator();
    file.add(exit);
    help.add(about);
    greenMenuBar.add(file);
    greenMenuBar.add(help);
    return greenMenuBar;
  }

  private JPanel createGeneratorPanel() {
    JPanel infoPanel = new JPanel(new MigLayout("", "[][grow][]", "[][][][][grow]"));
    JLabel typeLabel = new JLabel("");
    JLabel formatLabel = new JLabel("Format");
    JLabel orderbyLabel = new JLabel("Order by");
    JLabel removeLabel = new JLabel("Remove feed content");
    final JLabel idLabel = new JLabel("ID");
    final JLabel standardFeedLabel = new JLabel("Feed type");
    standardFeedLabel.setVisible(false);
    JLabel sizeLabel = new JLabel("Size");
    JLabel resultLabel = new JLabel("Result podcast URL");

    final JComboBox standardFeeds = new JComboBox(new Object[] { new StandardFeed("recently_featured", "Recently featured"), new StandardFeed("most_viewed", "Most viewed"),
        new StandardFeed("most_popular", "Most popular"), new StandardFeed("most_recent", "Most recent"), new StandardFeed("most_discussed", "Most discussed"),
        new StandardFeed("most_linked", "Most linked"), new StandardFeed("most_responded", "Most responded"), new StandardFeed("top_rated", "Top rated"),
        new StandardFeed("top_favorites", "Top favorites"), });

    standardFeeds.setVisible(false);
    ButtonGroup types = new ButtonGroup();
    final JRadioButton typeUser = new JRadioButton("User");
    final JRadioButton typePlaylist = new JRadioButton("Playlist");
    final JRadioButton typeFavorites = new JRadioButton("Favorites");
    final JRadioButton typeStandart = new JRadioButton("Standart");
    types.add(typeUser);
    types.add(typePlaylist);
    types.add(typeFavorites);
    types.add(typeStandart);
    typeUser.setSelected(true);
    ButtonGroup sizes = new ButtonGroup();
    final JRadioButton size25 = new JRadioButton("25 items");
    final JRadioButton size50 = new JRadioButton("50 items");
    sizes.add(size25);
    sizes.add(size50);
    size25.setSelected(true);

    ButtonGroup orderby = new ButtonGroup();
    final JRadioButton orderbyPublished = new JRadioButton("published");
    orderbyPublished.setToolTipText("Order by publishing date in reverse order");
    final JRadioButton orderbyPosition = new JRadioButton("position (for playlists)");
    orderbyPosition.setToolTipText("Order by item position in playlist, used in playlists");
    orderby.add(orderbyPublished);
    orderby.add(orderbyPosition);
    orderbyPublished.setSelected(true);
    
    final JCheckBox removeDescription = new JCheckBox("description");
    removeDescription.setToolTipText("Remove description from feed items");
    final JCheckBox removeTitle = new JCheckBox("title");
    removeTitle.setToolTipText("Remove title from feed items");
    removeDescription.setSelected(true);

    final JTextField id = new JTextField();
    id.setToolTipText("Name that identifies feed with given type: for user and favorites feeds - user name, for playlists - playlist id");
    final JTextArea result = new JTextArea();
    result.setToolTipText("Copy paste this text to your podcast player as podcast link");
    result.setLineWrap(true);
    typeStandart.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          standardFeedLabel.setVisible(true);
          standardFeeds.setVisible(true);
          idLabel.setVisible(false);
          id.setVisible(false);
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
          standardFeedLabel.setVisible(false);
          standardFeeds.setVisible(false);
          idLabel.setVisible(true);
          id.setVisible(true);
        }
      }
    });

    /*
     * 6 = 320x180 @ FLV; 18 = 480x270 @ MP4; 22 = 1280x720 @ MP4; 35 = 640x360
     * 
     * @ FLV;
     */

    final JComboBox formats = new JComboBox(new Object[] { new YoutubeFormat(18, "MP4 (iTunes)"), new YoutubeFormat(17, "Compact PSP"), new YoutubeFormat(FLV, "FLV"),
        new YoutubeFormat(22, "HD (MP4)") });
    JButton copyButton = new JButton("Copy to buffer");
    copyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        StringSelection stringSelection = new StringSelection(result.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, Gui.this);
      }
    });
    JButton generateButton = new JButton("Generate podcast URL");
    generateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = id.getText();
        if ((text.length() == 0) && !typeStandart.isSelected()) {
          JOptionPane.showMessageDialog(frame, "You must enter id", "Input error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        int size = -1;
        if (size25.isSelected()) {
          size = 25;
        } else if (size50.isSelected()) {
          size = 50;
        } else {
          JOptionPane.showMessageDialog(frame, "Select feed size", "Input error", JOptionPane.ERROR_MESSAGE);
        }
        String orderby = null;
        if (orderbyPublished.isSelected()) {
          orderby = "published";
        } else if (orderbyPosition.isSelected()) {
          orderby = "position";
        } else {
          JOptionPane.showMessageDialog(frame, "Select order by", "Input error", JOptionPane.ERROR_MESSAGE);
        }
        int format = ((YoutubeFormat) formats.getSelectedItem()).getId();
        if (typeUser.isSelected()) {
          result.setText(getUserPodcastUrl(text, format, size, orderby, removeDescription.isSelected(), removeTitle.isSelected()));
        } else if (typePlaylist.isSelected()) {
          result.setText(getPlaylistPodcastUrl(text, format, size, orderby, removeDescription.isSelected(), removeTitle.isSelected()));
        } else if (typeFavorites.isSelected()) {
          result.setText(getFavoritesPodcastUrl(text, format, size, orderby, removeDescription.isSelected(), removeTitle.isSelected()));
        } else if (typeStandart.isSelected()) {
          StandardFeed feed = (StandardFeed) standardFeeds.getSelectedItem();
          result.setText(getStandardUrl(feed, format, size, orderby, removeDescription.isSelected(), removeTitle.isSelected()));
        } else {
          JOptionPane.showMessageDialog(frame, "Select feed type", "Input error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    infoPanel.add(typeLabel, "");
    infoPanel.add(typeUser, "split 4");
    infoPanel.add(typePlaylist, "");
    infoPanel.add(typeFavorites, "");
    infoPanel.add(typeStandart, "wrap");
    infoPanel.add(formatLabel, "");
    infoPanel.add(formats, "wrap");
    infoPanel.add(sizeLabel, "");
    infoPanel.add(size25, "split 2");
    infoPanel.add(size50, "wrap");
    infoPanel.add(orderbyLabel, "");
    infoPanel.add(orderbyPublished, "split 2");
    infoPanel.add(orderbyPosition, "wrap");
    infoPanel.add(removeLabel, "");
    infoPanel.add(removeDescription, "split 2");
    infoPanel.add(removeTitle, "wrap");
    infoPanel.add(idLabel, "split 2,hidemode 2");
    infoPanel.add(standardFeedLabel, "hidemode 2");
    infoPanel.add(id, "growx, width 30::,split 2,hidemode 2");
    infoPanel.add(standardFeeds, "growx, width 30::,hidemode 2");
    infoPanel.add(generateButton, "wrap");
    infoPanel.add(resultLabel, "top");
    infoPanel.add(result, "grow, width 30:300:, height 10:100:");
    infoPanel.add(copyButton, "top");
    return infoPanel;
  }

  private String getStandardUrl(StandardFeed feed, int format, int size, String orderby, boolean removeDescription, boolean removeTitle) {
    return getPodcastUrl("standard", feed.getId(), format, size, orderby, removeDescription, removeTitle);
  }

  private String getPlaylistPodcastUrl(String text, int format, int size, String orderby, boolean removeDescription, boolean removeTitle) {
    return getPodcastUrl("playlist", text, format, size, orderby, removeDescription, removeTitle);
  }

  private String getFavoritesPodcastUrl(String text, int format, int size, String orderby, boolean removeDescription, boolean removeTitle) {
    return getPodcastUrl("favorite", text, format, size, orderby, removeDescription, removeTitle);
  }

  private String getUserPodcastUrl(String text, int format, int size, String orderby, boolean removeDescription, boolean removeTitle) {
    return getPodcastUrl("user.rss", text, format, size, orderby, removeDescription, removeTitle);
  }

  private String getPodcastUrl(String type, String text, int format, int size, String orderby, boolean removeDescription, boolean removeTitle) {
    String hostname = getHostName();
    return "http://" + getHostName() + ":" + port + "/" + type + "?id=" + text + "&format=" + format + "&host=" + hostname + "&port=" + port + "&size=" + size + "&orderby=" + orderby + "&removeDescription=" + removeDescription + "&removeTitle=" + removeTitle;
  }

  private String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      JOptionPane.showMessageDialog(frame, "Cann't detect IP address, please change it manually", "Error", JOptionPane.ERROR_MESSAGE);
      return "<INSERT YOUR IP ADDRESS>";
    }
  }

  private JPanel createStartStopPanel(final JDialog settings, int portValue, boolean proxyModeValue) {
    final JCheckBox proxyMode = new JCheckBox();
    proxyMode.setToolTipText("Check this to proxy all videos through program, otherwise users will be redirected to result video directly from YouTube");
    proxyMode.setSelected(proxyModeValue);
    final JTextField port = new JTextField(5);
    port.setToolTipText("Server port number for podcast server");
    port.setText("" + portValue);
    port.setInputVerifier(new InputVerifier() {
      public boolean verify(JComponent input) {
        String value = port.getText();
        int port = -1;
        try {
          port = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        if ((port > 0) && (port < 65535)) {
          return true;
        } else {
          JOptionPane.showMessageDialog(settings, "Port must be number between 0 and 65535", "Input error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }

      @Override
      public boolean shouldYieldFocus(JComponent input) {
        return verify(input);
      }
    });
    JButton updatePortButton = new JButton("Use new settings");
    updatePortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings(Integer.parseInt(port.getText()), proxyMode.isSelected());
        stopServer();
        startServer();
        settings.dispose();
      }
    });
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        settings.dispose();
      }
    });

    JPanel serverPanel = new JPanel(new MigLayout("", "[grow][grow]", "[][][grow]"));
    serverPanel.add(new JLabel("Port"), "w 50%");
    serverPanel.add(port, "w 50%, wrap");
    serverPanel.add(new JLabel("Proxy mode"), "");
    serverPanel.add(proxyMode, "wrap");
    serverPanel.add(updatePortButton, "split 2, span 2, center");
    serverPanel.add(cancel, "");
    return serverPanel;
  }

  private void saveSettings(int port, boolean isProxyMode) {
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    prefs.putBoolean(PROXY_MODE_PREF, isProxyMode);
    prefs.putInt(PORT_PREF, port);
    this.port = port;
    this.proxyMode = isProxyMode;
  }

  private void stopServer() {
    boolean result = server.stop();
    if (result) {
      stopServer.setEnabled(false);
      startServer.setEnabled(true);
    } else {
      JOptionPane.showMessageDialog(frame, "Cann't stop server, for error message - check logs", "Server error", JOptionPane.ERROR_MESSAGE);
    }
    updateStats();
  }

  private static void updateStats() {
  }

  public void startServer() {
    startServer.setEnabled(false);
    stopServer.setEnabled(true);
    new Thread(new Runnable() {
      public void run() {
        server.setPort(port);
        server.setProxyMode(proxyMode);
        boolean result = server.start();
        if (!result) {
          JOptionPane.showMessageDialog(frame, "Cann't start server, for error message - check logs", "Server error", JOptionPane.ERROR_MESSAGE);
          startServer.setEnabled(true);
          stopServer.setEnabled(false);
        }
      }
    }).start();
    updateStats();
  }

  public static void main(String[] args) {
    PodcastServer server = new PodcastServer() {
      @Override
      public int getPort() {
        return 0;
      }

      @Override
      public void setPort(int parseInt) {
      }

      @Override
      public boolean start() {
        return false;
      }

      @Override
      public boolean stop() {
        return false;
      }

      @Override
      public boolean isProxyMode() {
        return false;
      }

      @Override
      public void setProxyMode(boolean selected) {
      }
    };
    Gui gui = new Gui();
    gui.setServer(server);
    gui.createGui();
  }

  private class YoutubeFormat {
    private int id;
    private String name;

    public YoutubeFormat(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      YoutubeFormat that = (YoutubeFormat) o;
      if (id != that.id)
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      return id;
    }

    public int getId() {
      return id;
    }
  }

  @Override
  public void lostOwnership(Clipboard arg0, Transferable arg1) {
  }

  private class StandardFeed {
    private String id;
    private String name;

    public StandardFeed(String id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      StandardFeed that = (StandardFeed) o;
      if (!id.equals(that.id))
        return false;
      return true;
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    public String getId() {
      return id;
    }
  }
}