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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class PhotoDistApp extends JFrame {

    final PhotoPanel leftPhotoPanel, rightPhotoPanel;
    final JMenuItem fileSave;

    final Geometry geom = new Geometry();

    public PhotoDistApp() throws HeadlessException {
        setTitle("PhotoDist");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Container cp = getContentPane();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');

        fileSave = new JMenuItem("Save geometry...", 's');
        fileSave.setEnabled(false);
        fileSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (leftPhotoPanel.getImage().getWidth() != rightPhotoPanel.getImage().getWidth()
                    || leftPhotoPanel.getImage().getHeight() != rightPhotoPanel.getImage().getHeight()) {
                    JOptionPane.showMessageDialog(rootPane,
                        "Left and right image dimensions are different!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("geometry.txt"));
                if (fc.showSaveDialog(rootPane) == JFileChooser.APPROVE_OPTION) {
                    geom.triangulate(leftPhotoPanel.getImage().getWidth(),
                        leftPhotoPanel.getImage().getHeight());
                    try {
                        geom.export3DGeometry(new PrintStream(fc.getSelectedFile()));
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(rootPane,
                            "Error writing to selected file.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(fileSave);

        fileMenu.addSeparator();

        JMenuItem fileReset = new JMenuItem("Clear geometry", 'c');
        fileReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                geom.reset();
            }
        });
        fileMenu.add(fileReset);

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
        JPanel leftButtonPanel = new JPanel();
        JButton leftLoadButton = new JButton("Choose left image...");
        leftLoadButton.addActionListener(new LoadButtonActionListener(leftPhotoPanel));
        leftButtonPanel.add(leftLoadButton);
        leftPanel.add(leftButtonPanel, BorderLayout.PAGE_END);
        leftPanel.setPreferredSize(new Dimension(320, 480));

        final JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(new TitledBorder("Right"));
        rightPhotoPanel = new PhotoPanel(geom, 1);
        rightPanel.add(rightPhotoPanel, BorderLayout.CENTER);
        JPanel rightButtonPanel = new JPanel();
        JButton rightLoadButton = new JButton("Choose right image...");
        rightLoadButton.addActionListener(new LoadButtonActionListener(rightPhotoPanel));
        rightButtonPanel.add(rightLoadButton);
        rightPanel.add(rightButtonPanel, BorderLayout.PAGE_END);
        leftPanel.setPreferredSize(new Dimension(320, 480));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        cp.add(mainPanel, BorderLayout.CENTER);

        JPanel paramPanel = new JPanel();
        paramPanel.add(new JLabel("Horiz. FOV (deg):"));
        JSpinner hFOVspinner = new JSpinner(
                new SpinnerNumberModel(geom.getHorizontalFOV(), 1, 90, 1));
        hFOVspinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                geom.setHorizontalFOV((double)spinner.getValue());
            }
        });
        paramPanel.add(hFOVspinner);

        paramPanel.add(new JLabel("  Vert. FOV (deg):"));
        JSpinner vFOVspinner = new JSpinner(
                new SpinnerNumberModel(geom.getVeritcalFOV(), 1, 90, 1));
        vFOVspinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                geom.setVerticalFOV((double)spinner.getValue());
            }
        });
        paramPanel.add(vFOVspinner);

        paramPanel.add(new JLabel("  Sep. (m):"));
        JSpinner distSpinner = new JSpinner(
                new SpinnerNumberModel(geom.getSeparation(), 0.05, 10, 0.1));
        distSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner spinner = (JSpinner) e.getSource();
                geom.setSeparation((double)spinner.getValue());
            }
        });
        paramPanel.add(distSpinner);
        cp.add(paramPanel, BorderLayout.PAGE_END);

        pack();
        
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PhotoDistApp().setVisible(true);
            }
        });
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

                    if (leftPhotoPanel.getImage() != null
                        && rightPhotoPanel.getImage() != null)
                        fileSave.setEnabled(true);

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
