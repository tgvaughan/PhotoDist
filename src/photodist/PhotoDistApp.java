/*
 * Copyright (C) 2015 Tim Vaughan (tgvaughan@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package photodist;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class PhotoDistApp extends JFrame {

    PhotoPanel leftPhotoPanel, rightPhotoPanel;

    Geometry geom = new Geometry();

    public PhotoDistApp() throws HeadlessException {
        setTitle("PhotoDist");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container cp = getContentPane();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');

        JMenuItem fileSave = new JMenuItem("Save geometry...", 's');
        fileMenu.add(fileSave);

        fileMenu.addSeparator();

        JMenuItem fileExit = new JMenuItem("Exit", 'x');
        fileExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(fileExit);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        final JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Left"));
        leftPhotoPanel = new PhotoPanel(geom, 0);
        leftPanel.add(leftPhotoPanel, BorderLayout.CENTER);
        JButton leftLoadButton = new JButton("Choose left image...");
        leftLoadButton.addActionListener(new LoadButtonActionListener(leftPhotoPanel));
        leftPanel.setPreferredSize(new Dimension(320, 480));
        leftPanel.add(leftLoadButton, BorderLayout.PAGE_END);

        final JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Right"));
        rightPhotoPanel = new PhotoPanel(geom, 1);
        rightPanel.add(rightPhotoPanel, BorderLayout.CENTER);
        JButton rightLoadButton = new JButton("Choose right image...");
        rightLoadButton.addActionListener(new LoadButtonActionListener(rightPhotoPanel));
        leftPanel.setPreferredSize(new Dimension(320, 480));
        rightPanel.add(rightLoadButton, BorderLayout.PAGE_END);


        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        cp.add(mainPanel, BorderLayout.CENTER);

        pack();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PhotoDistApp().setVisible(true);
            }
        });
        // TODO code application logic here
    }

    private class LoadButtonActionListener implements ActionListener {

        PhotoPanel photoPanel;

        public LoadButtonActionListener(PhotoPanel photoPanel) {
            this.photoPanel = photoPanel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.addChoosableFileFilter(new FileNameExtensionFilter("Image files",
                "jpeg", "jpg", "png", "gif", "tiff"));
            if (fc.showOpenDialog(getContentPane()) == JFileChooser.APPROVE_OPTION) {
                try {
                    BufferedImage image = ImageIO.read(fc.getSelectedFile());
                    photoPanel.setImage(image);
                    photoPanel.repaint();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(getContentPane(),
                        "Error loading image from file `"
                            + fc.getSelectedFile().getName(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }
    
}
