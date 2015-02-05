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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class PhotoPanel extends JPanel {

    BufferedImage image;

    final Geometry geom;
    final int pidx, opidx;

    public PhotoPanel(Geometry geometry, int panelIdx) {
        this.geom = geometry;
        this.pidx = panelIdx;
        this.opidx = 1-pidx;

        geometry.addListener(new GeometryListener() {
            @Override
            public void geometryChanged() {
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getImage() == null)
                    return;

                if (e.getButton() == MouseEvent.BUTTON3) {
                    geom.endPath();
                    return;
                }

                Geometry.PointPair newPointPair = new Geometry.PointPair();
                newPointPair.x[pidx] = e.getX()*image.getWidth()/getWidth();
                newPointPair.x[opidx] = newPointPair.x[pidx];
                newPointPair.y[pidx] = e.getY()*image.getHeight()/getHeight();
                newPointPair.y[opidx] = newPointPair.y[pidx];
                geom.addPoint(newPointPair);

                System.out.println(String.format("(%d,%d)", e.getX(), e.getY()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                repaint();
            }

            
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (geom.getCurrentPath() != null)
                    repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        if (image == null) {
            super.paintComponent(g);
            return;
        }

        g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);

        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        for (List<Geometry.PointPair> path : geom.getPaths()) {
            for (int i=1; i<path.size(); i++) {
                g2d.drawLine(path.get(i-1).x[pidx]*getWidth()/image.getWidth(),
                    path.get(i-1).y[pidx]*getHeight()/image.getHeight(),
                    path.get(i).x[pidx]*getWidth()/image.getWidth(),
                    path.get(i).y[pidx]*getHeight()/image.getHeight());
//                g2d.fill(new Ellipse2D.Double(pair.x[pidx]*getWidth()/image.getWidth()-2,
//                    pair.y[pidx]*getHeight()/image.getHeight()-2, 4, 4));
            }
        }

        g2d.setColor(Color.YELLOW);
        Point mousePosition = getMousePosition();
        if (geom.getCurrentPath() != null
            && geom.getCurrentPath().size()>0
            && mousePosition != null
            && mousePosition.x>=0 && mousePosition.x<getWidth()
            && mousePosition.y>=0 && mousePosition.y<getHeight()) {
            Geometry.PointPair lastPair = geom.getCurrentPath().get(geom.getCurrentPath().size()-1);
            g2d.draw(new Line2D.Double(mousePosition.x, mousePosition.y,
                lastPair.x[pidx]*getWidth()/image.getWidth(),
                lastPair.y[pidx]*getHeight()/image.getHeight()));
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
