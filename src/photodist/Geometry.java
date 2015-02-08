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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tim Vaughan (tgvaughan@gmail.com)
 */
public class Geometry {

    List<GeometryListener> listeners;

    public static class PointPair {
        int[] x, y;
        double trueX, trueY, trueZ;

        public PointPair() {
            x = new int[2];
            y = new int[2];
        }
    }

    List<List<PointPair>> paths;
    double fovH, fovV, sep;

    List<PointPair> currentPath;

    PointPair focusedPoint;

    public Geometry() {
        paths = new ArrayList<>();
        listeners = new ArrayList<>();

        fovH = 15;
        fovV = 15;
        sep = 0.3;
    }

    public void addListener(GeometryListener gl) {
        listeners.add(gl);
    }

    public void notifyListeners() {
        for (GeometryListener listener : listeners)
            listener.geometryChanged();
    }

    public void addPath(List<PointPair> path) {
        paths.add(path);
        notifyListeners();
    }

    /**
     * Clear all paths and reset parameters to defaults.
     */
    public void reset() {
        currentPath = null;
        paths.clear();
        notifyListeners();
    }

    public List<List<PointPair>> getPaths() {
        return paths;
    }

    public List<PointPair> getCurrentPath() {
        return currentPath;
    }

    public PointPair getFocusedPoint() {
        return focusedPoint;
    }

    public void endPath() {
        if (currentPath.size()<2)
            paths.remove(currentPath);

        currentPath = null;
        notifyListeners();
    }

    public void addPoint(PointPair point) {
        if (currentPath == null) {
            currentPath = new ArrayList<>();
            paths.add(currentPath);
        }
        currentPath.add(point);
        notifyListeners();
    }

    public void setSeparation(double separation) {
        sep = separation;
    }

    public double getSeparation() {
        return sep;
    }

    public void setHorizontalFOV(double fov) {
        fovH = fov;
    }

    public double getHorizontalFOV() {
        return fovH;
    }

    public void setVerticalFOV(double fov) {
        fovV = fov;
    }

    public double getVeritcalFOV() {
        return fovV;
    }

    /**
     * Compute Euclidean distance between (x,y) and point pidx of PointPair.
     * 
     * @param x
     * @param y
     * @param pidx
     * @param pair
     * @return 
     */
    public double euDist(int x, int y, int pidx, PointPair pair) {
        double res = Math.sqrt(Math.pow(x-pair.x[pidx],2) + Math.pow(y-pair.y[pidx],2));
        return res;
    }

    public void updateFocusedPoint(int x, int y, int pidx) {
        PointPair newFP = null;
        for (List<PointPair> path : getPaths()) {
            for (PointPair pair : path) {
                if (euDist(x,y,pidx, pair)<50) {
                    if (newFP == null
                            || (euDist(x,y,pidx, pair) < euDist(x,y,pidx, newFP))) {
                            newFP = pair;
                    }
                }
            }
        }

        if (newFP != focusedPoint) {
            focusedPoint = newFP;
            notifyListeners();
        }
    }

    /**
     * Use point pairs to guestimate 3D positions.
     * 
     * @param imageWidth
     * @param imageHeight
     */
    public void triangulate(int imageWidth, int imageHeight) {
        double thetaFactor = fovH/(2*imageWidth)*Math.PI/180;
        double phiFactor = fovV/(2*imageHeight)*Math.PI/180;
        for (List<PointPair> path : getPaths()) {
            for (PointPair pair : path) {
                double tantheta0 = Math.tan((pair.x[0]-0.5*imageWidth)*thetaFactor);
                double tantheta1 = Math.tan((pair.x[1]-0.5*imageWidth)*thetaFactor);
                double tanphi = Math.tan((pair.y[0]-0.5*imageHeight)*phiFactor);

                pair.trueX = sep/(tantheta1 - tantheta0) + 0.5*sep;
                pair.trueY = pair.trueX*tantheta1;
                pair.trueZ = pair.trueY*tanphi;
            }
        }
    }

    public void export3DGeometry(PrintStream pstream) {
        pstream.println("x y z path");
        for (int i=0; i<paths.size(); i++) {
            for (PointPair pair : paths.get(i)) {
                pstream.format("%g %g %g %d\n",
                    pair.trueX, pair.trueY, pair.trueZ, i);
            }
        }
    }
}
