package com.rsshandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Gui {
  private PodcastServer server;
  private JButton startButton;
  private JButton stopButton;
  private JFrame frame;
  public static final int FLV = 35;

  public void setServer(PodcastServer server) {
    this.server = server;
  }

  public void createGui() {
    frame = new JFrame("RSS Handler");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JMenuBar greenMenuBar = new JMenuBar();
    greenMenuBar.setOpaque(true);

    frame.setJMenuBar(greenMenuBar);

    frame.getContentPane().add(createStartStopPanel(), BorderLayout.PAGE_START);
    frame.getContentPane().add(createGeneratorPanel(), BorderLayout.CENTER);

    frame.pack();
    frame.setVisible(true);
  }

  private JPanel createGeneratorPanel() {
    JLabel typeLabel = new JLabel("");
    JLabel formatLabel = new JLabel("Format");
    JLabel idLabel = new JLabel("ID");
    JLabel resultLabel = new JLabel("Result podcast URL");

    ButtonGroup types = new ButtonGroup();
    final JRadioButton typeUser = new JRadioButton("User");
    final JRadioButton typePlaylist = new JRadioButton("Playlist");
    final JRadioButton typeFavorites = new JRadioButton("Favorites");
    types.add(typeUser);
    types.add(typePlaylist);
    types.add(typeFavorites);
    JLabel typesGap = new JLabel("");
    typeUser.setSelected(true);
    JLabel formatsGap = new JLabel("");

    final JTextField id = new JTextField();
    final JTextArea result = new JTextArea();
    result.setLineWrap(true);
/*
    6 = 320x180 @ FLV;
    18 = 480x270 @ MP4;
    22 = 1280x720 @ MP4;
    35 = 640x360 @ FLV;
*/
    final JComboBox formats = new JComboBox(new Object[] {new YoutubeFormat(18, "MP4 (iTunes)"), new YoutubeFormat(17, "Compact PSP"), new YoutubeFormat(FLV, "FLV"), new YoutubeFormat(22, "HD (MP4)")});

    JButton copyButton = new JButton("Copy to buffer");
//TODO implement
    copyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

      }
    });
    JButton generateButton = new JButton("Generate podcast URL");
    generateButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = id.getText();
        if (text.length() == 0) {
          JOptionPane.showMessageDialog(frame, "You must enter id", "Input error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        int format = ((YoutubeFormat)formats.getSelectedItem()).getId();
        if (typeUser.isSelected()) {
          result.setText(getUserPodcastUrl(text, format));
        } else if (typePlaylist.isSelected()) {
          result.setText(getPlaylistPodcastUrl(text, format));
        } else {
          JOptionPane.showMessageDialog(frame, "Select feed type", "Input error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    JPanel infoPanel = new JPanel();
    GroupLayout layout = new GroupLayout(infoPanel);
    infoPanel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
    hGroup.addGroup(layout.createParallelGroup().addComponent(typeLabel).addComponent(formatLabel).addComponent(idLabel).addComponent(resultLabel));
    hGroup.addGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addComponent(typeUser).addComponent(typePlaylist).addComponent(typeFavorites)).addComponent(formats).addComponent(id).addComponent(result, GroupLayout.DEFAULT_SIZE, 400, GroupLayout.DEFAULT_SIZE));
    hGroup.addGroup(layout.createParallelGroup().addComponent(typesGap).addComponent(formatsGap).addComponent(generateButton).addComponent(copyButton));
    layout.setHorizontalGroup(hGroup);
    GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(typeLabel).addGroup(layout.createParallelGroup().addComponent(typeUser).addComponent(typePlaylist).addComponent(typeFavorites)).addComponent(typesGap));
    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(formatLabel).addComponent(formats).addComponent(formatsGap));
    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(idLabel).addComponent(id).addComponent(generateButton));
    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(resultLabel).addComponent(result, GroupLayout.DEFAULT_SIZE, 200, GroupLayout.DEFAULT_SIZE).addComponent(copyButton));
    layout.setVerticalGroup(vGroup);
    return infoPanel;
  }

  private String getPlaylistPodcastUrl(String text, int format) {
    return getPodcastUrl("playlist", text, format);
  }

  private String getUserPodcastUrl(String text, int format) {
    return getPodcastUrl("user.rss", text, format);
  }

  private String getPodcastUrl(String type, String text, int format) {
    String hostname = getHostName();
    return "http://"+ getHostName() +":"+PodcastServerImpl.PORT+"/"+type+"?id="+text+"&format="+format+"&host="+hostname+"&port="+PodcastServerImpl.PORT;
  }

  private String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      JOptionPane.showMessageDialog(frame, "Cann't detect IP address, please change it manually", "Error", JOptionPane.ERROR_MESSAGE);
      return "<INSERT YOUR IP ADDRESS>";
    }
  }

  private JPanel createStartStopPanel() {
    JLabel serverLabel = new JLabel("Podcast server");

    startButton = new JButton("Start");
    startButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        startServer();
      }
    });
    stopButton = new JButton("Stop");
    stopButton.setEnabled(false);
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopServer();
      }
    });

    JPanel startStopPanel = new JPanel();
    GroupLayout layout = new GroupLayout(startStopPanel);
    startStopPanel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);

    GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
    hGroup.addGroup(layout.createParallelGroup().addComponent(serverLabel));
    hGroup.addGroup(layout.createParallelGroup().addComponent(startButton));
    hGroup.addGroup(layout.createParallelGroup().addComponent(stopButton));
    layout.setHorizontalGroup(hGroup);
    GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
    vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(serverLabel).addComponent(startButton).addComponent(stopButton));
    layout.setVerticalGroup(vGroup);
    return startStopPanel;
  }

  private void stopServer() {
    boolean result = server.stop();
    if (result) {
      stopButton.setEnabled(false);
      startButton.setEnabled(true);
    } else {
      JOptionPane.showMessageDialog(frame, "Cann't stop server, for error message - check logs", "Server error", JOptionPane.ERROR_MESSAGE);
    }
    updateStats();
  }

  private static void updateStats() {

  }

  public void startServer() {
    startButton.setEnabled(false);
    stopButton.setEnabled(true);
    new Thread(new Runnable() {
      public void run() {
        boolean result = server.start();
        if (!result) {
          JOptionPane.showMessageDialog(frame, "Cann't start server, for error message - check logs", "Server error", JOptionPane.ERROR_MESSAGE);
          startButton.setEnabled(true);
          stopButton.setEnabled(false);
        }
      }
    }).start();
    updateStats();
  }


  public static void main(String[] args) {
    Gui gui = new Gui();
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
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      YoutubeFormat that = (YoutubeFormat) o;

      if (id != that.id) return false;

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

}
